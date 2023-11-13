package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.Pages;
import searchengine.model.Sites;
import searchengine.repositories.LemmasRepository;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private SitesRepository sitesRepository;

    @Autowired
    private PagesRepository pagesRepository;

    @Autowired
    private LemmasRepository lemmasRepository;

    @Override
    public StatisticsResponse getStatistics() {
        List<Sites> sitesList = sitesRepository.findAll();

        TotalStatistics total = new TotalStatistics();
        total.setSites(sitesList.size());
        total.setIndexing(true);
        total.setPages(pagesRepository.getTotalPages());
        total.setLemmas(lemmasRepository.getTotalLemmas());

        List<DetailedStatisticsItem> detailed = new ArrayList<>();

        for(int i = 0; i < sitesList.size(); i++) {
            Sites site = sitesList.get(i);

            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            item.setPages(pagesRepository.getTotalPagesSite(site));
            item.setLemmas(lemmasRepository.getTotalLemmasSites(site.getId()));
            item.setStatus(sitesRepository.getReferenceById(site.getId()).getStatus().toString());
            item.setError(sitesRepository.getReferenceById(site.getId()).getLastError());
            item.setStatusTime(sitesRepository.getReferenceById(site.getId()).getStatusTime());
            detailed.add(item);
        }
        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }



}
