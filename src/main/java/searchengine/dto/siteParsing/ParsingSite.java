package searchengine.dto.siteParsing;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ParsingSite extends RecursiveAction {

    private CopyOnWriteArraySet<IndexedPage> linksMap;
    private IndexedPage page;
    private String url;
    private String regex;

    public ParsingSite(String url,
                       CopyOnWriteArraySet<IndexedPage> linksMap,
                       String regex) {
        this.url = url;
        this.linksMap = linksMap;
        this.regex = regex;
        page = new IndexedPage();
    }

    @Override
    protected void compute() {

        List<String> urlLinks = parsingLinksSite(url);

        if (urlLinks.size() != 0) {

            List<ParsingSite> listTasks = new ArrayList<>();

            urlLinks.forEach(child -> {
                ParsingSite parsingSite = new ParsingSite(child, linksMap, regex);
                parsingSite.fork();
                listTasks.add(parsingSite);
            });
            listTasks.forEach(ParsingSite::join);
        }
    }

    private List<String> parsingLinksSite(String path) {

        List<String> childesLinks = new ArrayList<>();
        connect(path).forEach(element -> {

            String receivedURL = getUrls(String.valueOf(element), regex);

            if (receivedURL != ""
                    && !childesLinks.equals(receivedURL)
                    && !linksMap.contains(receivedURL)
                    && receivedURL.endsWith("/")
                    && receivedURL != null) {
                childesLinks.add(receivedURL);
            }
        });
        return childesLinks;
    }

    private Elements connect(String path) {
        page.setPath(path);
        if (linksMap.add(page)) {
            try {
                Document document = Jsoup.connect(path)
                        .timeout(10_000)
                        .userAgent("Chrome/109.0.5414.120 Safari/532.5")
                        .ignoreContentType(true)
                        .get();

                page.setContent(document.outerHtml());
                page.setCode(document.connection().response().statusCode());

                return document.select("[href]").select("a");
            } catch (IOException sTE) {
                log.error("IOException URL -> " + sTE.toString());
            } catch (Exception ex) {
                log.error("Other exceptions URL -> " + path + " " + ex.getMessage());
            }
        }
        return new Elements();
    }

    private String getUrls(String str, String regex) {
        String result = "";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            result = str.substring(start, end);
        }
        return result;
    }


}