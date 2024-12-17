package google.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import google.model.Keyword;

public class GoogleQuery {
    public String searchKeyword;
    public String url;
    public List<Keyword> keywords;

    public GoogleQuery(String searchKeyword, List<Keyword> keywords) {
        this.searchKeyword = searchKeyword;
        this.keywords = keywords;
        try {
            String encodeKeyword = java.net.URLEncoder.encode(searchKeyword, "utf-8");
            this.url = "https://www.google.com/search?q=" + encodeKeyword + "&oe=utf8&num=20";
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private String fetchContent(String pageUrl) throws IOException {
        StringBuilder retVal = new StringBuilder();

        URL u = new URL(pageUrl);
        URLConnection conn = u.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        InputStream in = conn.getInputStream();

        InputStreamReader inReader = new InputStreamReader(in, "utf-8");
        BufferedReader bufReader = new BufferedReader(inReader);
        String line;

        while ((line = bufReader.readLine()) != null) {
            retVal.append(line);
        }
        return retVal.toString();
    }

    public LinkedHashMap<String, String> query() throws IOException {
        // 儲存網頁標題與分數的映射
        HashMap<String, Integer> resultScores = new HashMap<>();
        // 儲存標題與網址的映射
        HashMap<String, String> urlMap = new HashMap<>();
        // 使用 LinkedHashMap 確保有序
        LinkedHashMap<String, String> sortedResults = new LinkedHashMap<>();
        // 跟蹤已處理的 URL，避免重複
        Set<String> processedUrls = new HashSet<>();

        // 只抓取第一頁的搜尋結果
        String content = fetchContent(url);
        Document doc = Jsoup.parse(content);
        Elements lis = doc.select("div.kCrYT");

        for (Element li : lis) {
            try {
                String citeUrl = li.select("a").get(0).attr("href").replace("/url?q=", "");
                String cleanUrl = removeTrackingParameters(citeUrl);

                if (processedUrls.contains(cleanUrl)) continue;
                processedUrls.add(cleanUrl);

                String title = li.select("a").get(0).select(".vvjwJb").text();
                if (title.isEmpty()) continue;

                int totalScore = 0;

                for (Keyword keyword : keywords) {
                    WordCounter counter = new WordCounter(cleanUrl);
                    try {
                        int count = counter.countKeyword(keyword.getName());
                        totalScore += count * keyword.getWeight();
                    } catch (IOException e) {
                        continue;
                    }
                }

                if (totalScore > 0) {
                    resultScores.put(title, totalScore);
                    urlMap.put(title, cleanUrl);
                    System.out.println(cleanUrl + " Score " + totalScore);
                }

            } catch (IndexOutOfBoundsException e) {
                continue;
            }
        }

        // 按分數排序並組織結果
        resultScores.entrySet()
            .stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // 分數降序排序
            .forEach(entry -> sortedResults.put(entry.getKey(), urlMap.get(entry.getKey())));

        return sortedResults; // 返回已排序的標題和網址
    }

    private String removeTrackingParameters(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }

        try {
            if (url.startsWith("/url?q=")) {
                url = url.replace("/url?q=", "");
            }

            int ampIndex = url.indexOf('&');
            if (ampIndex != -1) {
                url = url.substring(0, ampIndex);
            }

            URL parsedUrl = new URL(url);
            return parsedUrl.getProtocol() + "://" + parsedUrl.getHost() + parsedUrl.getPath();

        } catch (Exception e) {
            System.out.println("解析 URL 時出現錯誤：" + url);
            return url;
        }
    }
}
