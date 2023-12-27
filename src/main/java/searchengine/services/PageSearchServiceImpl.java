package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.lemmas.LemmaFinder;
import searchengine.dto.pageSearch.*;
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
    private final int PERCENT = 80;
    private Set<Relevance> setRelevance;
    private HtmlParser htmlParser;

    @Autowired
    PagesRepository pagesRepository;
    @Autowired
    LemmasRepository lemmasRepository;
    @Autowired
    IndexesRepository indexesRepository;
    @Autowired
    SitesRepository sitesRepository;
    @Override
    public PageSearchResponse pagesSearch(String query, String site, int offset, int limit) {
        this.query = query;
        this.site = site;
        this.offset = offset;
        this.limit = limit;
        htmlParser = new HtmlParser();
        return search();
    }

    private PageSearchResponse search() {
        setRelevance = new HashSet<>();
        long start = System.currentTimeMillis();
        List<Sites> sites = getSites(site);
        Map<Sites, Set<Lemmas>> lemmas = new HashMap<>();
        if (sites.isEmpty()) {
            return new PageSearchResponse(false, ("Сайты не найдены!"));
        }

        sites.forEach(webSite -> {
            if (indexingCheck(webSite)) {
                lemmas.put(webSite, getListOfLemmasToSearch(webSite));
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

        System.out.println("Затрачено времени: " + ((System.currentTimeMillis() - start)) / 1000);

        if(setRelevance.isEmpty()) {
            return new PageSearchResponse(false, "По запросу данных не найдено!");
        }

        return new PageSearchResponse(true, setRelevance.size(), getLimitList(setRelevance));
    }

    private List<DataSearch> getLimitList(Set<Relevance> setRelevances) {
        return setRelevances.stream()
                .sorted(Comparator.comparingDouble(Relevance::getRelativeRelevance))
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toList())
                .subList(offset, limit)
                .stream()
                .map(r -> getDataSearch(r))
                .sorted(Comparator.comparingDouble(DataSearch::getRelevance))
                .collect(Collectors.toList());
    }

    private DataSearch getDataSearch(Relevance relevance) {
        Pages page = pagesRepository.findById(relevance.getPageId()).get();

        return DataSearch.builder()
                .site(relevance.getSite().getUrl())
                .siteName(relevance.getSite().getName())
                .uri(page.getPath())
                .title(htmlParser.getTitle(page.getContent()))
                .snippet(htmlParser.getSnippets(query))
                .relevance(relevance.getRelativeRelevance())
                .build();
    }

    private Set<Lemmas> getModifiedListOfLemmas(Set<Lemmas> lemmas) {
        if (lemmas.size() == 1) {
            return lemmas;
        }
        int max = lemmas.stream()
                .max(Comparator.comparing(Lemmas::getFrequency))
                .get()
                .getFrequency();
        int percentageValue = (int) Math.ceil(max / 100 * PERCENT);

        return lemmas.stream()
                .filter(l -> l.getFrequency() < percentageValue)
                .collect(Collectors.toSet());
    }

    private boolean indexingCheck(Sites webSite) {
        if (webSite.getStatus().equals(Status.INDEXED)) {
            return true;
        }

        log.error("Сайт " + webSite.getName() + ", не проиндексирован!");
        return false;
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
            throw new RuntimeException(e);
        }

        return lemmaFinder.getSetLemmas(query)
                            .stream()
                            .map(word -> lemmasRepository.findByLemmaAndSiteId(word, webSite))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toSet());
    }
}
