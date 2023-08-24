package searchengine.dto.siteParsing;

import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.beans.factory.annotation.Autowired;
import java.lang.String;
import searchengine.dto.lemmas.LemmaFinder;
import searchengine.model.Indexes;
import searchengine.model.Lemmas;
import searchengine.model.Pages;
import searchengine.model.Sites;
import searchengine.repositories.IndexesRepository;
import searchengine.repositories.LemmasRepository;

import java.io.IOException;
import java.util.Optional;

public class WritingLemmas {

    private String text;
    private Pages page;

    private LemmasRepository lemmasRepository;
    private IndexesRepository indexesRepository;

    @Autowired
    public WritingLemmas(String text,
                         Pages page,
                         LemmasRepository lemmasRepository,
                         IndexesRepository indexesRepository) {
        this.text = text;
        this.page = page;
        this.lemmasRepository = lemmasRepository;
        this.indexesRepository = indexesRepository;
    }

    public synchronized void writeLemmaAndIndex() {
        try {
            LemmaFinder finder = new LemmaFinder(new RussianLuceneMorphology());

            finder.getCollectionLemmas(text)
                    .entrySet()
                    .forEach(word -> {
                        Lemmas lemma = saveLemmas(word.getKey(), page.getSite());
                        saveIndex(lemma, page, word.getValue());

                    });
        } catch (IOException e) {

            throw new RuntimeException(e);
        }

    }

    private void saveIndex (Lemmas lemma, Pages page, float rank) {
        Indexes index = new Indexes();
        index.setPage_id(page.getId());
        index.setLemma_id(lemma.getId());
        index.setRank(rank);
        indexesRepository.save(index);
    }

    private Lemmas saveLemmas(String word, Sites site) {
        Optional<Lemmas> lemma = null;
        try {
            lemma = lemmasRepository.findByLemmaAndIdSite(word, site.getId());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());//nested exception is javax.persistence.NonUniqueResultException: query did not return a unique result:
        }


        if (lemma.isEmpty()) {
            Lemmas newLemma = new Lemmas();
            newLemma.setLemma(word);
            newLemma.setFrequency(1);
            newLemma.setSiteId(site);

            return lemmasRepository.save(newLemma);
        }

        lemmasRepository.updateLemma(lemma.get().getFrequency() + 1, lemma.get().getId());

        return lemma.get();
    }


}
