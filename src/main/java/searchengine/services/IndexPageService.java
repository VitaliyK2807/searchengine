package searchengine.services;

import searchengine.dto.indexpageresponse.IndexPageResponse;

public interface IndexPageService {

    IndexPageResponse indexPageStart (String url);
}
