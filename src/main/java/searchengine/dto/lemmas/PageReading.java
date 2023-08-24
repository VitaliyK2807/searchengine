package searchengine.dto.lemmas;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.model.Pages;

import java.io.IOException;
@Slf4j
public class PageReading {
    private String url;
    private String domain;

    public PageReading(String url, String domain) throws RuntimeException {
        this.domain = domain;
        this.url = url;
    }

    public Pages readPage () {
        Pages newPage = new Pages();
        try {
            Document page = Jsoup.connect(url)
                    .timeout(25_000)
                    .userAgent("Chrome/109.0.5414.120 Safari/532.5")
                    .referrer("http://www.google.com")
                    .ignoreContentType(true)
                    .get();


            newPage.setPath(getResultPath());
            newPage.setContent(page.outerHtml());
            newPage.setCode(page.connection().response().statusCode());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return newPage;
    }

    private String getResultPath() {
        if (url.substring(url.indexOf("/") + 2, url.indexOf("/") + 5).equals("www")) {
            return url.substring(getDomainWWW().length());
        }
        return url.substring(getDomainUrl().length());
    }

    private String getDomainWWW() {
        return url.substring(0, url.indexOf("/") + 2) + "www." + domain.toLowerCase();
    }

    private String getDomainUrl() {
        return url.substring(0, url.indexOf("/") + 2) + domain.toLowerCase();
    }


}
