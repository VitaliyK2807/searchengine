package searchengine.dto.pageSearch;

import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.dto.lemmas.LemmaFinder;

import java.io.IOException;
import java.util.*;

public class HtmlParser {
    private static final Integer DIFFERENCE = 3;
    private static final Integer COUNTS_SYMBOL = 200;
    private String[] wordsContent;
    private LemmaFinder finder;
    private String text;
    private TreeSet<String> setLemmas;
    private Document document;
    public HtmlParser() {
        try {
            finder = new LemmaFinder(new RussianLuceneMorphology());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getTitle(String content) {
        document = Jsoup.parse(content);
        return document.title();
    }

    public String getSnippets(String query) {
        text = document.text();
        wordsContent = text.split("\\s+");
        setLemmas = finder.getSetLemmas(query);

        StringJoiner joiner = new StringJoiner(". ");

        List<String> listReadyText = new ArrayList<>();

        getArrayIndexes(getModifiedArray(wordsContent), setLemmas)
                .forEach(i -> listReadyText.add(getString(i)));

        listReadyText.forEach(string -> joiner.add(string));

       if (joiner.toString().length() > COUNTS_SYMBOL) {
           return joiner.toString().substring(0, COUNTS_SYMBOL);
       }
        return joiner.toString();
    }
    private String getString(Integer index) {
        int from = index - DIFFERENCE;
        int to = index + DIFFERENCE;

        if (testDiff(from) & testAddition(to)) {
            return redyString(0, wordsContent.length, index);
        }
        if (testDiff(from) & !testAddition(to)) {
            return redyString(0, to, index);
        }
        if (!testDiff(from) & testAddition(to)) {
            return redyString(from, wordsContent.length, index);
        }

        return redyString(from, to, index);
    }


    private boolean testDiff(Integer from) {
        if (from <= 0) {
            return true;
        }
        return false;
    }

    private boolean testAddition (Integer to) {
        if (to >= wordsContent.length) {
            return true;
        }
        return false;
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
    private ArrayList<Integer> getArrayIndexes(ArrayList<String> lemmasInText, TreeSet<String> set) {
        ArrayList<Integer> arrayIndexes = new ArrayList<>();
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

    private ArrayList<String> getModifiedArray(String[] wordsContent) {
        ArrayList<String> listLemmas = new ArrayList<>();
        for (int i = 0; i < wordsContent.length; i++) {
            listLemmas.add(finder.getNormalForm(wordsContent[i]));
        }

        return listLemmas;
    }

}
