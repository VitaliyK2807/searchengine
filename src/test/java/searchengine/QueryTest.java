package searchengine;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import searchengine.utils.lemmas.LemmaFinder;
import searchengine.model.Pages;
import searchengine.repositories.PagesRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.TreeSet;


@DisplayName("Тест поиска снипетов")
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
class QueryTest  extends TestCase {
    private LemmaFinder finder;
    private static final Integer DIFFERENCE = 3;
    private String query = "найти новую книгу по программированию";
    private TreeSet<String> setLemmas;
    private String text;
    private Pages page;
    private String[] wordsContent;
    private Document document;
    @Autowired
    PagesRepository pagesRepository;
    @BeforeEach
    protected void setUp() throws Exception {
        finder = new LemmaFinder(new RussianLuceneMorphology());
        page = pagesRepository.findById(10148).get();
        document = Jsoup.parse(page.getContent());
        text = document.text();
        wordsContent = text.split("\\s+");
        setLemmas = finder.getSetLemmas(query);
    }

    @Test
    @DisplayName("Тест поиска")
    public void testSnippet() {

        ArrayList<String> lemmasInText = getModifiedArray(wordsContent);
        ArrayList<Integer> arrayIndexes = getArrayIndexes(lemmasInText, setLemmas);
        List<String> listReadyText = new ArrayList<>();


        arrayIndexes.forEach(i -> listReadyText.add(getString(i)));

        listReadyText.forEach(t -> log.info(t));
    }

    private String getString(Integer index) {
        int from = index - DIFFERENCE;
        int to = index + DIFFERENCE;

        if (testDiff(from) & testAddition(to)) {
            return redyString(0, wordsContent.length, index);
        }
        if (testDiff(from) & !testAddition(to)) {
            return redyString(0, to, index);
        }
        if (!testDiff(from) & testAddition(to)) {
            return redyString(from, wordsContent.length, index);
        }

        return redyString(from, to, index);
    }


    private boolean testDiff(Integer from) {
        if (from <= 0) {
            return true;
        }
        return false;
    }

    private boolean testAddition (Integer to) {
        if (to >= wordsContent.length) {
            return true;
        }
        return false;
    }
    private String redyString(int from, int to, int index) {
        StringJoiner joiner = new StringJoiner("\s");
        for (int i = from; i < to; i++) {
            if (i == index) {
                joiner.add("<b>" + wordsContent[i] + "</b>");
            }
            joiner.add(wordsContent[i]);
        }
        return joiner.toString();
    }
    private ArrayList<Integer> getArrayIndexes(ArrayList<String> lemmasInText, TreeSet<String> set) {
        ArrayList<Integer> arrayIndexes = new ArrayList<>();
        set.forEach(lemma -> {
            for (int i = 0; i < lemmasInText.size(); i++) {
                if (lemmasInText.get(i).equals(lemma)) {
                    arrayIndexes.add(i);
                }
            }
        });
        return arrayIndexes;
    }


    private ArrayList<String> getModifiedArray(String[] wordsContent) {
        ArrayList<String> listLemmas = new ArrayList<>();
        for (int i = 0; i < wordsContent.length; i++) {
            listLemmas.add(finder.getNormalForm(wordsContent[i]));
        }

        return listLemmas;
    }
}
