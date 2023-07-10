package searchengine.dto.siteParsing;

import lombok.extern.slf4j.Slf4j;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.Sites;
import searchengine.model.Status;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class Parsing extends Thread{
    private SitesList sitesList;
    private long start;
    private CopyOnWriteArraySet<String> listUrls;
    private ForkJoinPool pool = new ForkJoinPool();
    private final PagesRepository pagesRepository;
    private final SitesRepository sitesRepository;

    private boolean stopFJPUser = false;
    private boolean isSiteIndexing;
    private static final int COUNT_PROCESSORS = 3;
    //Runtime.getRuntime().availableProcessors();
    private List<ParsingSite> parsingList;
    private Thread[] poolThreads;
    private int countSites = 1;

    public Parsing(SitesList sitesList, boolean isSiteIndexing, SitesRepository sitesRepository, PagesRepository pagesRepository) {
        this.sitesList = sitesList;
        listUrls = new CopyOnWriteArraySet<>();
        this.sitesRepository = sitesRepository;
        this.pagesRepository = pagesRepository;
        this.isSiteIndexing = isSiteIndexing;
    }
    private void stopped () {

        stopFJPUser = true;

        for (int i = 0; i < countSites; i++) {
            if (poolThreads[i].isAlive()) {
                parsingList.get(i).stop = true;
                poolThreads[i].interrupt();
                Sites webSite = sitesRepository.findByUrl(sitesList.getSites().get(i).getUrl());
                printMassageInfo(", was stopped by the user after: ", sitesList.getSites().get(i), start);
                sitesRepository.updateFailed(Status.FAILED,
                        "Остановлено пользователем!",
                        LocalDateTime.now(),
                        webSite.getId());
            }
        }


    }

    @Override
    public void run() {
        new Thread(() -> {
            log.info("Website indexing started");
            log.info("Number of sites to index: " + sitesList.getSites().size());

            start = System.currentTimeMillis();
            parsingList = getParsingList();
            int countThread = 0;
            int countOperations = parsingList.size();

            while (countOperations != 0 && countThread != 0) {
                if (isInterrupted()) {
                    log.info("STOP!");
                    stopped();
                    break;
                }

                if (countThread < COUNT_PROCESSORS && countOperations != 0) {
                    countThread++;
                    countOperations--;

                    startedThread(countOperations, parsingList.get(countOperations));

                } else if (countThread == COUNT_PROCESSORS) {
                    countThread = countWorkerThreads();
                }

                if (countOperations == 0) {
                    countThread = countWorkerThreads();
                }
            }
            log.info("Website indexing completed!");
            log.info("Time spent: " + ((start - System.currentTimeMillis()) / 1000) + " s.");
        }).start();
}
    private void startedThread (int numberOperations, ParsingSite parsingSite) {

        poolThreads[numberOperations] = new Thread(() -> {
            Site site = sitesList.getSites().get(numberOperations);
            Sites webSite = sitesRepository.findByUrl(site.getUrl());
            try {
                long startParsing = System.currentTimeMillis();
                log.info("Site " + site.getName() + " parsing start.");
                parsingSite.invoke();

                printMassageInfo(", completed in: ", site, startParsing);
                sitesRepository.updateStatusById(Status.INDEXED, webSite.getId());

            } catch (NullPointerException nEx) {
                log.error(nEx.getSuppressed().toString());
                printMessageError(site);

                sitesRepository.updateFailed(Status.FAILED,
                        nEx.getSuppressed().toString(),
                        LocalDateTime.now(),
                        webSite.getId());

            }
        });
        poolThreads[numberOperations].start();

    }
    private List<ParsingSite> getParsingList() {
      return sitesList.getSites().stream().map(site -> {
          Sites webSite = getSite(site);
          ParsingSite parsingSite = new ParsingSite(site.getUrl(),
                  site.getName(),
                  listUrls,
                  webSite,
                  sitesRepository,
                  pagesRepository);
          return parsingSite;
      }).collect(Collectors.toList());
    }

    private Sites getSite (Site site) {
        Sites newSite = new Sites();
        newSite.setStatus(Status.INDEXING);
        newSite.setStatusTime(LocalDateTime.now());
        newSite.setLastError("");
        newSite.setUrl(site.getUrl());
        newSite.setName(site.getName());

        sitesRepository.save(newSite);

        return newSite;
    }


    private int countWorkerThreads () {
        int count = 0;

        for (int i = 0; i < sitesList.getSites().size(); i++) {
            if (poolThreads[i].isAlive()) {
                count++;
            }
        }

        if (count < COUNT_PROCESSORS) {
            return  COUNT_PROCESSORS - (COUNT_PROCESSORS - count);
        }

        return COUNT_PROCESSORS;
    }


    private void printMassageInfo (String message, Site site, long startParsing) {
        log.info("Parsing of the site: " + site.getName() + message +
                ((System.currentTimeMillis() - startParsing) / 1000) + " s.");
        log.info("Added number of entries: " + listUrls.size() + ", for site: " + site.getName());
    }
    private void printMessageError (Site site) {
        log.error("Parsing of the site: " + site.getName() + ", stopped in: " +
                ((System.currentTimeMillis() - start) / 1000) + " s.");
        log.error("Added number of entries: " + listUrls.size() + ", for site: " + site.getName());
    }

    public List<String> getListPages() {
        return new ArrayList<>(listUrls);
    }

    public boolean isSiteIndexing() {
        return isSiteIndexing;
    }
}
