package searchengine.utils.siteparsing;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import searchengine.utils.lemmas.LemmaFinder;
import searchengine.model.Indexes;
import searchengine.model.Lemmas;
import searchengine.model.Pages;
import searchengine.model.Sites;
import searchengine.repositories.IndexesRepository;
import searchengine.repositories.LemmasRepository;
import java.io.IOException;
import java.util.*;

@Slf4j
public class WritingLemmas {

    private String text;
    private Pages page;
    private List<Lemmas> lemmasList = new ArrayList<>();
    private List<Indexes> indexesList = new ArrayList<>();
    private LemmasRepository lemmasRepository;
    private IndexesRepository indexesRepository;


    public WritingLemmas(LemmasRepository lemmasRepository,
                         IndexesRepository indexesRepository,
                         Pages page,
                         String text) {
        this.lemmasRepository = lemmasRepository;
        this.indexesRepository = indexesRepository;
        this.page = page;
        this.text = text;
    }


    public void writeLemmaAndIndex() throws RuntimeException, IOException {
        LemmaFinder finder = new LemmaFinder(new RussianLuceneMorphology());
        List<Lemmas> lemmas = new ArrayList<>();
        synchronized (lemmasRepository) {
            finder.getCollectionLemmas(text)
                    .entrySet()
                    .forEach(word -> {
                        Lemmas lemma = getLemma(word.getKey(), page.getSiteId());

                        if (lemma.getId() == 0) {
                            lemmasList.add(lemma);
                        }
                        indexesList.add(getIndex(word.getValue(), lemma));
                    });
            lemmas.addAll(lemmasRepository.saveAll(lemmasList));
            lemmasList = new ArrayList<>();
        }
        lemmas.stream().forEach(lemma -> indexesList.forEach(index -> {
            if (index.getLemma().getLemma().equals(lemma.getLemma())) {
                index.getLemma().setId(lemma.getId());
            }
        }));
        indexesRepository.saveAll(indexesList);
    }

    private Lemmas getLemma(String word, Sites site) {
        Optional<Lemmas> lemma = lemmasRepository.findByLemmaAndSiteId(word, site);

        if (!lemma.isPresent()) {

            return Lemmas.builder()
                    .lemma(word)
                    .frequency(1)
                    .siteId(site)
                    .build();
        }
        lemmasRepository.updateLemma(lemma.get().getFrequency() + 1, lemma.get().getId());

        return lemma.get();
    }

    private Indexes getIndex(int rank, Lemmas lemma) {
        return Indexes.builder()
                .lemma(lemma)
                .rank(rank)
                .page(page)
                .build();
    }

}
