package searchengine;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import searchengine.utils.pagesearch.IndexMapSearch;
import searchengine.model.Indexes;
import searchengine.model.Lemmas;
import searchengine.model.Sites;
import searchengine.repositories.IndexesRepository;
import searchengine.repositories.LemmasRepository;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;

import java.util.*;

@DisplayName("Тест расчета релевантности")
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
class PageSearchServiceImplTest extends TestCase {
    private Set<Lemmas> lemmas;
    private List<Integer> pages;
    private List<Indexes> indexes;
    IndexMapSearch indexMapSearch;
    @Autowired
    LemmasRepository lemmasRepository;
    @Autowired
    SitesRepository sitesRepository;
    @Autowired
    PagesRepository pagesRepository;
    @Autowired
    IndexesRepository indexesRepository;
    @BeforeEach
    protected void setUp() throws Exception {
        lemmas = new HashSet<>();
        pages = new ArrayList<>();
        indexes = new ArrayList<>();
        indexMapSearch = new IndexMapSearch(indexesRepository);
        Optional<Sites> site = sitesRepository.findById(1);

        lemmas.add(lemmasRepository.findByLemmaAndSiteId("устюг", site.get()).get());
        lemmas.add(lemmasRepository.findByLemmaAndSiteId("электронный", site.get()).get());
        lemmas.add(lemmasRepository.findByLemmaAndSiteId("инженерия", site.get()).get());

        pages.add(pagesRepository.findById(12).get().getId());
        lemmas.forEach(l -> indexes.add(indexesRepository.findByPage_IdAndLemma_Id(pages.get(0), l.getId()).get()));

        Map<Integer, List<Indexes>> mapIndexes = new HashMap<>();
        mapIndexes.put(pages.get(0), indexes);
        indexMapSearch.setIndexesMapForTest(mapIndexes);
    }

    @Test
    @DisplayName("Тест расчета релевантности")
    public void testGetAbsoluteRelevanceValue() {
        Map<Integer, Double> expected = new HashMap<>();
        expected.put(pages.get(0), 1.8);
        Map<Integer, Double> actual = indexMapSearch.getMapForTestAbsoluteRelevanceValue(pages, lemmas);
        assertEquals(expected, actual);

    }
}
