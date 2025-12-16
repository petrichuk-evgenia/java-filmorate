package ru.yandex.practicum.filmorate;

import io.restassured.http.ContentType;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utils.RestUtils;

import java.util.HashMap;
import java.util.Map;

public class BaseTest {
    protected static ConfigurableApplicationContext context;
    protected static String baseUrl = "http://localhost:8080";
    protected static Map<String, String> headers = new HashMap<>();

    @BeforeClass
    public static void setContext() {
        RestUtils.setRestAssuredConfigHttp();
        context = SpringApplication.run(FilmorateApplication.class);
        headers.put("Content-Type", "application/json; charset=UTF-8");
    }

    @AfterClass
    public static void closeContext() {
        context.close();
    }

    @BeforeMethod
    public void setUp() {
        RestUtils.get(getUrl("/films/clear"), ContentType.JSON.toString());
        RestUtils.get(getUrl("/users/clear"), ContentType.JSON.toString());
    }

    @Test
    public void contextLoads() {
        Assert.assertNotNull(context, "Контекст не проинициализирован");
    }

    public String getUrl(String endpoint) {
        return baseUrl + endpoint;
    }
}
