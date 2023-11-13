package searchengine.dto.siteParsing;

import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import searchengine.dto.lemmas.LemmaFinder;
import searchengine.model.Pages;
import searchengine.model.Sites;
import searchengine.repositories.PagesRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveAction;

public class ReadWriteLemmasAndIndexes extends RecursiveAction {
    private ConcurrentHashMap<String, Integer> mapLemmas;
    private PagesRepository pagesRepository;
    private int index;
    private Sites site;
    private int timeOut;

    public ReadWriteLemmasAndIndexes(ConcurrentHashMap<String, Integer> mapLemmas,
                                     PagesRepository pagesRepository,
                                     int index,
                                     Sites site) {
        this.mapLemmas = mapLemmas;
        this.pagesRepository = pagesRepository;
        this.index = index;
        this.site = site;
        this.timeOut = timeOut;
    }

    @Override
    protected void compute() {
        List<ReadWriteLemmasAndIndexes> listTasks = new ArrayList<>();

        Optional<Pages> page = pagesRepository.findByIdAndSite(index, site);
        saveLemmasAndIndexes(page.get());

        if (!page.isEmpty()) {
            ReadWriteLemmasAndIndexes readAndWrite =
                    new ReadWriteLemmasAndIndexes(mapLemmas,
                                                  pagesRepository,
                                                  ++index,
                                                  site);
            readAndWrite.fork();
            listTasks.add(readAndWrite);
            System.out.println(mapLemmas.size());
        }
        listTasks.forEach(ReadWriteLemmasAndIndexes::join);
    }

    private void saveLemmasAndIndexes(Pages page) {
        try {
            LemmaFinder finder = new LemmaFinder(new RussianLuceneMorphology());
            finder.getSetLemmas(page.getContent())
                    .forEach(word -> {
                        if (mapLemmas.containsKey(word)) {
                            mapLemmas.put(word, mapLemmas.get(word) + 1);
                        } else {
                            mapLemmas.put(word, 1);
                        }
                    });

        } catch (IOException e) {

            throw new RuntimeException(e);
        }
    }
}
