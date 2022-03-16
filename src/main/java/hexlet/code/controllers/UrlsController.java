package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;

import io.ebean.PagedList;
import io.javalin.http.Handler;


import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class UrlsController {

    private static Boolean urlValidator(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static Boolean checkUrlIsEmpty(String url) {

        List<Url> urlList = new QUrl()
                .orderBy()
                .id.asc()
                .findList();
        if (urlList.isEmpty()) {
            return false;
        }

        for (Url urlCheck : urlList) {
            if (urlCheck.getName().equals(url)) {
                return true;
            }
        }
        return false;
    }

    private static String buildUrl(URL url) {
        if (url.getPort() == -1) {

            return url.getProtocol() + "://" + url.getHost();
        }
        return url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
    }


    public static Handler pagedListUrls = ctx -> {

        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;
        int rowsPerPage = 10;

        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(page * rowsPerPage)
                .setMaxRows(rowsPerPage)
                .orderBy()
                .id.asc()
                .findPagedList();

        List<Url> urls = pagedUrls.getList();

        int lastPage = pagedUrls.getTotalPageCount() + 1;
        int currentPage = pagedUrls.getPageIndex() + 1;

        List<Integer> pages = IntStream
                .range(1, lastPage)
                .boxed()
                .collect(Collectors.toList());


        ctx.attribute("urls", urls);
        ctx.attribute("pages", pages);
        ctx.attribute("currentPage", currentPage);
        ctx.render("urls/index.html");
    };

    public static Handler createUrl = ctx -> {

        String nameUrl = ctx.formParam("url");

        if (urlValidator(nameUrl)) {
            URL url = new URL(nameUrl);
            String buildUrl = buildUrl(url);
            if (checkUrlIsEmpty(buildUrl)) {
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.redirect("/urls");
                return;
            }
            Url urls = new Url(buildUrl);
            urls.save();
            ctx.sessionAttribute("flash", "Страница успешно добавлена");
            ctx.redirect("/urls");
        } else {
            ctx.sessionAttribute("flashError", "Некорректный URL");
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
