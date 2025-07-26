package com.bojan.bootcamp_01;

import com.bojan.bootcamp_01.entity.HelloWorld;
import com.bojan.bootcamp_01.repository.HelloWorldRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(TestcontainersConfiguration.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class HelloWorldControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    
    @Autowired
    private HelloWorldRepository helloWorldRepository;

    @Test
    void helloDefault() {
        // When
        webTestClient.get().uri("/hello")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("Hello, World!");
                
        // Then - verify it was saved to database
        var messages = helloWorldRepository.findByNameContainingIgnoreCase("World");
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getName()).isEqualTo("World");
        assertThat(messages.get(0).getMessage()).isEqualTo("Hello, World!");
    }

    @Test
    void helloWithName() {
        // When
        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/hello").queryParam("name", "Bojan").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("Hello, Bojan!");
                
        // Then - verify it was saved to database
        var messages = helloWorldRepository.findByNameContainingIgnoreCase("Bojan");
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getName()).isEqualTo("Bojan");
        assertThat(messages.get(0).getMessage()).isEqualTo("Hello, Bojan!");
    }

    @Test
    void helloWithEmptyName() {
        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/hello").queryParam("name", "").build())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Parameter 'name' must not be empty");
    }
    
    @Test
    void getAllMessages() {
        // Given - create some test data
        HelloWorld hello1 = new HelloWorld();
        hello1.setName("Alice");
        hello1.setMessage("Hello, Alice!");
        helloWorldRepository.save(hello1);
        
        HelloWorld hello2 = new HelloWorld();
        hello2.setName("Bob");
        hello2.setMessage("Hello, Bob!");
        helloWorldRepository.save(hello2);
        
        // When & Then
        webTestClient.get().uri("/hello/messages")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(HelloWorld.class)
                .hasSize(2);
    }
    
    @Test
    void searchMessagesByName() {
        // Given
        HelloWorld hello = new HelloWorld();
        hello.setName("TestUser");
        hello.setMessage("Hello, TestUser!");
        helloWorldRepository.save(hello);
        
        // When & Then
        webTestClient.get().uri(uriBuilder -> 
                uriBuilder.path("/hello/messages/search")
                    .queryParam("name", "test")
                    .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(HelloWorld.class)
                .hasSize(1);
    }
}
