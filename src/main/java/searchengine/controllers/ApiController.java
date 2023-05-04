package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.indexingSites.IndexingSitesResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.StartSiteIndexingService;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StartSiteIndexingService startSiteIndexingService;

    private final StatisticsService statisticsService;

    public ApiController(StartSiteIndexingService startSiteIndexingService,
                         StatisticsService statisticsService) {
        this.startSiteIndexingService = startSiteIndexingService;
        this.statisticsService = statisticsService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @PatchMapping("/startIndexing")
    public ResponseEntity<IndexingSitesResponse> startIndexing () {

        return ResponseEntity.ok(startSiteIndexingService.indexingStart());
    }
}
