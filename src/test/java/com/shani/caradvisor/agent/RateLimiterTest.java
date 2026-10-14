package com.shani.caradvisor.agent;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterTest {

    @Test
    void limitsEachCaseAndRecoversAfterAMinute() {
        RateLimiter limiter = new RateLimiter(2, 100);
        Instant t0 = Instant.parse("2026-10-14T10:00:00Z");

        assertThat(limiter.tryAcquire("a", t0)).isTrue();
        assertThat(limiter.tryAcquire("a", t0.plusSeconds(1))).isTrue();
        assertThat(limiter.tryAcquire("a", t0.plusSeconds(2))).isFalse();
        assertThat(limiter.tryAcquire("b", t0.plusSeconds(2))).isTrue();
        assertThat(limiter.tryAcquire("a", t0.plusSeconds(61))).isTrue();
    }

    @Test
    void limitsAllCasesTogether() {
        RateLimiter limiter = new RateLimiter(10, 2);
        Instant t0 = Instant.parse("2026-10-14T10:00:00Z");

        assertThat(limiter.tryAcquire("a", t0)).isTrue();
        assertThat(limiter.tryAcquire("b", t0)).isTrue();
        assertThat(limiter.tryAcquire("c", t0)).isFalse();
    }
}
