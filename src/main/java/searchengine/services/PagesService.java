package searchengine.services;

import searchengine.model.Pages;

import java.util.List;

public interface PagesService {
    void addPage (Pages page);
    void addAllPages (List<Pages> pagesList);
}
