package searchengine.dto.siteParsing;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import java.lang.String;
import searchengine.dto.lemmas.LemmaFinder;
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
                        Lemmas lemma = getLemma(word.getKey(), word.getValue(), page.getSite());
                        indexesList.add(getIndex(word.getValue(), lemma));
                        lemmasList.add(lemma);
                    });
            lemmas.addAll(lemmasRepository.saveAll(lemmasList));
        }
        lemmasList = new ArrayList<>();
        lemmas.stream().forEach(lemma -> indexesList.forEach(index -> {
            if (index.getLemma().getLemma().equals(lemma.getLemma())) {
                index.getLemma().setId(lemma.getId());
            }
        }));
        indexesRepository.saveAll(indexesList);
    }

    private Lemmas getLemma(String word, Integer frequency, Sites site) {
        Optional<Lemmas> lemma = lemmasRepository.findByLemmaAndIdSite(word, site.getId());

        if (!lemma.isPresent()) {

            Lemmas newLemma = Lemmas.builder()
                            .lemma(word)
                                    .frequency(frequency)
                                            .site(site)
                                                    .build();
            return newLemma;
        }
        lemmasRepository.deleteLemmaById(lemma.get());
        lemma.get().setFrequency(lemma.get().getFrequency() + 1);

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
