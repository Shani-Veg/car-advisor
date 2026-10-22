package com.shani.caradvisor.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Keeps the public demo awake: Render's free plan sleeps after 15 minutes without visits, and the first visit after
 * that waits about a minute. The server visits its own public address every 10 minutes, which counts as a visit.
 * Render sets RENDER_EXTERNAL_URL by itself, so this is on only there. The home page is static: no database query,
 * so the database can still sleep between real visits.
 */
@Configuration
@EnableScheduling
@ConditionalOnExpression("'${RENDER_EXTERNAL_URL:}' != ''")
public class KeepAwake {

    private static final Logger log = LoggerFactory.getLogger(KeepAwake.class);

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final URI home;

    public KeepAwake(@Value("${RENDER_EXTERNAL_URL}") String publicUrl) {
        this.home = URI.create(publicUrl.endsWith("/") ? publicUrl : publicUrl + "/");
    }

    @Scheduled(initialDelay = 60_000, fixedDelay = 600_000)
    public void visit() {
        try {
            HttpResponse<Void> response = http.send(
                    HttpRequest.newBuilder(home).timeout(Duration.ofSeconds(30)).GET().build(),
                    HttpResponse.BodyHandlers.discarding());
            log.debug("Keep-awake visit: {}", response.statusCode());
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn("Keep-awake visit failed: {}", e.toString());
        }
    }
}
