package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexingSites.IndexingSitesResponse;
import searchengine.dto.siteParsing.Parsing;
import searchengine.model.Pages;
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
            log.error("Нет конфигурационного файла или отсутствует список сайтов");
            return new IndexingSitesResponse(false,
                    "Нет конфигурационного файла или отсутствует список сайтов");
        } else if (isSiteIndexing) {
            isSiteIndexing = false;
            log.info("Индексация сайтов запущена");
            long start = System.currentTimeMillis();
            List<Sites> sites = sitesRepository.findAll();
                sitesList.getSites().forEach(site ->
                new Thread(() -> siteUpdate(isEqualsUrl(sites, site), site)).start()
            );
            log.info("Индексация сайтов закончена. " + ((System.currentTimeMillis() - start) / 1000) + " s.");
            isSiteIndexing = true;
            return new IndexingSitesResponse(true);
        }
        log.error("Не верный запрос! Индексация уже запущена!");
        return new IndexingSitesResponse(false,
                "Индексация уже запущена!");
    }

    private void siteUpdate (boolean siteEquals, Site site) {
        if (siteEquals) {
            Sites sitesMemory = sitesRepository.findByUrl(site.getUrl());
            sitesRepository.delete(sitesMemory);
            sitesMemory.setStatus(Status.INDEXING);
            sitesMemory.setStatusTime(LocalDateTime.now());
            sitesRepository.save(sitesMemory);

            Parsing parsing = new Parsing(sitesMemory);
            parsing.startParsing();
            List<Pages> pages = parsing.getListIndexingPages();
            pagesRepository.saveAll(pages);

        } else {
            Sites newSite = new Sites();
            newSite.setStatus(Status.INDEXING);
            newSite.setUrl(site.getUrl());
            newSite.setName(site.getName());
            newSite.setStatusTime(LocalDateTime.now());
            sitesRepository.save(newSite);

            Parsing parsing = new Parsing(newSite);
            parsing.startParsing();
            List<Pages> pages = parsing.getListIndexingPages();
            pagesRepository.saveAll(pages);

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
