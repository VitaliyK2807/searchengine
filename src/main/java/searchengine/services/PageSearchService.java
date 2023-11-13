package searchengine.services;

import searchengine.dto.pageSearch.PageSearchResponse;

public interface PageSearchService {
    PageSearchResponse pageSearch(String query, String site, int offset, int limit);
}
