package searchengine.services;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexingSites.IndexingSitesResponse;
import searchengine.dto.siteParsing.Parsing;
import searchengine.model.Sites;
import searchengine.model.Status;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;
import java.time.LocalDateTime;
import java.util.List;


@Service
@Slf4j
public class StartSiteIndexingServiceImpl implements StartSiteIndexingService {

    private final int countProcessors = Runtime.getRuntime().availableProcessors();
    private final Thread[] poolThreads = new Thread[countProcessors];

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
            log.error("Missing config file or missing list of sites");
            return new IndexingSitesResponse(false,
                    "Нет конфигурационного файла или отсутствует список сайтов");
        } else if (isSiteIndexing) {
            isSiteIndexing = false;
            log.info("Website indexing started");
            List<Sites> sites = sitesRepository.findAll();
                sitesList.getSites().forEach(site ->
                 new Thread(() -> siteUpdate(isEqualsUrl(sites, site), site)
            ).start());
            isSiteIndexing = true;
            return new IndexingSitesResponse(true);
        }
        log.error("Invalid request! Indexing is already running!");
        return new IndexingSitesResponse(false,
                "Индексация уже запущена!");
    }

    private void siteUpdate (boolean siteEquals, Site site) {
        if (siteEquals) {
            Sites sitesMemory = sitesRepository.findByUrl(site.getUrl());
            sitesRepository.deleteById(sitesMemory.getId());

            sitesMemory.setStatus(Status.INDEXING);
            sitesMemory.setStatusTime(LocalDateTime.now());
            sitesRepository.save(sitesMemory);

            Parsing parsing = new Parsing(sitesMemory, sitesRepository, pagesRepository);
            parsing.startParsing();

        } else {
            Sites newSite = new Sites();
            newSite.setStatus(Status.INDEXING);
            newSite.setUrl(site.getUrl());
            newSite.setName(site.getName());
            newSite.setStatusTime(LocalDateTime.now());
            sitesRepository.save(newSite);

            Parsing parsing = new Parsing(newSite, sitesRepository, pagesRepository);
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
