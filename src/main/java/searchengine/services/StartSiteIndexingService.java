package searchengine.services;

import searchengine.dto.indexingSites.IndexingSitesResponse;
import searchengine.dto.indexingSites.IndexingStopResponse;

public interface StartSiteIndexingService {

    IndexingSitesResponse indexingStart ();

    IndexingStopResponse indexingStop();
}
