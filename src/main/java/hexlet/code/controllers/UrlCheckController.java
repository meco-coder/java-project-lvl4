package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import io.javalin.http.Handler;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class UrlCheckController {


    public static Handler checkUrl = ctx -> {

        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        final HttpResponse<String> response;
        try {
            response = Unirest.get(url.getName())
                    .asString();
        } catch (Throwable e) {
            ctx.sessionAttribute("flashError", e.getMessage());
            ctx.redirect("/urls/" + id);
            return;
        }

        int status = response.getStatus();

        Document doc = Jsoup.parse(response.getBody());
        String title = doc.title();

        String description = "";
        if (doc.select("meta[name=description]").size() != 0) {
            description = doc.select("meta[name=description]")
                    .first()
                    .attr("content");
        }

        String h1 = "";
        if (doc.select("h1").size() != 0) {
            h1 = doc.select("h1")
                    .first()
                    .text();
        }

        UrlCheck urlCheck = new UrlCheck(status, title, h1, description, url);
        urlCheck.save();

        ctx.sessionAttribute("flash", "Страница успешно проверена");
        ctx.redirect("/urls/" + id);

    };
}
