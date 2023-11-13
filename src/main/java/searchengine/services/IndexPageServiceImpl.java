package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexPageResponse.IndexPageResponse;
import searchengine.dto.lemmas.LemmaFinder;
import searchengine.dto.lemmas.PageReading;
import searchengine.model.*;
import searchengine.repositories.IndexesRepository;
import searchengine.repositories.LemmasRepository;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class IndexPageServiceImpl implements IndexPageService {

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
                    return startReadPage(site, url.toLowerCase());
                }
            }
        }

        log.error("This page is outside the sites specified in the configuration file!");
        return new IndexPageResponse(false,
                "Данная страница находится за пределами сайтов, указанных в конфигурационном файле!");
    }

    private IndexPageResponse startReadPage (Site site, String url){
        Sites webSite = getModelWebSite(site);
        LemmaFinder finder;
        Pages page;

        try {
            finder = new LemmaFinder(new RussianLuceneMorphology());
            page = getPage(url, webSite);

            sitesRepository.updateStatusById(Status.INDEXED, LocalDateTime.now(), webSite.getId());

            finder.getCollectionLemmas(page.getContent())
                    .entrySet()
                    .forEach(word -> {
                        Lemmas lemma = saveLemmas(word.getKey(), webSite);
                        saveIndex(lemma, page, word.getValue());
                    });

        } catch (RuntimeException ex) {
            sitesRepository.updateFailed(Status.FAILED,
                    "Проблема чтения страницы!",
                    LocalDateTime.now(),
                    webSite.getId());
            log.error("Page indexing: RuntimeException for URL -> " + site.getUrl() + " " + ex.getMessage());

            return new IndexPageResponse(false,
                    "Проблема чтения страницы!");
        } catch (IOException IOEx) {
            log.error(IOEx.getMessage());

            return new IndexPageResponse(false,
                    "Внутренняя ошибка!");
        }

        pagesRepository.save(page);

        return new IndexPageResponse(true);
    }
    private void saveIndex (Lemmas lemma, Pages page, float rank) {
        Indexes index = new Indexes();
        index.setPage(page);
        index.setLemma(lemma);
        index.setRank(rank);
        indexesRepository.save(index);
    }

    private Lemmas saveLemmas (String word, Sites site) {

        Optional<Lemmas> lemma = lemmasRepository.findByLemmaAndIdSite(word, site.getId());

        if (lemma.isEmpty()) {
            Lemmas newLemma = new Lemmas();
            newLemma.setLemma(word);
            newLemma.setFrequency(1);
            newLemma.setSite(site);

            return lemmasRepository.save(newLemma);
        }

        lemmasRepository.updateLemma(lemma.get().getFrequency() + 1, lemma.get());

        return lemma.get();
    }
    private Pages getPage(String url, Sites webSite) throws RuntimeException {
        PageReading pageReading = new PageReading(url, webSite.getName());
        Pages newPage = pageReading.readPage();
        newPage.setSite(webSite);

        if (newPage.getPath().isEmpty()) {
            throw new RuntimeException();

        }

        Optional<Pages> page = pagesRepository.getPageByPath(newPage.getPath());

        if (page.isEmpty()) {
            return pagesRepository.save(newPage);
        }

        deleteRecords(page.get());

        return pagesRepository.save(newPage);
    }

    private void deleteRecords (Pages page) {
        indexesRepository.findIndexesByIdPage(page.getId()).forEach(index -> deletingRecordsInTableLemma(index));
        pagesRepository.delete(page);
        indexesRepository.DeleteIndexesByPage(page.getId());
    }

    private void deletingRecordsInTableLemma (Indexes index) {
        Lemmas lemma = lemmasRepository.findById(index.getId()).get();
        lemma.setFrequency(lemma.getFrequency() - 1);

        if (lemma.getFrequency() == 0) {
            lemmasRepository.deleteLemmaById(index.getLemma());
        } else {
            lemmasRepository.updateLemma(lemma.getFrequency(), lemma);
        }
    }
    private Sites getModelWebSite(Site site) {
        Optional<Sites> webSite = Optional.ofNullable(sitesRepository.findByUrl(site.getUrl()));

        if (webSite.isEmpty()) {
            Sites newWebSite = newWebSite(site);
            log.info("Added a website " + newWebSite.getName() + " entry to the database");

            return sitesRepository.save(newWebSite);
        }

        sitesRepository.updateTime(LocalDateTime.now(), webSite.get().getId());
        log.info("Updated a website " + webSite.get().getName() + " entry to the database");

        return webSite.get();
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
