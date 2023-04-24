package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.Pages;
import searchengine.repositories.PagesRepository;

import java.util.List;

@Service
public class PagesServiceImpl implements PagesService{

    @Autowired
    PagesRepository pagesRepository;

    @Override
    public void addPage(Pages page) {
        pagesRepository.save(page);
    }

    @Override
    public void addAllPages(List<Pages> pagesList) {
        pagesRepository.saveAll(pagesList);
    }
}
