package com.shani.caradvisor.config;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class KeepAwakeTest {

    private final ApplicationContextRunner context = new ApplicationContextRunner().withUserConfiguration(KeepAwake.class);

    @Test
    void offOutsideRender() {
        context.run(app -> assertThat(app).doesNotHaveBean(KeepAwake.class));
    }

    @Test
    void onWhenRenderGivesThePublicAddress() {
        context.withPropertyValues("RENDER_EXTERNAL_URL=https://example.onrender.com")
                .run(app -> assertThat(app).hasSingleBean(KeepAwake.class));
    }

    @Test
    void visitsTheHomePage() throws Exception {
        List<String> visited = new CopyOnWriteArrayList<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            visited.add(exchange.getRequestURI().getPath());
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.start();
        try {
            new KeepAwake("http://127.0.0.1:" + server.getAddress().getPort()).visit();
        } finally {
            server.stop(0);
        }
        assertThat(visited).containsExactly("/");
    }

    @Test
    void aFailedVisitDoesNotThrow() {
        new KeepAwake("http://127.0.0.1:1").visit();
    }
}
