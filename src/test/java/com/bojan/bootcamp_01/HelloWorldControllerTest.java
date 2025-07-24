package com.bojan.bootcamp_01;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(HelloWorldController.class)
public class HelloWorldControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void helloDefault() {
        webTestClient.get().uri("/hello")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("Hello, World!");
    }

    @Test
    void helloWithName() {
        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/hello").queryParam("name", "Bojan").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("Hello, Bojan!");
    }

    @Test
    void helloWithEmptyName() {
        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/hello").queryParam("name", "").build())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Parameter 'name' must not be empty");
    }
}
