package searchengine.services;

import searchengine.dto.pagesearchresponse.PageSearchResponse;

public interface PageSearchService {
    PageSearchResponse pagesSearch(String query, String site, int offset, int limit);
}
