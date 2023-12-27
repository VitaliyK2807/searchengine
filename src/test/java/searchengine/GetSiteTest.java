package searchengine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import searchengine.model.Sites;
import searchengine.repositories.SitesRepository;

@DisplayName("Тест получения сущности сайта")
@RunWith(SpringRunner.class)
@SpringBootTest
class GetSiteTest {

    @Autowired
    SitesRepository sitesRepository;

    @Test
    @DisplayName("Тест")
    void testGetSite () {
        Sites site = sitesRepository.findByUrl("jklsdfklsd");
        if(site == null) {
            System.out.println("Сайт не найден!");
        } else {
            System.out.println(site.getName());
        }

    }
}
