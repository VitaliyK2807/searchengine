package searchengine.dto.siteParsing;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.model.Sites;
import searchengine.model.Status;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ForkJoinPool;

@Slf4j
@Data
public class Parsing {
    private Sites site;
    private CopyOnWriteArraySet<String> listUrls;
    private CopyOnWriteArraySet<IndexedPage> listPages = new CopyOnWriteArraySet<>();

//    private final PagesRepository pagesRepository;
//    private final SitesRepository sitesRepository;

    public Parsing(Sites site) {
            //, SitesRepository sitesRepository, PagesRepository pagesRepository) {
        this.site = site;
        //listUrls = new CopyOnWriteArraySet<>();
//        this.sitesRepository = sitesRepository;
//        this.pagesRepository = pagesRepository;
//        if (!site.getUrl().endsWith("/")) {
//            String memory = site.getUrl();
//            site.setUrl(memory + "/");
//        }
    }

    public void startParsing () {
        log.info("Site " + site.getName() + " parsing start" );
        long start = System.currentTimeMillis();

        ForkJoinPool pool = new ForkJoinPool();
        ParsingSite parsingSite = new ParsingSite(site.getUrl(),
                                                    site.getName(),
                                                    listPages,
                                                    site);
//                ,
//                                                    sitesRepository,
//                                                    pagesRepository);
        try {
            pool.invoke(parsingSite);
            //sitesRepository.updateStatusById(Status.INDEXED, site.getId());
            log.info("Parsing of the site: " + site.getName() + ", completed in: " +
                    ((System.currentTimeMillis() - start) / 1000) + " s.");
            log.info("Added number of entries: " + listUrls.size() + ", for site: " + site.getName());
        } catch (NullPointerException nEx) {
            //sitesRepository.updateFailed(Status.FAILED, nEx.getSuppressed().toString(), LocalDateTime.now(), site.getId());
            log.error(nEx.getSuppressed().toString());
            log.error("Parsing of the site: " + site.getName() + ", stopped in: " +
                    ((System.currentTimeMillis() - start) / 1000) + " s.");
            log.error("Added number of entries: " + listUrls.size() + ", for site: " + site.getName());
        }
     }
     public List<IndexedPage> getListPages() {
        return new ArrayList<>(listPages);
     }

//    public List<Pages> getListIndexingPages () {
//        return listPages.stream().map(indexedPage -> {
//            Pages pages = new Pages();
//            pages.setPath(indexedPage.getPath());
//            pages.setCode(indexedPage.getCode());
//            pages.setContent(indexedPage.getContent());
//            pages.setSite(site);
//            return pages;
//        }).collect(Collectors.toList());
//    }

}
