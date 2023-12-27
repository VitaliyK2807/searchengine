package searchengine.dto.pageSearch;

import searchengine.model.Indexes;
import searchengine.model.Lemmas;
import searchengine.model.Sites;
import searchengine.repositories.IndexesRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IndexMapSearch {

    private Map<Integer, List<Indexes>> indexesMap;

    private IndexesRepository indexesRepository;


    public IndexMapSearch(IndexesRepository indexesRepository) {
        this.indexesRepository = indexesRepository;
    }

    public Set<Relevance> getIndexMapSearch (Sites webSite, Set<Lemmas> lemmas) {
        List<Integer> foundIdPages = new ArrayList<>();

        foundIdPages.addAll(getListIdPages(lemmas));

        Map<Integer, Double> absolutesRelevance = getAbsoluteRelevanceValue(foundIdPages, lemmas);

        if (absolutesRelevance.isEmpty()) {
           return new HashSet<>();
        }

        indexesMap.clear();

        return getSetRelevance(webSite, absolutesRelevance);
    }

    private Set<Relevance> getSetRelevance(Sites webSite, Map<Integer, Double> mapAbsolutesRelevance) {
        Double maxAbsRelevance = mapAbsolutesRelevance.entrySet().stream()
                .mapToDouble(value -> value.getValue())
                .max()
                .getAsDouble();
        return mapAbsolutesRelevance
                .entrySet()
                .stream()
                .map(p -> Relevance.builder()
                        .site(webSite)
                        .pageId(p.getKey())
                        .absolutesRelevance(p.getValue())
                        .relativeRelevance(p.getValue() / maxAbsRelevance)
                        .build())
                .collect(Collectors.toSet());


    }
    private Map<Integer, Double> getAbsoluteRelevanceValue(List<Integer> idPages, Set<Lemmas> lemmas) {
        Map<Integer, Map<Lemmas, Double>> rankForPage = idPages.stream()
                .collect(Collectors.toMap(Function.identity(), page -> getMapLemmasAndRank(page, lemmas)));

        return rankForPage.entrySet().stream()
                .collect(Collectors.toMap(page -> page.getKey(), page -> getABSRelevance(page.getValue())));
    }

    private Double getABSRelevance(Map<Lemmas, Double> mapLemmasAndRank) {
        double maxValue = mapLemmasAndRank.entrySet().stream().mapToDouble(value -> value.getValue()).max().getAsDouble();
        return mapLemmasAndRank.entrySet().stream()
                .mapToDouble(value -> value.getValue())
                .sum() / maxValue;
    }

    private Map<Lemmas, Double> getMapLemmasAndRank(int pageId, Set<Lemmas> lemmas) {
        return lemmas.stream()
                .collect(Collectors.toMap(Function.identity(), lemma -> getSumRank(pageId, lemma.getId())));
    }

    private Double getSumRank(int pageId, int lemmaId) {
        for (Map.Entry<Integer, List<Indexes>> page : indexesMap.entrySet()) {
            if (page.getKey() == pageId) {
                return getSum(page.getValue(), lemmaId);
            }
        }
        return 0.0;
    }

    private Double getSum(List<Indexes> indexes, int lemmaId) {
        return indexes.stream()
                .filter(index -> index.getLemma().getId() == lemmaId)
                .mapToDouble(Indexes::getRank)
                .sum();
    }

    private List<Integer> getListIdPages(Set<Lemmas> lemmas) {//TODO:Оптимизировать
        List<Indexes> foundIndexes = new ArrayList<>();
        lemmas.stream()
                .sorted(Comparator.comparing(Lemmas::getFrequency))
                .forEach(l -> {
                    if (foundIndexes.isEmpty()) {
                        foundIndexes.addAll(getIndexes(l.getId()));
                    } else {
                        List<Indexes> indexes = updateIndexes(foundIndexes, l.getId());
                        foundIndexes.clear();
                        foundIndexes.addAll(indexes);
                    }
                });

        return foundIndexes
                .stream()
                .map(i -> i.getPage().getId())
                .collect(Collectors.toList());
    }

    private List<Indexes> updateIndexes(List<Indexes> indexList, int idLemma) {
        return indexList.stream()
                .map(index -> getIndex(index.getPage().getId(), idLemma))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<Indexes> getIndexes(int idLemma) {
        List<Indexes> indexes = indexesRepository.findByLemma_Id(idLemma);
        indexesMap = indexes.stream()
                .collect(Collectors.groupingBy(i -> i.getPage().getId(), Collectors.toList()));

        return indexes;
    }

    private Optional<Indexes> getIndex(int pageId, int idLemma) {
        Optional<Indexes> ind = indexesRepository.findByPage_IdAndLemma_Id(pageId, idLemma);
        if (ind.isPresent()) {
            indexesMap.entrySet().forEach(page -> {
                if (pageId == page.getKey()) {
                    page.getValue().add(ind.get());
                }
            });
        }
        return ind;
    }
    public Map<Integer, Double> getMapForTestAbsoluteRelevanceValue(List<Integer> pages, Set<Lemmas> lemmas) {
        Map<Integer, Map<Lemmas, Double>> rankForPage = pages.stream()
                .collect(Collectors.toMap(Function.identity(), page -> getMapLemmasAndRank(page, lemmas)));

        return rankForPage.entrySet().stream()
                .collect(Collectors.toMap(page -> page.getKey(), page -> getABSRelevance(page.getValue())));
    }

    public void setIndexesMapForTest (Map<Integer, List<Indexes>> mapIndexes) {
        indexesMap = mapIndexes;
    }
}
