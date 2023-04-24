package searchengine.services;

import searchengine.model.Lemmas;

import java.util.List;

public interface LemmasService {
    void addLemma (Lemmas lemma);
    void addAllLemmas (List<Lemmas> lemmasList);
}
