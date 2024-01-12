package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.indexingsites.IndexingSitesResponse;
import searchengine.dto.indexingsites.IndexingStopResponse;
import searchengine.utils.siteparsing.Parsing;
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
    private static final int COUNT_PROCESSORS = Runtime.getRuntime().availableProcessors();
    private List<Sites> listWebSites;
    private Thread watchingThread;
    private Runnable parsingWebSites;
    private final SitesRepository sitesRepository;
    private final PagesRepository pagesRepository;
    private final LemmasRepository lemmasRepository;
    private final IndexesRepository indexesRepository;
    private final SitesList sitesList;

    public StartSiteIndexingServiceImpl(SitesRepository sitesRepository,
                                        PagesRepository pagesRepository,
                                        LemmasRepository lemmasRepository,
                                        IndexesRepository indexesRepository,
                                        SitesList sitesList) {
        this.sitesRepository = sitesRepository;
        this.pagesRepository = pagesRepository;
        this.lemmasRepository = lemmasRepository;
        this.indexesRepository = indexesRepository;
        this.sitesList = sitesList;
    }

    @Override
    public IndexingSitesResponse indexingStart() {

        if (sitesList.getSites().isEmpty()) {
            log.error("Missing list of sites");
            return new IndexingSitesResponse(false,
                    "Отсутствует список сайтов");
        }
        if (isSiteIndexing) {
            isSiteIndexing = false;

            deleteAllEntries();

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

            stopped(parsings.length);

            return new IndexingStopResponse(true);
        }
        log.error("Invalid request! Indexing not running!");
        return new IndexingStopResponse(false, "Индексация не запущена!");
    }

    private void threadLoading () {
        parsingWebSites = () -> {
            long start = System.currentTimeMillis();

            log.info("Website indexing started");
            log.info("Number of sites to index: " + sitesList.getSites().size());

            listWebSites = getSites();
            cycleStart();

            log.info("Website indexing completed!");
            log.info("Time spent: " + ((System.currentTimeMillis() - start) / 1000) + " s.");
        };
    }

    private void cycleStart() {
        int countThread = 0;
        int countOperations = 0;
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
            if (parsings[i].isRun()) {
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
                .toList();
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
    private void deleteAllEntries() {
        log.info("Deleting all entries");
        indexesRepository.deleteAllInBatch();
        lemmasRepository.deleteAllInBatch();
        pagesRepository.deleteAllInBatch();
        sitesRepository.deleteAllInBatch();
    }


}
