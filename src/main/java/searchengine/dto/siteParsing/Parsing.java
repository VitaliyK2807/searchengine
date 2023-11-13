package searchengine.dto.siteParsing;

import lombok.extern.slf4j.Slf4j;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.*;
import searchengine.repositories.IndexesRepository;
import searchengine.repositories.LemmasRepository;
import searchengine.repositories.PagesRepository;
import searchengine.repositories.SitesRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class Parsing extends Thread{
    private Sites webSite;
    private ParsingSite parsingSite;
    private boolean stopFJPUser = false;
    private ForkJoinPool forkJoinPool;

    public boolean isRun;
    private CopyOnWriteArraySet<String> listUrls;
    private ConcurrentHashMap<Pages, AssemblyLemma> assemblyLemmas;

    private Map<String, Integer> mapFrequencyLemma;

    private PagesRepository pagesRepository;
    private SitesRepository sitesRepository;
    private LemmasRepository lemmasRepository;
    private IndexesRepository indexesRepository;



    public Parsing(Sites webSite,
                   SitesRepository sitesRepository,
                   PagesRepository pagesRepository,
                   LemmasRepository lemmasRepository,
                   IndexesRepository indexesRepository) {
        listUrls = new CopyOnWriteArraySet<>();
        assemblyLemmas = new ConcurrentHashMap<>();
        this.webSite = webSite;
        this.sitesRepository = sitesRepository;
        this.pagesRepository = pagesRepository;
        this.lemmasRepository = lemmasRepository;
        this.indexesRepository = indexesRepository;
        isRun = false;
    }

    @Override
    public void run() {

        isRun = true;

        long startParsing = System.currentTimeMillis();

        log.info("WebSite " + webSite.getName() + " parsing start.");

        if (!webSite.getUrl().endsWith("/")) {
            webSite.setUrl(webSite.getUrl() + "/");
        }
        parsingSite = new ParsingSite(webSite.getUrl(),
                                                    webSite.getName(),
                                                    listUrls,
                                                    webSite,
                                                    sitesRepository,
                                                    pagesRepository,
                                                    lemmasRepository,
                                                    indexesRepository);
        try {
            forkJoinPool = new ForkJoinPool();

            forkJoinPool.invoke(parsingSite);

            //savingTablesLemmaAndIndex();

            if (parsingSite.fatalError) {
                //savingTablesLemmaAndIndex();
                printMassageInfo(", stopped after critical error: ", startParsing);
                sitesRepository.updateFailed(Status.FAILED,
                        "Остановлено после критической ошибки!",
                        LocalDateTime.now(),
                        webSite.getId());

            } else if (stopFJPUser) {
                //savingTablesLemmaAndIndex();
                printMassageInfo(", was stopped by the user after: ", startParsing);
                sitesRepository.updateFailed(Status.FAILED,
                        "Остановлено пользователем!",
                        LocalDateTime.now(),
                        webSite.getId());

            } else {
               // savingTablesLemmaAndIndex();
                printMassageInfo(", completed in: ", startParsing);

                sitesRepository.updateStatusById(Status.INDEXED, LocalDateTime.now(), webSite.getId());

            }

        isRun = false;
        } catch (NullPointerException nEx) {
            log.error(nEx.getSuppressed().toString());
            printMessageError(startParsing);

            sitesRepository.updateFailed(Status.FAILED,
                    "NullPointerException",
                    LocalDateTime.now(),
                    webSite.getId());

            isRun = false;
        }

    }

    public void stopped () {

        parsingSite.stop = true;

        forkJoinPool.shutdown();

        stopFJPUser = true;

    }


    private void savingTablesLemmaAndIndex() {
        List<Pages> locals = new ArrayList<>(1_000);
        boolean finished = false;

        System.out.println(pagesRepository.findById(1).get().getSite().getName());
//        while (!finished) {
//            for (int i = 0; ; i++) {
//                Entity ent = readEntityFrom(xml);
//                // readEntity function must return null when no more remain to read
//                if (ent == null) {
//                    finished = true;
//                    break;
//                }
//            }
//        }
//        List<Pages> pages = pagesRepository.findBySite(webSite);
//        System.out.println(pages.size());
//        mapFrequencyLemma = new TreeMap<>();
//        assemblyLemmas.entrySet()
//                .stream()
//                .forEach(l -> addMapFrequencyLemma(l.getValue().getLemma()));
//
//        mapFrequencyLemma.entrySet()
//                .stream()
//                .forEach(l -> {
//                    Lemmas lemma = saveLemmas(l.getKey(), l.getValue());
//                    saveIndexes(lemma);
//                });
    }

    private void addMapFrequencyLemma(String lemma) {
        if (mapFrequencyLemma.containsKey(lemma)) {
            mapFrequencyLemma.put(lemma, mapFrequencyLemma.get(lemma) + 1);
        } else {
            mapFrequencyLemma.put(lemma, 1);
        }

    }

    private Lemmas saveLemmas(String word, int frequency) {
        Lemmas newLemma = new Lemmas();

        newLemma.setLemma(word);
        newLemma.setFrequency(frequency);
        newLemma.setSite(webSite);

        return lemmasRepository.save(newLemma);

    }

    private void saveIndexes (Lemmas lemma) {
        assemblyLemmas.entrySet()
                .stream()
                .forEach(l -> {
                    if (l.getValue().getLemma().equals(lemma.getLemma())) {
                        Indexes index = new Indexes();
                        index.setLemma(lemma);
                        index.setRank(l.getValue().getRank());
                        index.setPage(l.getKey());
                        indexesRepository.save(index);
                    }
                });
    }

    private void printMassageInfo (String message, long startParsing) {
        log.info("Parsing of the site: " + webSite.getName() + message +
                ((System.currentTimeMillis() - startParsing) / 1000) + " s.");
        log.info("Added number of entries: " + listUrls.size() + ", for site: " + webSite.getName());
    }
    private void printMessageError (long startParsing) {
        log.error("Parsing of the site: " + webSite.getName() + ", stopped in: " +
                ((System.currentTimeMillis() - startParsing) / 1000) + " s.");
        log.error("Added number of entries: " + listUrls.size() + ", for site: " + webSite.getName());
    }

}
