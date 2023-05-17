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
            return resultResponse(0,0,0, false);
        } else {
            List<Sites> sites = sitesRepository.findAll();
            sitesList.getSites().forEach(site ->
                siteUpdate(isEqualsUrl(sites, site), site)
            );

        }
        return resultResponse(1,1,1, true);
    }

    private void siteUpdate (boolean siteEquals, Site site) {
        if (siteEquals) {
            Sites sitesMemory = sitesRepository.findByUrl(site.getUrl());
            sitesRepository.delete(sitesMemory);
            pagesRepository.deleteBySite(sitesMemory);
            sitesMemory.setStatus(Status.INDEXING);
            sitesMemory.setStatusTime(LocalDateTime.now());
            sitesRepository.save(sitesMemory);
            Parsing parsing = new Parsing(sitesMemory);
            parsing.startParsing();
            log.debug("true");
        } else {
            Sites newSite = new Sites();
            newSite.setStatus(Status.INDEXING);
            newSite.setUrl(site.getUrl());
            newSite.setName(site.getName());
            newSite.setStatusTime(LocalDateTime.now());
            sitesRepository.save(newSite);
            Parsing parsing = new Parsing(newSite);
            parsing.startParsing();
            log.debug("false");
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

    private IndexingSitesResponse resultResponse (int countSites,
                                                  int countPages,
                                                  int countErrors,
                                                  boolean result) {
        IndexingSitesResponse sitesResponse = new IndexingSitesResponse();
        sitesResponse.setCountSitesIndexing(countSites);
        sitesResponse.setCountPagesIndexing(countPages);
        sitesResponse.setCountErrors(countErrors);
        sitesResponse.setResult(result);
        return sitesResponse;
    }
}
