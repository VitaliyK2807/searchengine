package searchengine.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.indexPageResponse.IndexPageResponse;
import searchengine.dto.indexingSites.IndexingSitesResponse;
import searchengine.dto.indexingSites.IndexingStopResponse;
import searchengine.dto.pageSearch.PageSearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexPageService;
import searchengine.services.PageSearchService;
import searchengine.services.StartSiteIndexingService;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
@Slf4j
public class ApiController {

    private final StartSiteIndexingService startSiteIndexingService;
    private final StatisticsService statisticsService;
    private final IndexPageService indexPageService; //Попробовать убрать final
    private final PageSearchService pageSearchService;



    public ApiController(StartSiteIndexingService startSiteIndexingService,
                         StatisticsService statisticsService,
                         IndexPageService indexPageService,
                         PageSearchService pageSearchService) {
        this.startSiteIndexingService = startSiteIndexingService;
        this.statisticsService = statisticsService;
        this.indexPageService = indexPageService;
        this.pageSearchService = pageSearchService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        log.info("Command: @GetMapping(\"/statistics\")");
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingSitesResponse> startIndexing() {
        log.info("Command: @GetMapping(\"/startIndexing\")");
        return ResponseEntity.ok(startSiteIndexingService.indexingStart());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexingStopResponse> stopIndexing() {
        log.info("Command: @GetMapping(\"/stopIndexing\")");
        return ResponseEntity.ok(startSiteIndexingService.indexingStop());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexPageResponse> indexPage(@RequestParam String url) {
        log.info("Command: @PostMapping(\"/indexPage\")");

        if (url.isEmpty()) {
            log.info("Empty page address entry field!");
            IndexPageResponse indexPageResponse =
                    new IndexPageResponse(false ,"Не введен адрес страницы!" );
            return ResponseEntity.ok(indexPageResponse);
        }

        return ResponseEntity.ok(indexPageService.indexPageStart(url));
    }

    @GetMapping("/search")
    public ResponseEntity<PageSearchResponse> searchPages (@RequestParam String query,
                                                           String site,
                                                           int offset,
                                                           int limit) {
        log.info("Command: @GetMapping(\"/search\")");

        if (query.isEmpty()) {
            log.info("Empty page search input field!");
            PageSearchResponse pageSearchResponse =
                    new PageSearchResponse(false, "Задан пустой поисковый запрос");
            return ResponseEntity.ok(pageSearchResponse);
        }
        return ResponseEntity.ok(pageSearchService.pagesSearch(query, site, offset, limit));
    }
}
