package searchengine.dto.siteParsing;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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
public class Parsing extends Thread{
    private Sites site;

    long start;
    private CopyOnWriteArraySet<String> listUrls;
    private ForkJoinPool pool = new ForkJoinPool();
    private final PagesRepository pagesRepository;
    private final SitesRepository sitesRepository;
    private ParsingSite parsingSite;

    public Parsing(Sites site, SitesRepository sitesRepository, PagesRepository pagesRepository) {
        this.site = site;
        listUrls = new CopyOnWriteArraySet<>();
        this.sitesRepository = sitesRepository;
        this.pagesRepository = pagesRepository;
        if (!site.getUrl().endsWith("/")) {
            String memory = site.getUrl();
            site.setUrl(memory + "/");
        }

    }

    public void started () {
        log.info("Site " + site.getName() + " parsing start" );
        start = System.currentTimeMillis();
        parsingSite = new ParsingSite(site.getUrl(),
                site.getName(),
                listUrls,
                site,
                sitesRepository,
                pagesRepository);
        try {

            pool.invoke(parsingSite);
            sitesRepository.updateStatusById(Status.INDEXED, site.getId());
            log.info("Parsing of the site: " + site.getName() + ", completed in: " +
                    ((System.currentTimeMillis() - start) / 1000) + " s.");
            log.info("Added number of entries: " + listUrls.size() + ", for site: " + site.getName());

        } catch (NullPointerException nEx) {
            sitesRepository.updateFailed(Status.FAILED, nEx.getSuppressed().toString(), LocalDateTime.now(), site.getId());
            log.error(nEx.getSuppressed().toString());
            log.error("Parsing of the site: " + site.getName() + ", stopped in: " +
                    ((System.currentTimeMillis() - start) / 1000) + " s.");
            log.error("Added number of entries: " + listUrls.size() + ", for site: " + site.getName());
        }
    }

    @Override
    public void run() {

    }

    public void stopped () {
        parsingSite = null;
        pool.shutdown();
        pool = null;
        log.info("Parsing of the site: " + site.getName() + ", was stopped by the user after: " +
                ((System.currentTimeMillis() - start) / 1000) + " s.");
        log.info("Added number of entries: " + listUrls.size() + ", for site: " + site.getName());
        sitesRepository.updateStatusById(Status.FAILED, site.getId());
    }

    public List<String> getListPages() {
        return new ArrayList<>(listUrls);
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
