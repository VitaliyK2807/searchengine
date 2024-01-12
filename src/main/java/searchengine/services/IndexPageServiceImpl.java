package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexpageresponse.IndexPageResponse;
import searchengine.utils.lemmas.LemmaFinder;
import searchengine.utils.lemmas.PageReading;
import searchengine.model.*;
import searchengine.repositories.IndexesRepository;
import searchengine.repositories.LemmasRepository;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class IndexPageServiceImpl implements IndexPageService {

    private final SitesRepository sitesRepository;
    private final PagesRepository pagesRepository;
    private final LemmasRepository lemmasRepository;
    private final IndexesRepository indexesRepository;
    private final SitesList sitesList;

    public IndexPageServiceImpl(SitesRepository sitesRepository,
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
        long start = System.currentTimeMillis();
        Sites webSite = getModelWebSite(site);
        LemmaFinder finder;
        Pages page;

        try {
            finder = new LemmaFinder(new RussianLuceneMorphology());
            page = getPage(url, webSite);

            updateOrCreateRecord(page, finder);

        } catch (RuntimeException ex) {
            log.error("Page indexing: RuntimeException for URL -> " + site.getUrl() + " " + ex.getMessage());

            return new IndexPageResponse(false,
                    "Проблема чтения страницы!");
        } catch (IOException iOEx) {
            log.error(iOEx.getMessage());

            return new IndexPageResponse(false,
                    "Внутренняя ошибка!");
        }

        pagesRepository.save(page);
        sitesRepository.updateStatusById(Status.INDEXED, LocalDateTime.now(), webSite.getId());
        log.info("Time spent: " + (System.currentTimeMillis() - start) + " s.");
        log.info("Add/update page completed");
        return new IndexPageResponse(true);
    }

    private void updateOrCreateRecord(Pages page, LemmaFinder finder) {
        if (page.getId() == 0) {
            createRecord(pagesRepository.save(page), finder);
        } else {
            updateRecord(page, finder);
        }
    }

    private void updateRecord(Pages page, LemmaFinder finder) {
        Optional<List<Indexes>> indexes = indexesRepository.findByPageId(page.getId());
        if (indexes.isPresent()) {
            indexesRepository.deleteAll(indexes.get());
            indexes.get().stream()
                    .map(index -> lemmasRepository.findById(index.getId()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(lemma -> {
                        if (lemma.getFrequency() <= 1) {
                            lemmasRepository.deleteById(lemma.getId());
                        } else {
                            lemmasRepository.updateLemma(lemma.getFrequency() - 1, lemma.getId());
                        }
                    });
            createRecord(page, finder);
        }
    }

    private void createRecord(Pages page, LemmaFinder finder) {
        List<Lemmas> lemmasList = new ArrayList<>();
        List<Indexes> indexesList = new ArrayList<>();
        List<Lemmas> lemmas = new ArrayList<>();
        finder.getCollectionLemmas(page.getContent())
                .entrySet()
                .forEach(word -> {
                    Lemmas lemma = getLemma(word.getKey(), page.getSiteId());
                    if (lemma.getId() == 0) {
                        lemmasList.add(lemma);
                    }
                    indexesList.add(getIndex(word.getValue(), lemma, page));
                });
        lemmas.addAll(lemmasRepository.saveAll(lemmasList));
        lemmas.stream().forEach(lemma -> indexesList.forEach(index -> {
            if (index.getLemma().getLemma().equals(lemma.getLemma())) {
                index.getLemma().setId(lemma.getId());
            }
        }));
        indexesRepository.saveAll(indexesList);
    }

    private Indexes getIndex(int rank, Lemmas lemma, Pages page) {
        return Indexes.builder()
                .lemma(lemma)
                .rank(rank)
                .page(page)
                .build();
    }

    private Lemmas getLemma(String word, Sites site) {
        Optional<Lemmas> lemma = lemmasRepository.findByLemmaAndSiteId(word, site);

        if (!lemma.isPresent()) {

            return Lemmas.builder()
                    .lemma(word)
                    .frequency(1)
                    .siteId(site)
                    .build();
        }
        lemmasRepository.updateLemma(lemma.get().getFrequency() + 1, lemma.get().getId());

        return lemma.get();
    }


    private Pages getPage(String url, Sites webSite) throws RuntimeException, IOException {
        Optional<Pages> page = pagesRepository.getPageByPath(getResultPath(url));

        if (!page.isPresent()) {
            PageReading pageReading = new PageReading(url, webSite.getName());
            Pages newPage = pageReading.readPage();
            newPage.setSiteId(webSite);
            return newPage;
        }
        return page.get();
    }


    private Sites getModelWebSite(Site site) {
        Optional<Sites> webSite = Optional.ofNullable(sitesRepository.findByUrl(site.getUrl()));

        if (webSite.isEmpty()) {
            Sites newWebSite = newWebSite(site);
            log.info("Added a website " + newWebSite.getName() + " entry to the database");

            return sitesRepository.save(newWebSite);
        }

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

            return getPathWithAbbreviation(url.substring(0, end));
        }

        if (url.startsWith("www")) {
            int end = url.indexOf("/");

            return "https://" + url.substring(0, end);
        }

        String regex = "[a-z0-9]+";
        if (url.substring(0, url.indexOf(".")).matches(regex)) {
            return "https://www." + url.substring(0, url.indexOf("/"));
        }

        return "";
    }

    private String getPathWithAbbreviation(String url) {
        int start = url.indexOf("/") + 2;
        if (url.substring(start).startsWith("www")) {
            return url;
        }
        return "https://www." + url.substring(start);
    }

    private String getResultPath(String url) {
        int start = url.indexOf("/") + 2;
        int end = url.substring(start).indexOf("/") + start;
        return url.substring(end);
    }

}
