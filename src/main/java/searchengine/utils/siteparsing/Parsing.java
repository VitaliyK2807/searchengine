package searchengine.utils.siteparsing;

import lombok.extern.slf4j.Slf4j;
import searchengine.model.*;
import searchengine.repositories.IndexesRepository;
import searchengine.repositories.LemmasRepository;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;
import java.time.LocalDateTime;
import java.util.concurrent.*;

@Slf4j
public class Parsing extends Thread{
    private Sites webSite;
    private ParsingSite parsingSite;
    private boolean stopFJPUser = false;
    private ForkJoinPool forkJoinPool;
    private boolean isRun;
    private CopyOnWriteArraySet<String> listUrls;
    private PagesRepository pagesRepository;
    private SitesRepository sitesRepository;
    private LemmasRepository lemmasRepository;
    private IndexesRepository indexesRepository;

    public Parsing(Sites webSite,
                   SitesRepository sitesRepository,
                   PagesRepository pagesRepository,
                   LemmasRepository lemmasRepository,
                   IndexesRepository indexesRepository) {
        this.webSite = webSite;
        this.sitesRepository = sitesRepository;
        this.pagesRepository = pagesRepository;
        this.lemmasRepository = lemmasRepository;
        this.indexesRepository = indexesRepository;
        listUrls = new CopyOnWriteArraySet<>();
        isRun = false;
    }

    @Override
    public void run() {

        isRun = true;

        long startParsing = System.currentTimeMillis();

        log.info("WebSite " + webSite.getName() + " parsing start.");

        if (!webSite.getUrl().endsWith("/")) {
            webSite.setUrl(webSite.getUrl() + "/");
        }
        parsingSite = new ParsingSite(webSite.getUrl(),
                listUrls,
                webSite);
        parsingSite.setSitesRepository(sitesRepository);
        parsingSite.setPagesRepository(pagesRepository);
        parsingSite.setIndexesRepository(indexesRepository);
        parsingSite.setLemmasRepository(lemmasRepository);

        forkJoinPool = new ForkJoinPool();

        try {
            forkJoinPool.invoke(parsingSite);

            if (stopFJPUser) {
                printMassageInfo(", was stopped by the user after: ", startParsing);
                sitesRepository.updateFailed(Status.INDEXED,
                        "Остановлено пользователем!",
                        LocalDateTime.now(),
                        webSite.getId());

            } else {
                printMassageInfo(", completed in: ", startParsing);
                sitesRepository.updateStatusById(Status.INDEXED, LocalDateTime.now(), webSite.getId());
            }
            isRun = false;

        } catch (RuntimeException e) {
            printMessageError(startParsing);
            sitesRepository.updateFailed(Status.FAILED, e.getMessage(), LocalDateTime.now(), webSite.getId());
        }

    }

    public void stopped () {

        forkJoinPool.shutdown();

        parsingSite.stop = true;

        stopFJPUser = true;

    }

    private void printMassageInfo (String message, long startParsing) {
        log.info("Parsing of the site: " + webSite.getName() + message +
                ((System.currentTimeMillis() - startParsing) / 1000) + " s.");
        log.info("Added number of entries: " + listUrls.size() + ", for site: " + webSite.getName());
    }
    private void printMessageError (long startParsing) {
        log.error("Parsing of the site: " + webSite.getName() + ", stopped in: " +
                ((System.currentTimeMillis() - startParsing) / 1000) + " s.");
        log.error("Added number of entries: " + listUrls.size() + ", for site: " + webSite.getName());
    }

    public boolean isRun() {
        return isRun;
    }

}
