package TestParsingSite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import searchengine.dto.siteParsing.Parsing;
import searchengine.model.Sites;
import searchengine.model.Status;
import java.time.LocalDateTime;

@DisplayName("Тестирование парсинга сайта")
public class TestParsingSite {

    private Sites site = new Sites();
    Parsing parsing;

    @BeforeEach
    protected void setup () {
        site.setId(1);
        site.setName("playback.ru");
        site.setUrl("http://www.playback.ru/");
        site.setStatus(Status.INDEXED);
        site.setLastError("");
        site.setStatusTime(LocalDateTime.now());
        parsing = new Parsing(site);
        System.out.println("Начало поиска!");
        long start = System.currentTimeMillis();

        parsing.startParsing();

        System.out.println("Затрачено времени: " + ((System.currentTimeMillis() - start) / 1000) + " сек.");
    }

    @Test
    @DisplayName("Тест количества парсинг")
    void testCountUrls() {
        System.out.println("Поиск ссылок окончен, количество ссылок: " + parsing.getListIndexingPages().size());
        parsing.getListIndexingPages().forEach(site -> System.out.println(site.getPath()));
    }

}
