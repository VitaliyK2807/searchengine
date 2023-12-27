package searchengine;

import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import searchengine.dto.lemmas.LemmaFinder;
import searchengine.dto.siteParsing.AssemblyLemma;
import searchengine.dto.siteParsing.ReadWriteLemmasAndIndexes;
import searchengine.model.Indexes;
import searchengine.model.Lemmas;
import searchengine.model.Pages;
import searchengine.model.Sites;
import searchengine.repositories.IndexesRepository;
import searchengine.repositories.LemmasRepository;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@DisplayName("Тест сохранения лемм")
@SpringBootTest
public class LemmasSaveTest {
    private ConcurrentHashMap<String, Integer> mapLemmas;
    Sites site;
    private TreeSet<String> lemmas;
    private LemmaFinder finder;
    @Autowired
    PagesRepository pagesRepository;
    @Autowired
    LemmasRepository lemmasRepository;
    @Autowired
    IndexesRepository indexesRepository;
    @Autowired
    SitesRepository sitesRepository;

    @BeforeEach
    protected void setUp() {
        mapLemmas = new ConcurrentHashMap<>();
        site = sitesRepository.findById(1).get();

    }
    @Test
    @DisplayName("Тест сохранения лемм")
    public void testLemmasSave() {

        long start = System.currentTimeMillis();

        ReadWriteLemmasAndIndexes readAndWrite =
                new ReadWriteLemmasAndIndexes(mapLemmas, pagesRepository, 1, site);
        ForkJoinPool forkJoinPool =new ForkJoinPool();
        forkJoinPool.invoke(readAndWrite);

        System.out.println("------------------|------------------------------|------------------");
        System.out.println("Общее количество найденных страниц с леммами: " + mapLemmas.size());
        System.out.println("------------------|------------------------------|------------------");
        System.out.println("Затраченное время на сохранение: " + (System.currentTimeMillis() - start) + " мс.");
        System.out.println("------------------|------------------------------|------------------");
    }
    private void findLemma(Sites site, Integer index) throws IOException {


//        System.out.println(finder.getCollectionLemmas(page.get().getContent()).size());
//        System.out.println(finder.getSetLemmas(page.get().getContent()).size());
    }


    private Lemmas saveLemmas(String word, Integer countLemmas, Sites site) {
        Optional<Lemmas> lemma = lemmasRepository.findByLemmaAndSiteId(word, site);

        if (lemma.isEmpty()) {
            Lemmas newLemma = new Lemmas();
            newLemma.setLemma(word);
            newLemma.setFrequency(1);
            newLemma.setId(site.getId());
            return lemmasRepository.save(newLemma);

        }

        lemmasRepository.updateLemma(lemma.get().getFrequency() + 1, lemma.get().getId());

        return lemma.get();
    }

    private void saveIndex (Lemmas lemma, Pages page, float rank) {
        Indexes index = new Indexes();
        index.setPage(page);
        index.setLemma(lemma);
        index.setRank(rank);
        indexesRepository.save(index);
    }

}
