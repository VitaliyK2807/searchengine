package searchengine.dto.siteParsing;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.model.Pages;
import searchengine.model.Sites;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Data
public class ParsingSite extends RecursiveAction {
    //private CopyOnWriteArraySet<String> listUrls;
    private CopyOnWriteArraySet<IndexedPage> listPages;

    private String url;
    private String domain;
    private Sites site;
    private IndexedPage indexedPage = new IndexedPage();


//    private final PagesRepository pagesRepository;

//    private final SitesRepository sitesRepository;

    public ParsingSite(String url,
                       String domain,
                       CopyOnWriteArraySet<IndexedPage> listPages,
                       //CopyOnWriteArraySet<String> listUrls,
                       Sites site) {
//            ,
//                       SitesRepository sitesRepository,
//                       PagesRepository pagesRepository) {
        this.url = url;
        this.domain = domain;
        this.listPages = listPages;
//        this.listUrls = listUrls;
        this.site = site;
//        this.sitesRepository = sitesRepository;
//        this.pagesRepository = pagesRepository;
    }

    @Override
    protected void compute() {

        TreeSet<String> urlLinks = parsingLinksSite();
        List<ParsingSite> listTasks = new ArrayList<>();

        if (urlLinks.size() != 0) {
            urlLinks.forEach(child -> {
                ParsingSite parsingSite = new ParsingSite(child,
                                                            domain,
                                                            listPages,
                                                            site);
//                        ,
//                                                            sitesRepository,
//                                                            pagesRepository);
                parsingSite.fork();
                listTasks.add(parsingSite);
            });

        }
        listTasks.forEach(ParsingSite::join);
    }

    private TreeSet<String> parsingLinksSite() {

        TreeSet<String> childesLinks = new TreeSet<>();
        connect().forEach(element -> {
            String receivedURL = getUrls(element.attr("href"));
            if (receivedURL != ""
                    && receivedURL != null
                    && !listPages.equals(testPath(receivedURL))
                    && (receivedURL.endsWith(".html") || receivedURL.endsWith("/"))
                    && !(receivedURL.equals(getDomainUrl()) || receivedURL.equals(getDomainUrlWWW()))) {
                childesLinks.add(receivedURL);
            }
        });

        return childesLinks;
    }

    private Elements connect() {
        //if (listUrls.add(getResultPath())) {
        indexedPage.setPath(url);
        if (listPages.add(indexedPage)) {
            Pages page = new Pages();
            page.setPath(getResultPath());
            page.setSite(site);
            try {
                Document document = Jsoup.connect(url)
                        .timeout(25_000)
                        .userAgent("Chrome/109.0.5414.120 Safari/532.5")
                        .referrer("http://www.google.com")
                        .ignoreContentType(true)
                        .get();
                indexedPage.setCode(document.connection().response().statusCode());
                indexedPage.setContent(document.outerHtml());
                //page.setContent(document.outerHtml());
                //page.setCode(document.connection().response().statusCode());
                //pagesRepository.save(page);
                //sitesRepository.updateTime(LocalDateTime.now(), site.getId());
                return document.select("a");

            } catch (HttpStatusException hse) {
                log.error(hse.getMessage());
                indexedPage.setCode(hse.getStatusCode());
                indexedPage.setContent(hse.getMessage());
                //page.setCode(hse.getStatusCode());
                //page.setContent(hse.getMessage());
                //pagesRepository.save(page);
                //sitesRepository.updateTime(LocalDateTime.now(), site.getId());
                return new Elements();

            } catch (SocketTimeoutException ste) {
                log.error(ste.getMessage() + " - " + url);
                indexedPage.setCode(504);
                indexedPage.setContent(ste.getMessage());
                //page.setCode(0);
                //page.setContent(ste.getMessage());
                //pagesRepository.save(page);
                //sitesRepository.updateTime(LocalDateTime.now(), site.getId());
                return new Elements();

            } catch (Exception ex) {
                log.error("Other exceptions URL -> " + url + " " + ex.getMessage());
                indexedPage.setCode(0);
                indexedPage.setContent(ex.getMessage());
                //page.setCode(0);
                //page.setContent(ex.getMessage());
                //pagesRepository.save(page);
                //sitesRepository.updateTime(LocalDateTime.now(), site.getId());
                return new Elements();
            }
        }
        return new Elements();
    }

    private String getUrls(String str) {
        if (str.startsWith("/")) {
            return  getDomainUrl() + str;
        }

        String regex = getRegexUrl();
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
    private String getRegexUrl () {
        return "http[s]?://" + "[www.]?" + domain.toLowerCase() + "/[^,\\s\"><«»а-яА-Я]+";
    }

    private String getDomainUrl () {
        return url.substring(0, url.indexOf("/") + 2) + domain.toLowerCase();
    }
    private String getDomainUrlWWW () {
        return url.substring(0, url.indexOf("/") + 2) + "www." + domain.toLowerCase();
    }
    private String getResultPath () {
        if (url.substring(url.indexOf("/") + 2, url.indexOf("/") + 5).equals("www")) {
            return url.substring(getDomainWWW().length());
        }
        return url.substring(getDomainUrl().length());
    }

    private String getDomainWWW () {
        return url.substring(0, url.indexOf("/") + 2) + "www." + domain.toLowerCase();
    }

    private String testPath (String path) {
        if (path.substring(path.indexOf("/") + 2, path.indexOf("/") + 5).equals("www")) {
            return path.substring(getDomainWWW().length());
        }
        return path.substring(getDomainUrl().length());
    }
}