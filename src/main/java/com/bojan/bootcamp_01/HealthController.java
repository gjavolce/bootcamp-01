package com.bojan.bootcamp_01;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Application health check endpoints")
public class HealthController {

    @Operation(summary = "Liveness probe", description = "Returns the liveness status of the application")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application is alive",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(type = "object")))
    })
    @GetMapping("/live")
    public Mono<Map<String, Object>> liveness() {
        return Mono.just(Map.of(
                "status", "UP",
                "timestamp", Instant.now(),
                "checks", Map.of(
                        "application", "UP")));
    }

    @Operation(summary = "Startup probe", description = "Returns the startup status of the application")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application has started successfully",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(type = "object")))
    })
    @GetMapping("/startup")
    public Mono<Map<String, Object>> startup() {
        return Mono.just(Map.of(
                "status", "UP",
                "timestamp", Instant.now(),
                "message", "Application has started successfully"));
    }
}
