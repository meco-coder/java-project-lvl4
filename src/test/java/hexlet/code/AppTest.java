package hexlet.code;


import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.DB;
import io.ebean.Transaction;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class AppTest {

    @Test
    void testInit() {
        assertThat(true).isEqualTo(true);
    }

    private static Javalin app;
    private static String baseUrl;
    private static Url existingUrl;
    private static Transaction transaction;

    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;

        existingUrl = new Url("https://github.com:8080");
        existingUrl.save();
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    @BeforeEach
    void beforeEach() {
        transaction = DB.beginTransaction();
    }

    @AfterEach
    void afterEach() {
        transaction.rollback();
    }

    @Test
    void testIndex() {
        HttpResponse<String> response = Unirest.get(baseUrl).asString();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains("Анализатор страниц");
    }

    @Test
    void testCreateUrl() {
        String inputName = "https://ru.hexlet.io/";
        HttpResponse<String> responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("name", inputName)
                .asEmpty();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls")
                .asString();
        String body = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body).contains("https://ru.hexlet.io:8080");
        assertThat(body).contains("Страница успешно добавлена");

        Url actualUrl = new QUrl()
                .name.equalTo("https://ru.hexlet.io:8080")
                .findOne();

        assertThat(actualUrl).isNotNull();
        assertThat(actualUrl.getName()).isEqualTo("https://ru.hexlet.io:8080");
    }


    @Test
    void testCreateUrl_negative() {
        String inputName = "hps://github.com/";
        HttpResponse<String> responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("name", inputName)
                .asEmpty();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/");

        HttpResponse<String> response = Unirest
                .get(baseUrl)
                .asString();
        String body = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body).contains("Некорректный URL");
    }

    @Test
    void testCreateUrl_negative2() {

        String inputName = "https://github.com/";

        HttpResponse<String> responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("name", inputName)
                .asEmpty();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls")
                .asString();
        String body = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body).contains("https://github.com:8080");
        assertThat(body).contains("Страница уже существует");
    }
@Test
        void testShowUrl() {
    HttpResponse<String> response = Unirest
            .get(baseUrl + "/urls/1")
            .asString();

    String content = response.getBody();

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(content).contains("https://github.com:8080");

}

}


