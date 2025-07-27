package com.bojan.bootcamp_01;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Application health check endpoints")
public class HealthController {

    @Operation(summary = "Liveness probe", description = "Returns the liveness status of the application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application is alive", content = @Content(mediaType = "application/json", schema = @Schema(type = "object")))
    })
    @GetMapping("/live")
    public Map<String, Object> liveness() {
        return Map.of(
                "status", "UP",
                "timestamp", Instant.now(),
                "checks", Map.of(
                        "application", "UP"));
    }

    @Operation(summary = "Startup probe", description = "Returns the startup status of the application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application has started successfully", content = @Content(mediaType = "application/json", schema = @Schema(type = "object")))
    })
    @GetMapping("/startup")
    public Map<String, Object> startup() {
        return Map.of(
                "status", "UP",
                "timestamp", Instant.now(),
                "message", "Application has started successfully");
    }

    // OpenAPI annotations removed due to invalid location error
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> databaseHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", Instant.now());
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            if (result != null && result == 1) {
                response.put("status", "UP");
                response.put("database", Map.of(
                        "status", "UP",
                        "type", "PostgreSQL",
                        "connectionStatus", "CONNECTED",
                        "details", "Database is accessible and responsive"));
                return ResponseEntity.ok(response);
            } else {
                throw new RuntimeException("Unexpected DB result");
            }
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("database", Map.of(
                    "status", "DOWN",
                    "type", "PostgreSQL",
                    "connectionStatus", "FAILED",
                    "error", e.getMessage(),
                    "details", "Database connection failed"));
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }
}
