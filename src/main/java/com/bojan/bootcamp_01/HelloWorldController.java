package com.bojan.bootcamp_01;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bojan.bootcamp_01.entity.HelloWorld;
import com.bojan.bootcamp_01.repository.HelloWorldRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "HelloWorld", description = "HelloWorld management API for greeting messages")
public class HelloWorldController {

    private final HelloWorldRepository helloWorldRepository;

    @Operation(summary = "Generate a greeting message", description = "Creates and stores a personalized greeting message")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Greeting generated successfully",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
        @ApiResponse(responseCode = "400", description = "Invalid parameter: name cannot be empty", content = @Content)
    })
    @GetMapping("/hello")
    public ResponseEntity<String> hello(
            @Parameter(description = "Name to greet. If not provided, defaults to 'World'", example = "John")
            @RequestParam(name = "name", required = false) String name) {
        if (name != null && name.isEmpty()) {
            return ResponseEntity.badRequest().body("Parameter 'name' must not be empty");
        }
        if (name == null) {
            name = "World";
        }
        
        // Create a greeting message
        String greeting = "Hello, " + name + "!";
        
        // Save to database
        HelloWorld helloWorld = new HelloWorld();
        helloWorld.setName(name);
        helloWorld.setMessage(greeting);
        helloWorldRepository.save(helloWorld);
        
        return ResponseEntity.ok(greeting);
    }
    
    @Operation(summary = "Get all stored messages", description = "Retrieves all greeting messages stored in the database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all messages",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = HelloWorld.class)))
    })
    @GetMapping("/hello/messages")
    public List<HelloWorld> getAllMessages() {
        return helloWorldRepository.findAll();
    }
    
    @Operation(summary = "Get recent messages", description = "Retrieves the 10 most recent greeting messages")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved recent messages",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = HelloWorld.class)))
    })
    @GetMapping("/hello/messages/recent")
    public List<HelloWorld> getRecentMessages() {
        return helloWorldRepository.findTop10ByOrderByCreatedAtDesc();
    }
    
    @Operation(summary = "Search messages by name", description = "Searches for greeting messages containing the specified name (case-insensitive)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = HelloWorld.class)))
    })
    @GetMapping("/hello/messages/search")
    public List<HelloWorld> searchMessagesByName(
            @Parameter(description = "Name to search for in messages", required = true, example = "John")
            @RequestParam String name) {
        return helloWorldRepository.findByNameContainingIgnoreCase(name);
    }
    
    @Operation(summary = "Delete a message", description = "Deletes a greeting message by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Message deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Message not found", content = @Content)
    })
    @DeleteMapping("/hello/messages/{id}")
    public ResponseEntity<Void> deleteMessage(
            @Parameter(description = "ID of the message to delete", required = true)
            @PathVariable Long id) {
        if (helloWorldRepository.existsById(id)) {
            helloWorldRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
