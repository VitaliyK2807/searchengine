package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.Lemmas;
import searchengine.repositories.LemmasRepository;

import java.util.List;

@Service
public class LemmasServiceImpl implements LemmasService{

    @Autowired
    LemmasRepository lemmasRepository;

    @Override
    public void addLemma(Lemmas lemma) {
        lemmasRepository.save(lemma);
    }

    @Override
    public void addAllLemmas(List<Lemmas> lemmasList) {
        lemmasRepository.saveAll(lemmasList);
    }
}
