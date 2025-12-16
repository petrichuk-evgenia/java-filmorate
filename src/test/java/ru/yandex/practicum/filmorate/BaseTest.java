package ru.yandex.practicum.filmorate;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class BaseTest {
    public static ConfigurableApplicationContext context;
    public static HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    @BeforeClass
    public static void setContext() {
        context = SpringApplication.run(FilmorateApplication.class);
    }

    @AfterClass
    public static void closeContext() {
        context.close();
    }

    @BeforeMethod
    public void setUp() throws IOException, InterruptedException {
        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/films/clear"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .GET()
                .build();

        client.send(req1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users/clear"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .GET()
                .build();

        client.send(req2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    @Test
    public void contextLoads() {
        Assert.assertNotNull(context, "Контекст не проинициализирован");
    }
}
