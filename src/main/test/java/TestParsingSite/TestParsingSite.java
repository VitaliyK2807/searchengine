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
        site.setName("PlayBack.Ru");
        site.setUrl("https://www.playback.ru");
        site.setStatus(Status.INDEXED);
        site.setLastError("");
        site.setStatusTime(LocalDateTime.now());

        //parsing = new Parsing(site);
        parsing.startParsing();


    }

    @Test
    @DisplayName("Тест количества парсинг")
    void testCountUrls() {
        System.out.println("Количество ссылок в листе ссылок: " + parsing.getListPages().size());
        parsing.getListPages().forEach(System.out::println);
    }

}
