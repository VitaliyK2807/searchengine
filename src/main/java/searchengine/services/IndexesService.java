package searchengine.services;

import searchengine.model.Indexes;

import java.util.List;

public interface IndexesService {

    void addIndex (Indexes index);

    void addAllIndexes (List<Indexes> indexesList);
}
