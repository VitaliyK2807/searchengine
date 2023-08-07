package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexPageResponse.IndexPageResponse;
import searchengine.dto.siteParsing.ParsingSite;
import searchengine.model.Sites;
import searchengine.model.Status;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArraySet;

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

        String namePage = getDomainPage(url.toLowerCase());

        if (!namePage.isEmpty()) {
            for (Site site : resultResponse) {
                if (site.getUrl().equals(namePage)) {
                    startPageUpdate(site);
                    return new IndexPageResponse(true);
                }
            }
        }

        log.error("This page is outside the sites specified in the configuration file!");
        return new IndexPageResponse(false,
                "Данная страница находится за пределами сайтов, указанных в конфигурационном файле!");
    }

    private void startPageUpdate (Site site) {
        Optional<Sites> webSite = Optional.ofNullable(sitesRepository.findByUrl(site.getUrl()));
        if (webSite.isEmpty()) {
            Sites newWebSite = newWebSite(site);
            sitesRepository.save(newWebSite);
            log.info("Added a website " + newWebSite.getName() + " entry to the database");
        } else {
            sitesRepository.updateTime(LocalDateTime.now(), webSite.get().getId());
            log.info("Updated a website " + webSite.get().getName() + " entry to the database");
        }
    }

    private Sites newWebSite (Site site) {
        Sites newWebSite = new Sites();
        newWebSite.setName(site.getName());
        newWebSite.setUrl(site.getUrl());
        newWebSite.setStatus(Status.INDEXING);
        newWebSite.setStatusTime(LocalDateTime.now());

        return newWebSite;
    }

    private String getDomainPage(String url) {

        if (url.startsWith("https") ||
                url.startsWith("http")) {
            int start = url.indexOf("/") + 2;
            int end = url.substring(start).indexOf("/") + start;

            return url.substring(0, end);
        }

        if (url.startsWith("www")) {
            int end = url.indexOf("/");

            return "https//" + url.substring(0, end);
        }

        String regex = "[a-z0-9]+";
        if (url.substring(0, url.indexOf(".")).matches(regex)) {
            return "https//www." + url.substring(0, url.indexOf("/"));
        }

        return "";
    }


}
