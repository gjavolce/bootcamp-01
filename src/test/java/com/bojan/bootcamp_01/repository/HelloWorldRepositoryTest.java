package com.bojan.bootcamp_01.repository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import com.bojan.bootcamp_01.TestcontainersConfiguration;
import com.bojan.bootcamp_01.entity.HelloWorld;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
class HelloWorldRepositoryTest {

    @Autowired
    private HelloWorldRepository helloWorldRepository;

    @Test
    void shouldSaveAndFindHelloWorldMessage() {
        // Given
        HelloWorld helloWorld = new HelloWorld();
        helloWorld.setName("John");
        helloWorld.setMessage("Hello, John!");

        // When
        HelloWorld saved = helloWorldRepository.save(helloWorld);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();

        List<HelloWorld> all = helloWorldRepository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getName()).isEqualTo("John");
        assertThat(all.get(0).getMessage()).isEqualTo("Hello, John!");
    }

    @Test
    void shouldFindMessagesByNameContaining() {
        // Given
        HelloWorld hello1 = new HelloWorld();
        hello1.setName("John");
        hello1.setMessage("Hello, John!");
        helloWorldRepository.save(hello1);

        HelloWorld hello2 = new HelloWorld();
        hello2.setName("Johnny");
        hello2.setMessage("Hello, Johnny!");
        helloWorldRepository.save(hello2);

        HelloWorld hello3 = new HelloWorld();
        hello3.setName("Jane");
        hello3.setMessage("Hello, Jane!");
        helloWorldRepository.save(hello3);

        // When
        List<HelloWorld> johnMessages = helloWorldRepository.findByNameContainingIgnoreCase("john");

        // Then
        assertThat(johnMessages).hasSize(2);
        assertThat(johnMessages)
                .extracting(HelloWorld::getName)
                .containsExactlyInAnyOrder("John", "Johnny");
    }

    @Test
    void shouldFindFirstByNameIgnoreCase() {
        // Given
        HelloWorld hello = new HelloWorld();
        hello.setName("World");
        hello.setMessage("Hello, World!");
        helloWorldRepository.save(hello);

        // When
        var found = helloWorldRepository.findFirstByNameIgnoreCase("WORLD");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("World");
    }
}
