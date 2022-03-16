package hexlet.code;


import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.DB;
import io.ebean.Transaction;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;


public class AppTest {

    @Test
    void testInit() {
        assertThat(true).isEqualTo(true);
    }

    private static Javalin app;
    private static String baseUrl;
    private static String urlMockServer;
    private static Transaction transaction;
    private static MockWebServer server;

    @BeforeAll
    public static void beforeAll() throws IOException {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;

        server = new MockWebServer();
        server.enqueue(new MockResponse().setBody("<!DOCTYPE HTML>\n"
                + "<html>\n"
                + " <head>\n"
                + "  <meta charset=\"utf-8\">\n"
                + "  <title>Тестирование</title>\n"
                + " <meta name=\"description\" content=\"Описание страницы сайта.\"/>\n"
                + " </head>\n"
                + " <body>\n"
                + "\n"
                + "  <h1>Здесь могла быть ваша реклама</h1>\n"
                + "\n"
                + " </body>\n"
                + "</html>")
        );

        server.start();

        URL url = new URL(server.url("/").toString());
        urlMockServer = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();


        Url existingUrl = new Url("https://github.com");
        existingUrl.save();


    }

    @AfterAll
    public static void afterAll() throws IOException {
        app.stop();
        server.shutdown();
    }

    /**
     * @since OpenJDK version 17.0.1
     */
    @BeforeEach
    void beforeEach() {
        transaction = DB.beginTransaction();
    }

    /**
     * @since OpenJDK version 17.0.1
     */
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
        HttpResponse responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", inputName)
                .asEmpty();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls")
                .asString();
        String body = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body).contains("https://ru.hexlet.io");
        assertThat(body).contains("Страница успешно добавлена");

        Url actualUrl = new QUrl()
                .name.equalTo("https://ru.hexlet.io")
                .findOne();

        assertThat(actualUrl).isNotNull();
        assertThat(actualUrl.getName()).isEqualTo("https://ru.hexlet.io");
    }


    @Test
    void testCreateUrlNegative() {
        String inputName = "hps://github.com/";
        HttpResponse responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", inputName)
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
    void testCreateUrlNegative2() {

        String inputName = "https://github.com/";

        HttpResponse responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", inputName)
                .asEmpty();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls")
                .asString();
        String body = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body).contains("https://github.com");
        assertThat(body).contains("Страница уже существует");
    }

    @Test
    void testShowUrl() {
        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls/1")
                .asString();

        String content = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(content).contains("https://github.com");

    }

    @Test
    void testCheckUrl() {

        String inputName = urlMockServer;
        HttpResponse responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", inputName)
                .asEmpty();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls")
                .asString();
        String body = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body).contains(urlMockServer);
        assertThat(body).contains("Страница успешно добавлена");

        Url actualUrl = new QUrl()
                .name.equalTo(urlMockServer)
                .findOne();

        assertThat(actualUrl).isNotNull();
        assertThat(actualUrl.getName()).isEqualTo(urlMockServer);

        HttpResponse<String> response2 = Unirest
                .post(baseUrl + "/urls/2/checks")
                .asString();

        assertThat(response2.getStatus()).isEqualTo(302);
        assertThat(response2.getHeaders().getFirst("Location")).isEqualTo("/urls/2");

        HttpResponse<String> response3 = Unirest
                .get(baseUrl + "/urls/2")
                .asString();

        String body3 = response3.getBody();

        assertThat(response3.getStatus()).isEqualTo(200);
        assertThat(body3).contains("Страница успешно проверена");
        assertThat(body3).contains("Тестирование");
        assertThat(body3).contains("Описание страницы сайта.");
        assertThat(body3).contains("Здесь могла быть ваша реклама");
    }
}


