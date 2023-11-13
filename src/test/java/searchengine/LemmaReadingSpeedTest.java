package searchengine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import searchengine.model.Lemmas;
import searchengine.repositories.LemmasRepository;
import searchengine.repositories.SitesRepository;

import java.util.*;
import java.util.stream.Collectors;

@DisplayName("Тест скорости чтения лемм")
@RunWith(SpringRunner.class)
@SpringBootTest
class LemmaReadingSpeedTest {

    private List<Lemmas> allLemmas;
    private Map<String, Integer> mapLemmas;

    private String maximumRepeatableWord;
    private String minimumRepeatableWord;

    @Autowired
    SitesRepository sitesRepository;

    @Autowired
    LemmasRepository lemmasRepository;

    @BeforeEach
    protected void setUp() {
        mapLemmas = new HashMap<>();

        long fullReadingTime = System.currentTimeMillis();

        allLemmas = lemmasRepository.findAll();

        System.out.println(System.currentTimeMillis() - fullReadingTime);

        System.out.println("Общее количество лемм: " + allLemmas.size());
        System.out.println("Время затраченное на чтение общего списка: " + fullReadingTime + " мс.");

        List<String> listWords = getListWords();

        maximumRepeatableWord = listWords.get(0);
        minimumRepeatableWord = listWords.get(listWords.size() - 1);

        System.out.println("Максимально повторяющаяся лемма по всем сайтам: " + maximumRepeatableWord);
        System.out.println("Минимально повторяющеаяя лемма по всем сайтам: " + minimumRepeatableWord);

        System.out.println("----------------|-------------------------|--------------------------------");
    }

    @Test
    @DisplayName("Тест времени поиска леммы, путем чтения всей таблицы Леммы")
    void testOptionFirst () {
        System.out.println("Старт поиска со словом, которое максимально встречается на большинстве сайтах");
        List<Lemmas> lemmas;

        long startTime = System.currentTimeMillis();
        lemmas = lemmasRepository.findAll();
        lemmas.removeIf(l -> !l.getLemma().equals(maximumRepeatableWord));
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("Затрачено на выполнения поиска: " + (System.currentTimeMillis() - startTime) + " мс.");
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("Количество найденных записей в таблице Леммы: " + lemmas.size());
        System.out.println("----------------|-------------------------|--------------------------------");

        System.out.println("Старт поиска со словом, которое минимально встречается на большинстве сайтах");
        lemmas = new ArrayList<>();
        startTime = System.currentTimeMillis();
        lemmas = lemmasRepository.findAll();
        lemmas.removeIf(l -> !l.getLemma().equals(minimumRepeatableWord));
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("Затрачено на выполнения поиска: " + (System.currentTimeMillis() - startTime) + " мс.");
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("Количество найденных записей в таблице Леммы: " + lemmas.size());
    }

    @Test
    @DisplayName("Тест времени поиска леммы, путем запроса в таблице Леммы")
    void testOptionTwo () {
        System.out.println("Старт поиска со словом, которое максимально встречается на большинстве сайтах");
        List<Lemmas> lemmas;
        long startTime = System.currentTimeMillis();
        lemmas = lemmasRepository.findByLemma(maximumRepeatableWord);
        System.out.println("Затрачено на выполнения поиска: " + (System.currentTimeMillis() - startTime) + " мс.");
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("Количество найденных записей в таблице Леммы: " + lemmas.size());
        System.out.println("----------------|-------------------------|--------------------------------");

        System.out.println("Старт поиска со словом, которое минимально встречается на большинстве сайтах");
        lemmas = new ArrayList<>();
        startTime = System.currentTimeMillis();
        lemmas = lemmasRepository.findByLemma(minimumRepeatableWord);
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("Затрачено на выполнения поиска: " + (System.currentTimeMillis() - startTime) + " мс.");
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("Количество найденных записей в таблице Леммы: " + lemmas.size());
    }

    private List<String> getListWords() {
        allLemmas.sort(Comparator.comparing(Lemmas::getLemma));

        allLemmas.forEach(l -> {
            if (mapLemmas.containsKey(l.getLemma())) {
                mapLemmas.put(l.getLemma(), mapLemmas.get(l.getLemma()) + 1);
            } else {
                mapLemmas.put(l.getLemma(), 1);
            }
        });

        return mapLemmas.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
