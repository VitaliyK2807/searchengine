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
import java.util.Random;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ForkJoinPool;

@Slf4j
@Data
public class Parsing {
    private Sites site;
    long start;
    private CopyOnWriteArraySet<String> listUrls;
    private ForkJoinPool pool = new ForkJoinPool();
    private final PagesRepository pagesRepository;
    private final SitesRepository sitesRepository;
    private ParsingSite parsingSite;
    private boolean stopFJPUser = false;


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
        try {
            pool.invoke(parsingSite = new ParsingSite(site.getUrl(),
                    site.getName(),
                    listUrls,
                    site,
                    sitesRepository,
                    pagesRepository));

            if (stopFJPUser) {
                printMassageInfo(", was stopped by the user after: ");
                sitesRepository.updateFailed(Status.FAILED,
                        "Остановлено пользователем!",
                        LocalDateTime.now(),
                        site.getId());
            } else {
                printMassageInfo(", completed in: ");
                sitesRepository.updateStatusById(Status.INDEXED, site.getId());
            }

        } catch (NullPointerException nEx) {
            log.error(nEx.getSuppressed().toString());
            printMessageError();

            sitesRepository.updateFailed(Status.FAILED,
                    nEx.getSuppressed().toString(),
                    LocalDateTime.now(),
                    site.getId());

        }
    }

    public void stopped () {
        parsingSite.stop = true;

        stopFJPUser = true;

        pool.shutdown();

    }

    public List<String> getListPages() {
        return new ArrayList<>(listUrls);
    }

    private void printMassageInfo (String message) {
        log.info("Parsing of the site: " + site.getName() + message +
                ((System.currentTimeMillis() - start) / 1000) + " s.");
        log.info("Added number of entries: " + listUrls.size() + ", for site: " + site.getName());
    }
    private void printMessageError () {
        log.error("Parsing of the site: " + site.getName() + ", stopped in: " +
                ((System.currentTimeMillis() - start) / 1000) + " s.");
        log.error("Added number of entries: " + listUrls.size() + ", for site: " + site.getName());
    }

}
