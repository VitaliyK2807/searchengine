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


@Service
@Slf4j
public class StartSiteIndexingServiceImpl implements StartSiteIndexingService {

    private Parsing parsing;
    private boolean isSiteIndexing = true;

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

            pagesRepository.deleteAll();
            sitesRepository.deleteAll();

            parsing = new Parsing(sitesList, isSiteIndexing, sitesRepository, pagesRepository);
            parsing.run();

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

            parsing.interrupt();

            isSiteIndexing = true;

            return new IndexingStopResponse(true);
        }
        log.error("Invalid request! Indexing not running!");
        return new IndexingStopResponse(isSiteIndexing, "Индексация не запущена!");
    }

    private void stopThreads () {

//        for (int i = 0; i < sitesList.getSites().size(); i++) {
//            if (poolThreads[i].isAlive()) {
//                Sites sites = listSites.get(poolThreads[i].getName());
//                sites.setStatus(Status.FAILED);
//                sites.setLastError("Индексация остановлена пользователем");
//                poolThreads[i] = null;
//            }
//        }
    }

    private void runningThreads () {


    }

    private void siteUpdate (Site site) {
        Sites newSite = new Sites();

        newSite.setStatus(Status.INDEXING);
        newSite.setUrl(site.getUrl());
        newSite.setName(site.getName());
        newSite.setStatusTime(LocalDateTime.now());

        sitesRepository.save(newSite);

parsing.run();
    }

}
