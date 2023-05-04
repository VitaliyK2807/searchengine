package searchengine.dto.siteParsing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.TreeSet;
import java.util.concurrent.ForkJoinPool;

public class Parsing {

    private static final String PATH_SITE = "https://skillbox.ru/";

    private static final String PATH_RESULT = "src/main/FileSiteMap/MapSite.txt";

    public static void main(String[] args) {
        TreeSet<String> list = new TreeSet<>();

        System.out.println("Начало поиска!");
        ParsingSite parsingSite = new ParsingSite(PATH_SITE, list);

        try {
            new ForkJoinPool().invoke(parsingSite);
        } catch (NullPointerException nEx) {
            System.out.println("Exception for Class.main, 25 line -> " + nEx.getMessage());
        } catch (ConcurrentModificationException cME) {
            System.out.println("Exception for Class.main, 25 line -> " + cME.getMessage());
        }
        list.addAll(parsingSite.getLinksMap());

        System.out.println("Поиск ссылок окончен, количество ссылок: " + list.size());
        creatFile(listModification(list).toString());


    }

    private static void creatFile (String string) {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);

        try {
            Files.write(Paths.get(PATH_RESULT), bytes);
            System.out.println("Создан файл: " + PATH_RESULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static StringBuilder listModification (TreeSet<String> readyList) {
        StringBuilder builder = new StringBuilder();
        readyList.stream()
                .sorted(Comparator.naturalOrder())
                .forEach(line -> builder.append(getCountOfTabs(line) + line + "\n"));
        return builder;
    }

    private static String getCountOfTabs (String string) {
        int start = string.indexOf("/") + 2;
        int count = -1;
        String result = "";

        for (int i = start ; i < string.length(); i++) {
            if (string.charAt(i) == '/') {
                count++;
            }
        }

        if (count == 0) {return result;}

        for (int i = 0; i < count; i++) {
            result += "\t";
        }

        return result;
    }
}
