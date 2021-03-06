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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;


public final class AppTest {

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
        final Path fileForParse = Paths.get("src/test/resources/page-test.html").normalize()
                .toAbsolutePath();
        server.enqueue(new MockResponse().setBody(Files.readString(fileForParse)));

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
        assertThat(response.getBody()).contains("???????????????????? ??????????????");
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
        assertThat(body).contains("???????????????? ?????????????? ??????????????????");

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
        assertThat(body).contains("???????????????????????? URL");
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
        assertThat(body).contains("???????????????? ?????? ????????????????????");
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
        assertThat(body).contains("???????????????? ?????????????? ??????????????????");

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
        assertThat(body3).contains("???????????????? ?????????????? ??????????????????");
        assertThat(body3).contains("????????????????????????");
        assertThat(body3).contains("???????????????? ???????????????? ??????????.");
        assertThat(body3).contains("?????????? ?????????? ???????? ???????? ??????????????");
    }
}


