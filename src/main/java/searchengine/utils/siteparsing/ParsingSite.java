package searchengine.utils.siteparsing;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.dao.DataIntegrityViolationException;
import searchengine.model.Pages;
import searchengine.model.Sites;
import searchengine.repositories.IndexesRepository;
import searchengine.repositories.LemmasRepository;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ParsingSite extends RecursiveAction {
    private CopyOnWriteArraySet<String> listUrls;
    private String url;
    private Sites site;
    private PagesRepository pagesRepository;
    private SitesRepository sitesRepository;
    private LemmasRepository lemmasRepository;
    private IndexesRepository indexesRepository;
    static volatile boolean fatalError;
    static volatile boolean stop;

    public ParsingSite(String url,
                       CopyOnWriteArraySet<String> listUrls,
                       Sites site) {
        this.url = url;
        this.listUrls = listUrls;
        this.site = site;
    }

    @Override
    protected void compute() {
        if (stop || fatalError) {
            return;
        }

        TreeSet<String> urlLinks = parsingLinksSite();
        List<ParsingSite> listTasks = new ArrayList<>();

        if (!urlLinks.isEmpty()) {
            urlLinks.forEach(child -> {
                ParsingSite parsingSite = new ParsingSite(child,
                        listUrls,
                        site);
                parsingSite.setSitesRepository(sitesRepository);
                parsingSite.setPagesRepository(pagesRepository);
                parsingSite.setIndexesRepository(indexesRepository);
                parsingSite.setLemmasRepository(lemmasRepository);

                parsingSite.fork();
                listTasks.add(parsingSite);
            });

        }
        listTasks.forEach(ParsingSite::join);
    }

    private TreeSet<String> parsingLinksSite() {

        TreeSet<String> childesLinks = new TreeSet<>();
        try {
            connect().forEach(element -> {
                String receivedURL = getUrls(element.attr("href"));
                if (testElements(receivedURL)) {
                    childesLinks.add(receivedURL);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return childesLinks;
    }

    private Elements connect() throws IOException {
        String path = getResultPath();
        if (listUrls.add(path)) {
            Pages page = new Pages();
            page.setPath(path);
            page.setSiteId(site);
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
                sitesRepository.updateLastErrorAndStatusTimeById("", LocalDateTime.now(), site.getId());
                writeLemma(code, page, document.text());
                return document.select("a");

            } catch (HttpStatusException hse) {
                log.error(hse.getMessage() + " - " + url);
                return new Elements();

            } catch (SocketTimeoutException ste) {
                log.error(ste.getMessage() + " - " + url);
                return new Elements();
            }
        }
        return new Elements();
    }

    private void writeLemma(int code, Pages page, String text) throws IOException {
        if (code < 400) {
            WritingLemmas writingLemmas =
                    new WritingLemmas(lemmasRepository,
                            indexesRepository,
                            page,
                            text);
            writingLemmas.writeLemmaAndIndex();
        }
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
        return !element.equals("")
                && element != null
                && !listUrls.equals(testPath(element))
                && (endsWith(element, element.endsWith(".html")) || element.endsWith("/"))
                && !(element.equals(getDomainUrl()) || element.equals(getDomainUrlWWW()));
    }

    private boolean endsWith (String element, boolean ends) {
        if (ends) {
            return true;
        }
        String regex = "[a-zA-Z-?!=%0-9]+";
        int lastSlash = element.lastIndexOf('/');
        String string = element.substring(lastSlash + 1);

        return string.matches(regex);
    }
    private String getRegexUrl() {
        return "http[s]?://" + "[www.]?" + site.getName().toLowerCase() + "/[^,\\s\"><«»а-яА-Я]+";
    }


    private String getResultPath() {
        if (url.substring(url.indexOf("/") + 2, url.indexOf("/") + 5).equals("www")) {
            return url.substring(getDomainWWW().length());
        }
        return url.substring(getDomainUrl().length());
    }

    private String getDomainWWW() {
        return url.substring(0, url.indexOf("/") + 2) + "www." + site.getName().toLowerCase();
    }

    private String testPath (String path) {
        if (path.substring(path.indexOf("/") + 2, path.indexOf("/") + 5).equals("www")) {
            return path.substring(getDomainWWW().length());
        }
        return path.substring(getDomainUrl().length());
    }

    private String getDomainUrl() {
        return url.substring(0, url.indexOf("/") + 2) + site.getName().toLowerCase();
    }

    private String getDomainUrlWWW() {
        return url.substring(0, url.indexOf("/") + 2) + "www." + site.getName().toLowerCase();
    }

    public void setPagesRepository(PagesRepository pagesRepository) {
        this.pagesRepository = pagesRepository;
    }

    public void setSitesRepository(SitesRepository sitesRepository) {
        this.sitesRepository = sitesRepository;
    }

    public void setLemmasRepository(LemmasRepository lemmasRepository) {
        this.lemmasRepository = lemmasRepository;
    }

    public void setIndexesRepository(IndexesRepository indexesRepository) {
        this.indexesRepository = indexesRepository;
    }
}