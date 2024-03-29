package searchengine;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import searchengine.model.Lemmas;
import searchengine.repositories.LemmasRepository;

import java.util.*;
import java.util.stream.Collectors;

@DisplayName("Тест скорости чтения лемм")
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
class LemmaReadingSpeedTest {

    private List<Lemmas> allLemmas;
    private Map<String, Integer> mapLemmas;

    private String maximumRepeatableWord;
    private String minimumRepeatableWord;

    @Autowired
    LemmasRepository lemmasRepository;

    @BeforeEach
    protected void setUp() {
        mapLemmas = new HashMap<>();

        long fullReadingTime = System.currentTimeMillis();

        allLemmas = lemmasRepository.findAll();

        log.info(String.valueOf(System.currentTimeMillis() - fullReadingTime));

        log.info("Общее количество лемм: " + allLemmas.size());
        log.info("Время затраченное на чтение общего списка: " + fullReadingTime + " мс.");

        List<String> listWords = getListWords();

        maximumRepeatableWord = listWords.get(0);
        minimumRepeatableWord = listWords.get(listWords.size() - 1);

        log.info("Максимально повторяющаяся лемма по всем сайтам: " + maximumRepeatableWord);
        log.info("Минимально повторяющеаяя лемма по всем сайтам: " + minimumRepeatableWord);

        log.info("----------------|-------------------------|--------------------------------");
    }

    @Test
    @DisplayName("Тест времени поиска леммы, путем чтения всей таблицы Леммы")
    void testOptionFirst () {
        System.out.println("Старт поиска со словом, которое максимально встречается на большинстве сайтах");
        List<Lemmas> lemmas;

        long startTime = System.currentTimeMillis();
        lemmas = lemmasRepository.findAll();
        lemmas.removeIf(l -> !l.getLemma().equals(maximumRepeatableWord));
        log.info("--------------------------------------------------------------------------");
        log.info("Затрачено на выполнения поиска: " + (System.currentTimeMillis() - startTime) + " мс.");
        log.info("--------------------------------------------------------------------------");
        log.info("Количество найденных записей в таблице Леммы: " + lemmas.size());
        log.info("----------------|-------------------------|--------------------------------");

        log.info("Старт поиска со словом, которое минимально встречается на большинстве сайтах");
        startTime = System.currentTimeMillis();
        lemmas = lemmasRepository.findAll();
        lemmas.removeIf(l -> !l.getLemma().equals(minimumRepeatableWord));
        log.info("--------------------------------------------------------------------------");
        log.info("Затрачено на выполнения поиска: " + (System.currentTimeMillis() - startTime) + " мс.");
        log.info("--------------------------------------------------------------------------");
        log.info("Количество найденных записей в таблице Леммы: " + lemmas.size());
    }

    @Test
    @DisplayName("Тест времени поиска леммы, путем запроса в таблице Леммы")
    void testOptionTwo () {
        log.info("Старт поиска со словом, которое максимально встречается на большинстве сайтах");
        List<Lemmas> lemmas;
        long startTime = System.currentTimeMillis();
        lemmas = lemmasRepository.findByLemma(maximumRepeatableWord);
        log.info("Затрачено на выполнения поиска: " + (System.currentTimeMillis() - startTime) + " мс.");
        log.info("--------------------------------------------------------------------------");
        log.info("Количество найденных записей в таблице Леммы: " + lemmas.size());
        log.info("----------------|-------------------------|--------------------------------");

        log.info("Старт поиска со словом, которое минимально встречается на большинстве сайтах");
        lemmas = new ArrayList<>();
        startTime = System.currentTimeMillis();
        lemmas = lemmasRepository.findByLemma(minimumRepeatableWord);
        log.info("--------------------------------------------------------------------------");
        log.info("Затрачено на выполнения поиска: " + (System.currentTimeMillis() - startTime) + " мс.");
        log.info("--------------------------------------------------------------------------");
        log.info("Количество найденных записей в таблице Леммы: " + lemmas.size());
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
