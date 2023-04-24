package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.Indexes;
import searchengine.repositories.IndexesRepository;

import java.util.List;

@Service
public class IndexesServiceImpl implements IndexesService{

    @Autowired
    IndexesRepository indexesRepository;

    @Override
    public void addIndex(Indexes index) {
        indexesRepository.save(index);
    }

    @Override
    public void addAllIndexes(List<Indexes> indexesList) {
        indexesRepository.saveAll(indexesList);
    }
}
