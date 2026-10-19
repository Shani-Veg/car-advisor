package com.shani.caradvisor.agent;

import com.shani.caradvisor.observability.AssistantMetrics;
import com.shani.caradvisor.observability.AssistantMetrics.CallOutcome;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The client against a local stand-in for Gemini: busy models, a fallback, waiting and going round again, giving up. */
class GeminiClientTest {

    private static final String ANSWER = "{\"candidates\":[{\"content\":{\"role\":\"model\",\"parts\":[{\"text\":\"שלום\"}]}}]}";
    private static final String BUSY = "{\"error\":{\"code\":429,\"status\":\"RESOURCE_EXHAUSTED\"}}";
    private static final String QUOTA = "{\"error\":{\"code\":429,\"details\":[{\"@type\":\"type.googleapis.com/google.rpc.RetryInfo\",\"retryDelay\":\"30s\"}]}}";

    private record Reply(int status, String body) {
    }

    private final Deque<Reply> replies = new ArrayDeque<>();
    private final List<String> calledModels = new ArrayList<>();
    private final AssistantMetrics metrics = new AssistantMetrics();
    private HttpServer server;

    private GeminiClient client() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            calledModels.add(path.substring(path.lastIndexOf('/') + 1));
            Reply reply = replies.isEmpty() ? new Reply(429, BUSY) : replies.poll();
            byte[] body = reply.body().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(reply.status(), body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        String endpoint = "http://127.0.0.1:" + server.getAddress().getPort() + "/%s";
        return new GeminiClient("test-key", "first,second", 5, 2, "", endpoint, 2, 10, metrics);
    }

    @AfterEach
    void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private LlmClient.Turn ask(GeminiClient client) {
        return client.next("system", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", "היי")))), List.of());
    }

    @Test
    void aBusyFirstModelFallsThroughToTheSecond() throws IOException {
        GeminiClient client = client();
        replies.add(new Reply(429, BUSY));
        replies.add(new Reply(200, ANSWER));

        LlmClient.Turn turn = ask(client);

        assertThat(turn.model()).isEqualTo("second");
        assertThat(turn.text()).isEqualTo("שלום");
        assertThat(metrics.snapshot().fallbackAnswers()).isEqualTo(1);
        assertThat(metrics.snapshot().backoffRounds()).isZero();
    }

    @Test
    void whenEveryModelIsBusyItWaitsAndGoesRoundAgain() throws IOException {
        GeminiClient client = client();
        replies.add(new Reply(429, BUSY));
        replies.add(new Reply(503, "{}"));
        replies.add(new Reply(200, ANSWER));

        LlmClient.Turn turn = ask(client);

        assertThat(turn.model()).isEqualTo("first");
        assertThat(calledModels).containsExactly("first", "second", "first");
        assertThat(metrics.snapshot().backoffRounds()).isEqualTo(1);
        assertThat(metrics.snapshot().modelCalls().get("first").get(CallOutcome.BUSY)).isEqualTo(1);
    }

    @Test
    void itGivesUpAfterTheLastRoundSoTheAppCanCarryOnWithoutTheModel() throws IOException {
        GeminiClient client = client();

        assertThatThrownBy(() -> ask(client)).isInstanceOf(LlmClient.UnavailableException.class);

        // three rounds (the first try and two retries) over two models
        assertThat(calledModels).hasSize(6);
        assertThat(metrics.snapshot().backoffRounds()).isEqualTo(2);
    }

    @Test
    void aQuotaThatFreesUpOnlyLaterIsNotWaitedFor() throws IOException {
        GeminiClient client = client();
        replies.add(new Reply(429, QUOTA));
        replies.add(new Reply(429, QUOTA));

        assertThatThrownBy(() -> ask(client)).isInstanceOf(LlmClient.UnavailableException.class);

        assertThat(calledModels).containsExactly("first", "second");
        assertThat(metrics.snapshot().backoffRounds()).isZero();
    }

    @Test
    void anUnusableAnswerIsTreatedLikeAMissingOneAndTheNextModelIsTried() throws IOException {
        GeminiClient client = client();
        replies.add(new Reply(200, "{\"candidates\":[]}"));
        replies.add(new Reply(200, ANSWER));

        assertThat(ask(client).model()).isEqualTo("second");
        assertThat(metrics.snapshot().modelCalls().get("first").get(CallOutcome.UNREADABLE)).isEqualTo(1);
    }

    @Test
    void theBackoffDoublesWithJitterAndNeverPassesTheCap() throws IOException {
        GeminiClient client = new GeminiClient("k", "m", 5, 2, "", GeminiClient.ENDPOINT, 2, 800, metrics);

        assertThat(client.backoff(0, 0)).isBetween(800L, 1000L);
        assertThat(client.backoff(1, 0)).isBetween(1600L, 2000L);
        assertThat(client.backoff(6, 0)).isEqualTo(GeminiClient.MAX_BACKOFF_MS);
        assertThat(client.backoff(0, 2500)).isGreaterThanOrEqualTo(2500L);
    }
}
