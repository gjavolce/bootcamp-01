package com.bojan.bootcamp_01;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bojan.bootcamp_01.repository.HelloWorldRepository;

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

    @Autowired
    private HelloWorldRepository helloWorldRepository;

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

    @Operation(summary = "Database connectivity check", description = "Checks the connectivity to the PostgreSQL database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Database is accessible",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(type = "object"))),
        @ApiResponse(responseCode = "503", description = "Database is not accessible",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(type = "object")))
    })
    @GetMapping("/database")
    public Mono<ResponseEntity<Map<String, Object>>> databaseHealth() {
        return Mono.fromCallable(() -> {
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", Instant.now());
            
            try {
                // Test database connectivity by performing a simple count query
                long messageCount = helloWorldRepository.count();
                
                response.put("status", "UP");
                response.put("database", Map.of(
                    "status", "UP",
                    "type", "PostgreSQL",
                    "connectionStatus", "CONNECTED",
                    "messageCount", messageCount,
                    "details", "Database is accessible and responsive"
                ));
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                response.put("status", "DOWN");
                response.put("database", Map.of(
                    "status", "DOWN",
                    "type", "PostgreSQL", 
                    "connectionStatus", "FAILED",
                    "error", e.getMessage(),
                    "details", "Database connection failed"
                ));
                
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }
        });
    }
}
