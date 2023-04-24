package searchengine.siteParsing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParsingSite extends RecursiveTask<TreeSet<String>> {

    private volatile TreeSet<String> linksMap;
    private String url;

    public ParsingSite(String url, TreeSet<String> linksMap) {
        this.url = url;
        this.linksMap = linksMap;

        linksMap.add(url);
    }

    @Override
    protected TreeSet<String> compute() {

        List<ParsingSite> listTasks = new ArrayList<>();
        List<String> urlLinks = parsingLinksSite(url);

        urlLinks.forEach(child -> {
            ParsingSite parsingSite = new ParsingSite(child, linksMap);
            parsingSite.fork();
            listTasks.add(parsingSite);
        });

        listTasks.stream().forEach(task -> linksMap.addAll(task.join()));

        return linksMap;
    }

    private List<String> parsingLinksSite(String path) {
        String regex = "http[s]?://" + getDomain(path) + ".[a-z]+/[^,\\s\"><«»а-яА-Я]+";
        List<String> childesLinks = new ArrayList<>();
        connect(path).forEach(element -> {
            String receivedURL = getAddress(String.valueOf(element), regex);
            if (receivedURL != ""
                    && !childesLinks.equals(receivedURL)
                    && !linksMap.contains(receivedURL)
                    && receivedURL.endsWith("/")
                    && receivedURL != null) {
                childesLinks.add(receivedURL);
                linksMap.add(receivedURL);
            }
        });
        return childesLinks;
    }

    private Elements connect(String path) {
        try {
            Document document = document = Jsoup.connect(path)
                    .timeout(10000)
                    .userAgent("Chrome/109.0.5414.120 Safari/532.5")
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true)
                    .get();
            return document.select("[href]").select("a");
        } catch (IOException sTE) {
            System.out.println("Exception URL -> " + path + " " + sTE.toString());
        } catch (Exception ex) {
            System.out.println("Other exceptions URL -> " + path + " " + ex.getMessage());
        }
        return null;
    }

    private String getAddress(String str, String regex) {
        String result = "";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            result = str.substring(start, end);
        }
        return result;
    }

    private String getDomain(String path) {
        int start = path.indexOf("/");
        int end = path.indexOf(".", start + 2);
        return path.substring(start + 2, end);
    }

    public TreeSet<String> getLinksMap() {
        return linksMap;
    }
}
