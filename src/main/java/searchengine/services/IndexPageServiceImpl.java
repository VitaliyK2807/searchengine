package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexPageResponse.IndexPageResponse;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;

import java.util.List;

@Service
@Slf4j
public class IndexPageServiceImpl implements IndexPageService {

    @Autowired
    SitesRepository sitesRepository;
    @Autowired
    PagesRepository pagesRepository;
    @Autowired
    SitesList sitesList;

    @Override
    public IndexPageResponse indexPageStart(String url) {
        List<Site> resultResponse = sitesList.getSites();

        if (resultResponse.isEmpty()) {
            log.error("Missing list of sites!");
            return new IndexPageResponse(false,
                    "Отсутствует список сайтов!");
        }

        String namePage = getDomainPage(url);

        if (!namePage.isEmpty()) {
            log.info(namePage);
            return new IndexPageResponse(true);
        }

        log.error("This page is outside the sites specified in the configuration file!");
        return new IndexPageResponse(false,
                "Данная страница находится за пределами сайтов, указанных в конфигурационном файле!");
    }

    private String getDomainPage(String url) {

        if (url.toLowerCase().startsWith("https")) {
            return url.substring(0, url.indexOf("d"));
        }
        return ""; //Доработать
    }


}
