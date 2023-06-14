package searchengine.services;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexingSites.IndexingSitesResponse;
import searchengine.dto.indexingSites.IndexingStopResponse;
import searchengine.dto.siteParsing.Parsing;
import searchengine.model.Sites;
import searchengine.model.Status;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class StartSiteIndexingServiceImpl implements StartSiteIndexingService {

    private final int countProcessors = Runtime.getRuntime().availableProcessors();
    private Thread[] poolThreads;
    private int countSites = 1;
    private boolean isSiteIndexing = true;
    private Map<String, Sites> listSites = new HashMap<>();

    @Autowired
    SitesRepository sitesRepository;
    @Autowired
    PagesRepository pagesRepository;
    @Autowired
    SitesList sitesList;

    @Override
    public IndexingSitesResponse indexingStart() {

        boolean resultResponse = sitesList.getSites().isEmpty();

        if (resultResponse) {
            log.error("Missing list of sites");
            return new IndexingSitesResponse(false,
                    "Отсутствует список сайтов");
        } else if (isSiteIndexing) {
            isSiteIndexing = false;

            log.info("Website indexing started");

            runningThreads();

            return new IndexingSitesResponse(true);
        }
        log.error("Invalid request! Indexing is already running!");
        return new IndexingSitesResponse(false,
                "Индексация уже запущена!");
    }

    @Override
    public IndexingStopResponse indexingStop() {
        if (!isSiteIndexing) {
            stopThreads();
            log.info("Indexing stopped by user");
            return new IndexingStopResponse(true);
        }
        log.error("Invalid request! Indexing not running!");
        return new IndexingStopResponse(false, "Индексация не запущена!");
    }

    private void stopThreads () {
        for (int i = 0; i < sitesList.getSites().size(); i++) {
            if (poolThreads[i].isAlive()) {
                Sites sites = listSites.get(poolThreads[i].getName());
                sites.setStatus(Status.FAILED);
                sites.setLastError("Индексация остановлена пользователем");
                poolThreads[i] = null;
            }
        }
    }

    private void runningThreads () {
        List<Sites> sites = sitesRepository.findAll();
        Site site = sitesList.getSites().get(0);
        new Thread(() -> siteUpdate(isEqualsUrl(sites, site), site)).start();
//        poolThreads = new Thread[sitesList.getSites().size()];
//        log.info("Number of sites to index: " + sitesList.getSites().size());
//        int countOperations = 1;
//
//        while (countSites < sitesList.getSites().size()) {
//            if (countOperations == countProcessors) {
//                countOperations = countWorkerThreads();
//            } else {
//                Site site = sitesList.getSites().get(countSites - 1);
//                poolThreads[countSites] = new Thread(() -> siteUpdate(isEqualsUrl(sites, site), site));
//                poolThreads[countSites].setName(site.getUrl());
//                poolThreads[countSites].start();
//                countOperations++;
//                countSites++;
//            }
//        }
//        log.info("Первый цикл окончен!");
//        int countThreads = countWorkerThreads();
//        while (countThreads != 0) {
//            countThreads = countWorkerThreads();
//        }
//        log.info("Второй цикл окончен!");
        isSiteIndexing = true;
    }
    private int countWorkerThreads () {
        int count = 0;

        for (int i = 0; i < sitesList.getSites().size(); i++) {
            if (poolThreads[i].isAlive()) {
                count++;
            }
        }

        if (count < countProcessors) {
            return  countProcessors - (countProcessors - count);
        }

        return countProcessors;
    }
    private void siteUpdate (boolean siteEquals, Site site) {
        if (siteEquals) {
            Sites sitesMemory = sitesRepository.findByUrl(site.getUrl());
            sitesRepository.deleteById(sitesMemory.getId());
            sitesMemory.setStatus(Status.INDEXING);
            sitesMemory.setStatusTime(LocalDateTime.now());
            sitesRepository.save(sitesMemory);
            listSites.put(site.getUrl(), sitesMemory);
            Parsing parsing = new Parsing(sitesMemory);
                    //, sitesRepository, pagesRepository);
            parsing.startParsing();

        } else {
            Sites newSite = new Sites();
            newSite.setStatus(Status.INDEXING);
            newSite.setUrl(site.getUrl());
            newSite.setName(site.getName());
            newSite.setStatusTime(LocalDateTime.now());
            sitesRepository.save(newSite);
            listSites.put(site.getUrl(), newSite);
            Parsing parsing = new Parsing(newSite);
                    //, sitesRepository, pagesRepository);
            parsing.startParsing();

        }
    }

    private boolean isEqualsUrl (List<Sites> sites, Site site) {
        for (Sites siteFromListSites : sites) {
            if (siteFromListSites.getUrl().equals(site.getUrl())) {
                return true;
            }
        }
        return false;
    }



}
