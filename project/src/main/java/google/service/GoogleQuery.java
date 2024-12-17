package google.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedHashMap;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class GoogleQuery {
    public String searchKeyword;
    public String url;
    public String content;
    // public List<Keyword> keywords;

    public GoogleQuery(String searchKeyword) {
        this.searchKeyword = searchKeyword;
        try {
            String encodeKeyword = java.net.URLEncoder.encode(searchKeyword, "utf-8");
            this.url = "https://www.google.com/search?q=" + encodeKeyword + "&oe=utf8&num=30";
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private String fetchContent(String pageUrl) throws IOException {
        StringBuilder retVal = new StringBuilder();

        URL u = new URL(pageUrl);
        URLConnection conn = u.openConnection();
        conn.setRequestProperty("User-agent", "Mozilla/5.0");
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

        // 遍歷抓取多個頁面的搜尋結果
        for (int page = 0; page < 3; page++) { // 假設我們抓取前 5 頁的結果
            String pageUrl = url + "&start=" + (page * 30); // 每一頁的 URL，頁數從 0 開始
            String content = fetchContent(pageUrl); // 獲取指定頁面的 HTML 內容
            
            // 解析 HTML 文件
            Document doc = Jsoup.parse(content);
            Elements lis = doc.select("div.kCrYT");

            for (Element li : lis) {
                try {
                    // 提取網址
                    String citeUrl = li.select("a").get(0).attr("href").replace("/url?q=", "");
                    String cleanUrl = removeTrackingParameters(citeUrl);

                    // 提取標題
                    String title = li.select("a").get(0).select(".vvjwJb").text();
                    if (title.isEmpty()) {
                        continue;
                    }

                    // 計算分數（如網頁無法訪問則略過）
                    WordCounter counter = new WordCounter(cleanUrl);
                    int count = counter.countKeyword("taiwan");

                    // 只儲存有效分數的網頁
                    if (count > 0) {
                        resultScores.put(title, count);
                        urlMap.put(title, cleanUrl);
                        System.out.println(cleanUrl + " Score " + count);
                    }

                } catch (IndexOutOfBoundsException | IOException e) {
                    // 略過有錯誤的網頁
                    continue;
                }
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
            return url; // 如果 URL 為空，直接返回
        }
    
        try {
            // 移除 Google 特有的 "/url?q=" 前綴
            if (url.startsWith("/url?q=")) {
                url = url.replace("/url?q=", "");
            }
    
            // 在第一個 "&" 處截斷 URL，移除多餘的追蹤參數
            int ampIndex = url.indexOf('&');
            if (ampIndex != -1) {
                url = url.substring(0, ampIndex); // 只保留 "&" 之前的部分
            }
    
            // 使用 URL 類解析 URL，確保格式正確
            URL parsedUrl = new URL(url);
    
            // 返回只包含協議、主機名和路徑的乾淨 URL
            return parsedUrl.getProtocol() + "://" + parsedUrl.getHost() + parsedUrl.getPath();
    
        } catch (Exception e) {
            // 如果解析出錯，返回原始 URL 並打印錯誤訊息
            System.out.println("解析 URL 時出現錯誤：" + url);
            return url;
        }
    }
    
    
}
