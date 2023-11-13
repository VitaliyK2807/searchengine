package searchengine.dto.lemmas;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class LemmaFinder {
    private final LuceneMorphology luceneMorphology;

    private static final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ", "МС", "ЧАСТ", "МС-П", "ВВОДН"};

    public LemmaFinder(LuceneMorphology luceneMorphology) {
        this.luceneMorphology = luceneMorphology;
    }

    public Map<String, Integer> getCollectionLemmas (String text) {
        return textCleaning(text).stream()
                .filter(word -> !anyWordBaseBelongToParticle(luceneMorphology.getMorphInfo(word)))
                .filter(word -> !luceneMorphology.getNormalForms(word).isEmpty())
                .collect(HashMap::new, (map, word) -> {
                    String normalWord = luceneMorphology.getNormalForms(word).get(0);
                    if (map.containsKey(normalWord)) {
                        map.put(normalWord, map.get(normalWord) + 1);
                    }
                    else {
                        map.put(normalWord, 1);
                    }
                }, HashMap::putAll);

    }

    public TreeSet<String> getSetLemmas (String text) {
        return textCleaning(text)
                .stream()
                .filter(word -> !anyWordBaseBelongToParticle(luceneMorphology.getMorphInfo(word)))
                .filter(word -> !luceneMorphology.getNormalForms(word).isEmpty())
                .collect(TreeSet::new, (set, word) -> {
                    String normalWord = luceneMorphology.getNormalForms(word).get(0);
                    if (!set.contains(normalWord)) {
                        set.add(normalWord);
                    }
                }, TreeSet::addAll);
    }

    private boolean anyWordBaseBelongToParticle(List<String> wordBaseForms) {
        return wordBaseForms.stream().anyMatch(this::hasParticleProperty);
    }

    private boolean hasParticleProperty(String wordBase) {
        for (String property : particlesNames) {
            if (wordBase.toUpperCase().contains(property)) {
                return true;
            }
        }
        return false;
    }
    private List<String> textCleaning (String text) {
        return Arrays.stream(text.toLowerCase(Locale.ROOT)
                .replaceAll("([^а-я\\s])", " ")
                .trim()
                .split("\\s+"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

    }

}
