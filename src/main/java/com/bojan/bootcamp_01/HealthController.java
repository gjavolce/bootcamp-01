package com.bojan.bootcamp_01;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("/live")
    public Mono<Map<String, Object>> liveness() {
        return Mono.just(Map.of(
                "status", "UP",
                "timestamp", Instant.now(),
                "checks", Map.of(
                        "application", "UP")));
    }

    @GetMapping("/startup")
    public Mono<Map<String, Object>> startup() {
        return Mono.just(Map.of(
                "status", "UP",
                "timestamp", Instant.now(),
                "message", "Application has started successfully"));
    }
}
