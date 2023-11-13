package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.PropertySource;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.lemmas.LemmaFinder;
import searchengine.dto.pageSearch.PageSearchResponse;
import searchengine.model.ComparatorByLemma;
import searchengine.model.Lemmas;
import searchengine.repositories.LemmasRepository;
import searchengine.repositories.PagesRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PageSearchServiceImpl implements PageSearchService{
    private String query;
    private String site;
    private int offset;
    private int limit;
    private List<String> lemmasForSearch;
    private List<Lemmas> lemmas;

    @Autowired
    PagesRepository pagesRepository;
    @Autowired
    LemmasRepository lemmasRepository;
    @Override
    public PageSearchResponse pageSearch(String query, String site, int offset, int limit) {
        this.query = query;
        this.site = site;
        this.offset = offset;
        this.limit = limit;

        return null;
    }

    private void lemmasListUpdate() {
        lemmas.removeIf(l -> lemmasForSearch.stream().anyMatch(w -> !w.equals(l.getLemma())));
    }


    private List<String> getListOfLemmasToSearch() {
        LemmaFinder lemmaFinder;
        try {
            lemmaFinder = new LemmaFinder(new RussianLuceneMorphology());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return lemmaFinder.getCollectionLemmas(query)
                            .entrySet()
                            .stream()
                            .map(key -> key.getKey())
                            .collect(Collectors.toList());
    }


}
