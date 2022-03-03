package hexlet.code;



import hexlet.code.domain.Url;
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

        existingUrl = new Url("https://github.com/");
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

//    void testCreate() {
//        String inputName = "https://ru.hexlet.io/";
//        HttpResponse<String> responsePost = Unirest
//                .post(baseUrl)
//                .field("name", inputName)
//                .field("description", inputDescription)
//                .asEmpty();
//
//        assertThat(responsePost.getStatus()).isEqualTo(302);
//        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/articles");
//
//        HttpResponse<String> response = Unirest
//                .get(baseUrl + "/articles")
//                .asString();
//        String body = response.getBody();
//
//        assertThat(response.getStatus()).isEqualTo(200);
//        assertThat(body).contains(inputName);
//        assertThat(body).contains("Статья успешно создана");
//
//        Article actualArticle = new QArticle()
//                .name.equalTo(inputName)
//                .findOne();
//
//        assertThat(actualArticle).isNotNull();
//        assertThat(actualArticle.getName()).isEqualTo(inputName);
//        assertThat(actualArticle.getDescription()).isEqualTo(inputDescription);
//    }

}
