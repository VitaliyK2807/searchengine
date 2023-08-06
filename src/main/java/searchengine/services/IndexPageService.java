package searchengine.services;

import searchengine.dto.indexPageResponse.IndexPageResponse;

public interface IndexPageService {

    IndexPageResponse indexPageStart (String url);
}
