package searchengine.services;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.indexingSites.IndexingSitesResponse;
import searchengine.dto.indexingSites.IndexingStopResponse;
import searchengine.dto.siteParsing.Parsing;
import searchengine.model.Sites;
import searchengine.model.Status;
import searchengine.repositories.IndexesRepository;
import searchengine.repositories.LemmasRepository;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class StartSiteIndexingServiceImpl implements StartSiteIndexingService {

    private boolean isSiteIndexing = true;
    private Parsing[] parsings;
    private static final int COUNT_PROCESSORS = 3;
            //Runtime.getRuntime().availableProcessors();
    private List<Sites> listWebSites;
    private Thread watchingThread;
    private Runnable parsingWebSites;
    @Autowired
    SitesRepository sitesRepository;
    @Autowired
    PagesRepository pagesRepository;
    @Autowired
    LemmasRepository lemmasRepository;
    @Autowired
    IndexesRepository indexesRepository;
    @Autowired
    SitesList sitesList;

    @Override
    public IndexingSitesResponse indexingStart() {

        if (sitesList.getSites().isEmpty()) {
            log.error("Missing list of sites");
            return new IndexingSitesResponse(false,
                    "Отсутствует список сайтов");
        }
        if (isSiteIndexing) {
            isSiteIndexing = false;

            pagesRepository.deleteAll();
            sitesRepository.deleteAll();
            indexesRepository.deleteAll();
            lemmasRepository.deleteAll();

            parsings = new Parsing[sitesList.getSites().size()];


            threadLoading();

            watchingThread = new Thread(parsingWebSites);
            watchingThread.start();

            return new IndexingSitesResponse(true);
        }

        log.error("Invalid request! Indexing is already running!");
        return new IndexingSitesResponse(false,
                "Индексация уже запущена!");
    }

    @Override
    public IndexingStopResponse indexingStop() {

        if (!isSiteIndexing) {
            log.info("Indexing stopped by user");

            watchingThread.interrupt();

            isSiteIndexing = true;

            return new IndexingStopResponse(true);
        }
        log.error("Invalid request! Indexing not running!");
        return new IndexingStopResponse(false, "Индексация не запущена!");
    }

    private void threadLoading () {
        parsingWebSites = new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                int countThread = 0;
                int countOperations = 0;

                log.info("Website indexing started");
                log.info("Number of sites to index: " + sitesList.getSites().size());

                listWebSites = getSites();

                while (countOperations != listWebSites.size() || countThread != 0) {
                    if (watchingThread.isInterrupted()) {
                        stopped(countOperations);
                        break;
                    }

                    if (countThread < COUNT_PROCESSORS && countOperations < listWebSites.size()) {
                        startedThread(countOperations, listWebSites.get(countOperations));
                        countThread++;
                        countOperations++;
                    }

                    if (countThread == COUNT_PROCESSORS) {
                        countThread = countWorkerThreads(countThread);
                    }

                    if (countOperations == listWebSites.size()) {
                        countThread = countWorkerThreads(countThread);
                    }
                }

                log.info("Website indexing completed!");
                log.info("Time spent: " + ((System.currentTimeMillis() - start) / 1000) + " s.");
            }
        };
    }
    private void startedThread (int numberOperations, Sites webSite) {
        parsings[numberOperations] = new Parsing(webSite,
                sitesRepository,
                pagesRepository,
                lemmasRepository,
                indexesRepository);
        parsings[numberOperations].start();
    }

    private void stopped (int countOperations) {
        for (int i = 0; i < countOperations; i++) {
            if (parsings[i].isRun) {
                parsings[i].stopped();
            }
        }
    }

    private List<Sites> getSites() {
        return sitesList.getSites()
                .stream()
                .map(site -> {
                    Sites newSite = new Sites();
                    newSite.setStatus(Status.INDEXING);
                    newSite.setStatusTime(LocalDateTime.now());
                    newSite.setLastError("");
                    newSite.setUrl(site.getUrl());
                    newSite.setName(site.getName());

                    return sitesRepository.save(newSite);})
                .collect(Collectors.toList());
    }


    private int countWorkerThreads (int countThreads) {
        int count = 0;

        for (int i = 0; i < countThreads; i++) {
            if (parsings[i].isAlive()) {
                count++;

            }
        }

        if (count < COUNT_PROCESSORS) {
            return  COUNT_PROCESSORS - (COUNT_PROCESSORS - count);
        }

        return COUNT_PROCESSORS;
    }

}
