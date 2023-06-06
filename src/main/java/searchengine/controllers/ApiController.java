package searchengine.controllers;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.indexingSites.IndexingSitesResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.StartSiteIndexingService;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
@Slf4j
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
        log.info("Command: @GetMapping(\"/statistics\")");
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingSitesResponse> startIndexing () {
        log.info("Command: @GetMapping(\"/startIndexing\")");
        return ResponseEntity.ok(startSiteIndexingService.indexingStart());
    }
}
