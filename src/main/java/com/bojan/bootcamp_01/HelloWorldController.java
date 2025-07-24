package com.bojan.bootcamp_01;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {

    @GetMapping("/hello")
    public org.springframework.http.ResponseEntity<String> hello(
            @org.springframework.web.bind.annotation.RequestParam(name = "name", required = false) String name) {
        if (name != null && name.isEmpty()) {
            return org.springframework.http.ResponseEntity.badRequest().body("Parameter 'name' must not be empty");
        }
        if (name == null) {
            name = "World";
        }
        return org.springframework.http.ResponseEntity.ok("Hello, " + name + "!");
    }
}
