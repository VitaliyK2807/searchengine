package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.Sites;
import searchengine.repositories.SitesRepository;

@Service
public class SitesServiceImpl implements SitesService {

    @Autowired
    private SitesRepository sitesRepository;

    @Override
    public void addSite(Sites site) {
        sitesRepository.save(site);
    }
}
