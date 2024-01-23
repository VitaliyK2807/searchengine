package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Service;
import searchengine.dto.pagesearchresponse.DataSearch;
import searchengine.dto.pagesearchresponse.PageSearchResponse;
import searchengine.utils.lemmas.LemmaFinder;
import searchengine.utils.pagesearch.*;
import searchengine.model.*;
import searchengine.repositories.IndexesRepository;
import searchengine.repositories.LemmasRepository;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PageSearchServiceImpl implements PageSearchService{
    private String query;
    private String site;
    private int offset;
    private int limit;
    private static final int PERCENT = 80;
    private final PagesRepository pagesRepository;
    private final LemmasRepository lemmasRepository;
    private final IndexesRepository indexesRepository;
    private final SitesRepository sitesRepository;

    public PageSearchServiceImpl(PagesRepository pagesRepository,
                                 LemmasRepository lemmasRepository,
                                 IndexesRepository indexesRepository,
                                 SitesRepository sitesRepository) {
        this.pagesRepository = pagesRepository;
        this.lemmasRepository = lemmasRepository;
        this.indexesRepository = indexesRepository;
        this.sitesRepository = sitesRepository;
    }

    @Override
    public PageSearchResponse pagesSearch(String query, String site, int offset, int limit) {
        this.query = query;
        this.site = site;
        this.offset = offset;
        this.limit = limit;

        return search();
    }

    private PageSearchResponse search() {
        Set<Relevance> setRelevance = new HashSet<>();
        long start = System.currentTimeMillis();
        List<Sites> sites = getSites(site);
        Map<Sites, Set<Lemmas>> lemmas = new HashMap<>();

        if (sites.isEmpty()) {
            return new PageSearchResponse(false, ("Сайты не найдены!"));
        }

        if (requestForTheSiteParameter(sites)) {
            log.error(getIndexingErrorMessage(site));
            return new PageSearchResponse(false, getIndexingErrorMessage(site));
        }

        sites.forEach(webSite -> {
            if (indexingCheck(webSite)) {
                lemmas.put(webSite, getListOfLemmasToSearch(webSite));
            } else {
                log.error(getIndexingErrorMessage(webSite.getName()));
            }
        });


        lemmas.entrySet().forEach(lemma -> {
            Set<Lemmas> lemmasSet = lemma.getValue();
            if (!lemmasSet.isEmpty()) {
                lemma.getValue().retainAll(getModifiedListOfLemmas(lemmasSet));
            }
        });

        lemmas.entrySet().forEach(lemma -> {
                    IndexMapSearch indexMapSearch = new IndexMapSearch(indexesRepository);
                    setRelevance.addAll(indexMapSearch.getIndexMapSearch(lemma.getKey(), lemmas.get(lemma.getKey())));
                }
        );

        log.info("Затрачено времени: " + (System.currentTimeMillis() - start) / 1000);

        if(setRelevance.isEmpty()) {
            return new PageSearchResponse(false, "По введенному запросу, данных не найдено!");
        }

        List<DataSearch> fynalyList = getLimitList(setRelevance);
        if (fynalyList.isEmpty()) {
            return new PageSearchResponse(false, "Ошибка формирования финального списка!");
        }

        return new PageSearchResponse(true, setRelevance.size(), fynalyList);
    }

    private List<DataSearch> getLimitList(Set<Relevance> setRelevances) {
        return setRelevances.stream()
                .sorted(Comparator.comparingDouble(Relevance::getRelativeRelevance))
                .sorted(Collections.reverseOrder())
                .toList()
                .subList(offset, getToIndex(setRelevances.size()))
                .stream()
                .map(this::getDataSearch)
                .sorted(Comparator.comparingDouble(DataSearch::getRelevance))
                .toList();
    }

    private DataSearch getDataSearch(Relevance relevance) {
        Optional<Pages> foundPage = pagesRepository.findById(relevance.getPageId());
        if (foundPage.isEmpty()) {
            log.error("Page not found in method: PageSearchServiceImpl/getDataSearch");
        }

        Pages page = foundPage.get();

        HtmlParser htmlParser = new HtmlParser(page.getContent());

        return DataSearch.builder()
                .site(relevance.getSite().getUrl())
                .siteName(relevance.getSite().getName())
                .uri(page.getPath())
                .title(htmlParser.getTitle())
                .snippet(htmlParser.getSnippets(query))
                .relevance(relevance.getRelativeRelevance())
                .build();
    }

    private Set<Lemmas> getModifiedListOfLemmas(Set<Lemmas> lemmas) {
        if (lemmas.isEmpty()) {
            log.error("Empty set: PageSearchServiceImp/getModifiedListOfLemmas");
            return new HashSet<>();
        }

        if (lemmas.size() == 1) {
            return lemmas;
        }

        double max = lemmas.stream()
                .max(Comparator.comparing(Lemmas::getFrequency))
                .get()
                .getFrequency();
        int percentageValue = (int) Math.ceil(max * PERCENT / 100);

        return lemmas.stream()
                .filter(l -> l.getFrequency() < percentageValue)
                .collect(Collectors.toSet());
    }

    private boolean indexingCheck(Sites webSite) {
        return webSite.getStatus().equals(Status.INDEXED);
    }

    private List<Sites> getSites(String site) {
        if (site == null || site.isBlank()) {
           return sitesRepository.findAll();
        }

        return getSite(site);
    }

    private List<Sites> getSite(String urlSite) {
        Sites webSite = sitesRepository.findByUrl(urlSite);
        if (webSite == null) {
            return List.of();
        }

        return List.of(webSite);
    }


    private Set<Lemmas> getListOfLemmasToSearch(Sites webSite) {
        LemmaFinder lemmaFinder = null;
        try {
            lemmaFinder = new LemmaFinder(new RussianLuceneMorphology());
        } catch (IOException e) {
            log.error("PageSearchServiceImpl/getListOfLemmasToSearch" + e.getMessage());
        }

        return lemmaFinder.getSetLemmas(query)
                            .stream()
                            .map(word -> lemmasRepository.findByLemmaAndSiteId(word, webSite))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toSet());
    }

    private Integer getToIndex(Integer setSize) {
        return setSize < 10 ? setSize : limit;
    }
    private boolean requestForTheSiteParameter(List<Sites> sitesList) {
        if (site == null || site.isEmpty()) {
            return false;
        }
        for (Sites webSite : sitesList) {
            if (webSite.getUrl().equals(site)) {
                return !webSite.getStatus().equals(Status.INDEXED);
            }
        }
        return true;
    }

    private String getIndexingErrorMessage(String webSite) {
        return "Сайт " + webSite + ", не проиндексирован!";
    }
}
