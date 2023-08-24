package searchengine.dto.siteParsing;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.model.Pages;
import searchengine.model.Sites;
import searchengine.model.Status;
import searchengine.repositories.IndexesRepository;
import searchengine.repositories.LemmasRepository;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ParsingSite extends RecursiveAction {
    private CopyOnWriteArraySet<String> listUrls;
    private String url;
    private String domain;
    private Sites site;
    private PagesRepository pagesRepository;
    private SitesRepository sitesRepository;
    private LemmasRepository lemmasRepository;
    private IndexesRepository indexesRepository;
    public boolean fatalError;
    static boolean stop;

    public ParsingSite(String url,
                       String domain,
                       CopyOnWriteArraySet<String> listUrls,
                       Sites site,
                       SitesRepository sitesRepository,
                       PagesRepository pagesRepository,
                       LemmasRepository lemmasRepository,
                       IndexesRepository indexesRepository) {
        this.url = url;
        this.domain = domain;
        this.listUrls = listUrls;
        this.site = site;
        this.sitesRepository = sitesRepository;
        this.pagesRepository = pagesRepository;
        this.lemmasRepository = lemmasRepository;
        this.indexesRepository = indexesRepository;

    }

    @Override
    protected void compute() {

        if (stop) {
            return;
        }

        TreeSet<String> urlLinks = parsingLinksSite();
        List<ParsingSite> listTasks = new ArrayList<>();

        if (urlLinks.size() != 0) {
                urlLinks.forEach(child -> {
                    ParsingSite parsingSite = new ParsingSite(child,
                            domain,
                            listUrls,
                            site,
                            sitesRepository,
                            pagesRepository,
                            lemmasRepository,
                            indexesRepository);
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
            if (testElements(receivedURL)) {
                childesLinks.add(receivedURL);
            }
        });

        return childesLinks;
    }

    private Elements connect() {
        String path = getResultPath();
        if (listUrls.add(path)) {
            Pages page = new Pages();
            page.setPath(path);
            page.setSite(site);
            try {
                Document document = Jsoup.connect(url)
                        .timeout(25_000)
                        .userAgent("Chrome/109.0.5414.120 Safari/532.5")
                        .referrer("http://www.google.com")
                        .ignoreContentType(true)
                        .get();
                int code = document.connection().response().statusCode();
                String documentText = document.outerHtml();
                page.setContent(documentText);
                page.setCode(code);

                page.setId(pagesRepository.save(page).getId());
                sitesRepository.updateTime(LocalDateTime.now(), site.getId());

                if (code < 399 || code > 599) {
                    WritingLemmas writingLemmas =
                            new WritingLemmas(documentText,
                            page,
                            lemmasRepository,
                            indexesRepository);
                    writingLemmas.writeLemmaAndIndex();


                }

                return document.select("a");

            } catch (HttpStatusException hse) {
                log.error(hse.getMessage());
                page.setCode(hse.getStatusCode());
                page.setContent(hse.getMessage());
                pagesRepository.save(page);
                sitesRepository.updateTime(LocalDateTime.now(), site.getId());

                return new Elements();

            } catch (SocketTimeoutException ste) {
                log.error(ste.getMessage() + " - " + url);
                page.setCode(0);
                page.setContent(ste.getMessage());
                pagesRepository.save(page);
                sitesRepository.updateTime(LocalDateTime.now(), site.getId());

                return new Elements();

           } catch (Exception ex) {
                log.error("IOException for URL -> " + url + " " + ex.getMessage());

                page.setCode(0);
                page.setContent(ex.getMessage());
                pagesRepository.save(page);
                sitesRepository.updateFailed(Status.FAILED, ex.getMessage(), LocalDateTime.now(), site.getId());
                fatalError = true;

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

    private boolean testElements (String element) {
        if (element != ""
                && element != null
                && !listUrls.equals(testPath(element))
                && (endsWith(element, element.endsWith(".html")) || element.endsWith("/"))
                && !(element.equals(getDomainUrl()) || element.equals(getDomainUrlWWW()))) {
            return true;
        }

        return false;
    }

    private boolean endsWith (String element, boolean ends) {
        if (ends) {
            return true;
        }
        String regex = "[a-zA-Z-?!=%0-9]+";
        int lastSlash = element.lastIndexOf('/');
        String string = element.substring(lastSlash + 1);

        if (string.matches(regex)) {
            return true;
        }
        return false;
    }
    private String getRegexUrl() {
        return "http[s]?://" + "[www.]?" + domain.toLowerCase() + "/[^,\\s\"><«»а-яА-Я]+";
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

    private String testPath (String path) {
        if (path.substring(path.indexOf("/") + 2, path.indexOf("/") + 5).equals("www")) {
            return path.substring(getDomainWWW().length());
        }
        return path.substring(getDomainUrl().length());
    }
    private String getDomainUrl() {
        return url.substring(0, url.indexOf("/") + 2) + domain.toLowerCase();
    }
    private String getDomainUrlWWW() {
        return url.substring(0, url.indexOf("/") + 2) + "www." + domain.toLowerCase();
    }

    public Sites getSite() {
        return site;
    }
}