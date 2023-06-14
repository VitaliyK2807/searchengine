package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
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

//    private final Random random = new Random();
//    private final SitesList sites;

    @Autowired
    private SitesRepository sitesRepository;

    @Autowired
    private PagesRepository pagesRepository;

    @Autowired
    private LemmasRepository lemmasRepository;

    @Override
    public StatisticsResponse getStatistics() {

        TotalStatistics total = new TotalStatistics();
        total.setSites(sitesRepository.findAll().size());
        total.setIndexing(true);
        total.setPages(pagesRepository.findAll().size());
        //total.getPages() + pages);
        total.setLemmas(lemmasRepository.findAll().size());
        //total.getLemmas() + lemmas);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Sites> sitesList = sitesRepository.findAll();

                //sites.getSites();
        for(int i = 0; i < sitesList.size(); i++) {
            Sites site = sitesList.get(i);

            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            //int pages = random.nextInt(1_000);
            //int lemmas = pages * random.nextInt(1_000);
            item.setPages(sitesRepository.getReferenceById(site.getId()).getPages().size());
            item.setLemmas(lemmasRepository.countRecordsById(site.getId()));
            item.setStatus(sitesRepository.getReferenceById(site.getId()).getStatus().toString());
                    //statuses[i % 3]);
            item.setError(sitesRepository.getReferenceById(site.getId()).getLastError());
                    //errors[i % 3]);
            item.setStatusTime(sitesRepository.getReferenceById(site.getId()).getStatusTime());
//                    System.currentTimeMillis() -
//                    (random.nextInt(10_000)));
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
