package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;

import io.javalin.http.Handler;

import java.net.URL;
import java.util.List;


public class UrlsController {

    private static Boolean UrlValidator(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static Boolean CheckUrls(String url) {
        List<Url> urlList = new QUrl()
                .orderBy()
                .id.asc()
                .findList();
        for (Url urlCheck : urlList) {
            if (urlCheck.getName().equals(url)) {
                return true;
            }
        }
        return false;
    }

    private static String BuildUrl(URL url) {
        if (url.getPort() == -1) {
            return "https://" + url.getHost() + ":8080";
        }
        return "https://" + url.getHost() + ":" + url.getPort();
    }


    public static Handler listUrls = ctx -> {

        List<Url> urlSt = new QUrl()
                .orderBy()
                .id.asc()
                .findList();

        ctx.attribute("urls", urlSt);
        ctx.render("urls/index.html");
    };

    public static Handler createUrl = ctx -> {

        String name = ctx.formParam("name");

        if (UrlValidator(name)) {
            URL url = new URL(name);
            String buildUrl = BuildUrl(url);
            if (CheckUrls(buildUrl)) {
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.redirect("/urls");
                return;
            }
            Url urls = new Url(buildUrl);
            urls.save();
            ctx.sessionAttribute("flash", "Страница успешно добавлена");
            ctx.redirect("/urls");
        } else {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.redirect("/");
        }

    };


    public static Handler showUrl = ctx -> {

        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        ctx.attribute("url", url);
        ctx.render("urls/show.html");


    };
}
