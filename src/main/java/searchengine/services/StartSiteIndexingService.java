package searchengine.services;

import searchengine.dto.indexingsites.IndexingSitesResponse;
import searchengine.dto.indexingsites.IndexingStopResponse;

public interface StartSiteIndexingService {

    IndexingSitesResponse indexingStart ();

    IndexingStopResponse indexingStop();
}
