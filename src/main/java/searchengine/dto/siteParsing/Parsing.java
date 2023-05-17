package searchengine.dto.siteParsing;

import lombok.extern.slf4j.Slf4j;
import searchengine.model.Sites;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
//@Slf4j
public class Parsing {

    //static Logger logger = LogManager.getLogger(Parsing.class);
    private Sites site;
    private CopyOnWriteArraySet<IndexedPage> listUrls;
    private CopyOnWriteArraySet<String> list;
    String regex;

    public Parsing(Sites site) {
        this.site = site;
        listUrls = new CopyOnWriteArraySet<>();
        list = new CopyOnWriteArraySet<>();
        regex = "http[s]?://" + getDomain(site.getUrl()) + ".[a-z]+/[^,\\s\"><«»а-яА-Я]+";

    }

    public void startParsing () {
        //log.error("Start site parsing: " + site.getUrl());
        ParsingSite parsingSite = new ParsingSite(site.getUrl(), listUrls, regex);
        //new ForkJoinPool().invoke(parsingSite);
    }

    public List<String> getUrls () {
        return new ArrayList<>(list);
    }

    public List<IndexedPage> getListIndexingPages () {
        return new ArrayList<>(listUrls);
    }
    private String getDomain(String path) {
        int start = path.indexOf("/");
        if (path.substring(start + 2, start + 3).toLowerCase().equals("w")) {
            start = path.indexOf(".");
            int end = path.indexOf(".", start + 2);
            return path.substring(start + 1, end);
        }
        int end = path.indexOf(".", start + 2);
        return path.substring(start + 2, end);
    }

}
