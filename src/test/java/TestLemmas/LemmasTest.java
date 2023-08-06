package TestLemmas;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import searchengine.dto.lemmas.LemmaFinder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@DisplayName("Тест создания лемм")
public class LemmasTest {

    private LuceneMorphology luceneMorphology;

    {
        try {
            luceneMorphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private LemmaFinder lemmaFinder = new LemmaFinder(luceneMorphology);

    private String text = "Повторное появление леопарда в Осетии позволяет предположить, " +
            "что леопард постоянно обитает в некоторых районах Северного Кавказа.";
    private Map<String, Integer> Expected = new HashMap<>();

    @BeforeEach
    protected void setUp() {
        Expected.put("повторный", 1);
        Expected.put("появление", 1);
        Expected.put("постоянно", 1);
        Expected.put("позволять", 1);
        Expected.put("предположить", 1);
        Expected.put("северный", 1);
        Expected.put("район", 1);
        Expected.put("кавказ", 1);
        Expected.put("осетия", 1);
        Expected.put("леопард", 2);
        Expected.put("обитать", 1);

    }


    @Test
    @DisplayName("Тест поиск лемм")
    void testGetCollectionLemmas() {
       assertEquals(Expected, lemmaFinder.getCollectionLemmas(text));
    }

    @Test
    @DisplayName("Тест чтение файла с разнообразным текстом")
    void testFindLemmas () {
        try {
            String text = Files.readString(Path.of("testFiles/TestText.txt"));

            for (Map.Entry<String, Integer> word: lemmaFinder.getCollectionLemmas(text).entrySet()) {
                System.out.println(word.getKey() + " - " + word.getValue());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
