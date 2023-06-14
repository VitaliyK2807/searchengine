package StatisticTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.Lemmas;
import searchengine.model.Pages;
import searchengine.model.Sites;
import searchengine.model.Status;
import searchengine.repositories.LemmasRepository;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;
import searchengine.services.StatisticsService;
import searchengine.services.StatisticsServiceImpl;
import java.time.LocalDateTime;

import java.util.Random;

@DisplayName("Тестирование статистики по сайтам")
@SpringBootTest(classes = StatisticsTest.class)
public class StatisticsTest {

    private String[] statuses = { "INDEXED", "FAILED", "INDEXING" };
    private String[] errors = {
            "Ошибка индексации: главная страница сайта не доступна",
            "Ошибка индексации: сайт не доступен",
            ""
            };
    @Autowired
    private SitesRepository sitesRepository;
    @Autowired
    private PagesRepository pagesRepository;
    @Autowired
    private LemmasRepository lemmasRepository;

    @Test
    void testGetStatistics() {
        addSites();
        StatisticsService statisticsService = new StatisticsServiceImpl();
        StatisticsResponse response = statisticsService.getStatistics();
        System.out.println(response.toString());
    }

    private void addSites () {
        Sites siteGoogle = new Sites();
        siteGoogle.setName("Google");
        siteGoogle.setStatus(Status.INDEXING);
        siteGoogle.setUrl("https://Google.com");
        siteGoogle.setLastError(errors[(new Random().nextInt(10) + 1) % 3]);
        siteGoogle.setStatusTime(LocalDateTime.now());

        Pages pagesGoogleSearch = new Pages();
        pagesGoogleSearch.setPath("/search");
        pagesGoogleSearch.setSite(siteGoogle);
        pagesGoogleSearch.setCode(200);
        pagesGoogleSearch.setContent("<html itemscope=\"\" itemtype=\"http://schema.org/SearchResultsPage\" " +
                "lang=\"ru\"><head><meta charset=\"UTF-8\"><meta content=\"origin\" name=\"referrer\">" +
                "<meta content=\"/images/branding/googleg/1x/googleg_standard_color_128dp.png\"");

        Pages pagesGooglePolicies = new Pages();
        pagesGooglePolicies.setPath("/terms");
        pagesGooglePolicies.setSite(siteGoogle);
        pagesGooglePolicies.setCode(201);
        pagesGooglePolicies.setContent("<!doctype html><html lang=\"ru\" dir=\"ltr\"><head><base href=" +
                "\"https://policies.google.com/\"><meta name=\"referrer\" content=\"origin\"><meta name=" +
                "\"viewport\" content=\"initial-scale=1, maximum-scale=5, width=device-width\"><meta name=" +
                "\"mobile-web-app-capable\" content=\"yes\"><meta name=\"apple-mobile-web-app-capable\" " +
                "content=\"yes\"><meta name=\"application-name\" content=\"Privacy &amp; Terms – Google\"" +
                "><meta name=\"apple-mobile-web-app-title\" content=\"Privacy &amp; Terms – Google\">" +
                "<meta name=\"apple-mobile-web-app-status-bar-style\" content=\"black\"><meta name=\"" +
                "msapplication-tap-highlight\" content=\"no\"><link rel=\"manifest\" crossorigin=\"u" +
                "se-credentials\" href=\"_/IdentityPoliciesUi/manifest.json\"><link rel=\"home\" href" +
                "=\"/?lfhs=2\"><link rel=\"msapplication-starturl\" href=\"/?lfhs=2\"><link rel=\"icon\"" +
                " href=\"//ssl.gstatic.com/policies/favicon.ico\" sizes=\"32x32\"><link rel=\"apple-to" +
                "uch-icon-precomposed\" href=\"//ssl.gstatic.com/policies/favicon.ico\" sizes=\"32x32\">" +
                "<link rel=\"msapplication-square32x32logo\" href=\"//ssl.gstatic.com/policies/favicon.ico\"" +
                " sizes=\"32x32\"><script data-id=\"_gd\" nonce=\"kCuFspnXQp57WvVLLQk_Xw\">" +
                "window.WIZ_global_data = {\"DpimGf\":false,\"EP1ykd\":[\"/_/*\",\"/accounts/*\"],\"FdrFJe\":\"" +
                "-1122720826732327387\",\"FoW6je\":false,\"Im6cmf\":\"/_/IdentityPoliciesUi\",\"LVIXXb\":1,\"");

        Lemmas lemmasGoogle = new Lemmas();
        //lemmasGoogle.setSiteId(siteGoogle);
        lemmasGoogle.setLemma("Гугл");
        lemmasGoogle.setFrequency(55);

        Lemmas lemmasGoogleSearch = new Lemmas();
        //lemmasGoogleSearch.setSiteId(siteGoogle);
        lemmasGoogleSearch.setLemma("Программирование");
        lemmasGoogleSearch.setFrequency(5135);

        Lemmas lemmasPolicies = new Lemmas();
        //lemmasPolicies.setSiteId(siteGoogle);
        lemmasPolicies.setLemma("Технология");
        lemmasPolicies.setFrequency(334);

        sitesRepository.save(siteGoogle);
        pagesRepository.save(pagesGoogleSearch);
        pagesRepository.save(pagesGooglePolicies);
        lemmasRepository.save(lemmasPolicies);
        lemmasRepository.save(lemmasGoogleSearch);
        lemmasRepository.save(lemmasGoogle);
    }
}
