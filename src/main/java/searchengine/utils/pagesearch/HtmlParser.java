package searchengine.utils.pagesearch;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.utils.lemmas.LemmaFinder;
import java.io.IOException;
import java.util.*;
@Slf4j
public class HtmlParser {
    private static final Integer DIFFERENCE = 4;
    private static final Integer COUNTS_SYMBOL = 200;
    private String[] wordsContent;
    private LemmaFinder finder;
    private Document document;

    public HtmlParser(String content) {
        document = Jsoup.parse(content);
        try {
            finder = new LemmaFinder(new RussianLuceneMorphology());
        } catch (IOException e) {
            log.error("HtmlParser/ " + e.getMessage());
        }
    }

    public String getTitle() {
        return document.title();
    }

    public String getSnippets(String query) {
        String text = document.text();
        wordsContent = text.split("\\s+");
        Set<String> setLemmas = finder.getSetLemmas(query);

        StringJoiner joiner = new StringJoiner("... ");

        List<String> listReadyText = new ArrayList<>();

        getArrayIndexes(getModifiedArray(wordsContent), setLemmas)
                .forEach(i -> listReadyText.add(getString(i)));

        listReadyText.forEach(joiner::add);

       if (joiner.toString().length() > COUNTS_SYMBOL) {
           return joiner.toString().substring(0, COUNTS_SYMBOL);
       }
        return joiner.toString();
    }

    private String getString(Integer index) {
        int from = index - DIFFERENCE;
        int to = index + DIFFERENCE;

        if (testDiff(from) && testAddition(to)) {
            return redyString(0, wordsContent.length, index);
        }
        if (testDiff(from) && !testAddition(to)) {
            return redyString(0, to, index);
        }
        if (!testDiff(from) && testAddition(to)) {
            return redyString(from, wordsContent.length, index);
        }

        return redyString(from, to, index);
    }


    private boolean testDiff(Integer from) {
        return  from <= 0;
    }

    private boolean testAddition (Integer to) {
        return to >= wordsContent.length;
    }

    private String redyString(int from, int to, int index) {
        StringJoiner joiner = new StringJoiner("\s");
        for (int i = from; i < to; i++) {
            if (i == index) {
                joiner.add("<b>" + wordsContent[i] + "</b>");
            } else {
                joiner.add(wordsContent[i]);
            }
        }
        return joiner.toString();
    }
    private List<Integer> getArrayIndexes(List<String> lemmasInText, Set<String> set) {
        List<Integer> arrayIndexes = new ArrayList<>();
        set.forEach(lemma -> {
            for (int i = 0; i < lemmasInText.size(); i++) {
                if (lemmasInText.get(i).equals(lemma)) {
                    arrayIndexes.add(i);
                    break;
                }
            }
        });

        return arrayIndexes;
    }

    private List<String> getModifiedArray(String[] wordsContent) {
        List<String> listLemmas = new ArrayList<>();
        for (int i = 0; i < wordsContent.length; i++) {
            listLemmas.add(finder.getNormalForm(wordsContent[i]));
        }

        return listLemmas;
    }

}
