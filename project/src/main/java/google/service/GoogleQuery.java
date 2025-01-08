package google.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


import java.util.*;
import java.util.concurrent.*;

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
            this.url = "https://www.google.com/search?q=" + encodeKeyword  + "台灣運動賽事隊伍" + "&oe=utf8&num=30";
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private String fetchContent(String pageUrl) throws IOException {
        StringBuilder retVal = new StringBuilder();
    
        try {
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
    
            // 延遲以減少請求頻率
            Thread.sleep(1000); // 延遲 1 秒
    
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("請求被中斷", e);
        }
    
        return retVal.toString();
    }
    

    // 計算頁面分數
    private int calculatePageScore(String url) throws IOException {
        int score = 0;
        WordCounter counter = new WordCounter(url);
        for (Keyword keyword : keywords) {
            try {
                int count = counter.countKeyword(keyword.getName());
                score += count * keyword.getWeight();
            } catch (IOException e) {
                continue;
            }
        }
        return score;
    }



    // 抓取子頁面連結 (限制為最多抓取 5 個)
    private List<String> fetchSubPages(String url) throws IOException {
        List<String> subPages = new ArrayList<>();
        try {
            String content = fetchContent(url);
            Document doc = Jsoup.parse(content);
            Elements links = doc.select("a[href]");
            int count = 0;

            for (Element link : links) {
                String subPageUrl = link.attr("abs:href");
                if (subPageUrl.startsWith(url)) { // 確保是子網頁
                    subPages.add(subPageUrl);
                    count++;
                    if (count >= 5) { // 最多抓取 5 個
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error fetching sub-pages for URL: " + url);
        }
        return subPages;
    }


    public List<String> relatedSearch() throws IOException{
        List<String> relatedSearchResults = new ArrayList<>();
        try {
            String content = fetchContent(url);
            Document doc = Jsoup.parse(content);
            Elements relatedSearches = doc.select("div.kjGX2");
            for (Element search : relatedSearches) {
                // 提取每個相關搜尋的文本
                String searchText = search.select("div.kjGX2").text();
                relatedSearchResults.add(searchText); // 保存每個搜尋項目的文字
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(relatedSearchResults);
        return relatedSearchResults;
    }

    // public List<String> relatedSearch() {
    //     List<String> relatedSearchResults = new ArrayList<>();
    //     WebDriver driver = null;

    //     try {
    //         // 設定 ChromeDriver 路徑
    //         System.setProperty("webdriver.chrome.driver", "/Users/tobyfan/chromedriver/chromedriver");

    //         // 初始化 WebDriver
    //         driver = new ChromeDriver();

    //         // 打開搜尋結果頁面
    //         driver.get(url);

    //         WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    //         WebElement elements = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//b[.//span[@class='dg6jd']]")));

    //         // 找到相關搜尋的元素 (Google 通常將相關搜尋放在頁面底部)
    //         List<WebElement> relatedElements = driver.findElements(By.xpath("//span[@class='dg6jd']/ancestor::b"));




    //         // 提取每個相關搜尋的文字
    //         for (WebElement element : relatedElements) {
    //             String text = element.getText();
    //             if (!text.isEmpty()) {
    //                 relatedSearchResults.add(text);
    //             }
    //         }
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     } finally {
    //         if (driver != null) {
    //             driver.quit(); // 確保資源釋放
    //         }
    //     }
    //     System.out.println(relatedSearchResults);
    //     return relatedSearchResults;
    // }


    public LinkedHashMap<String, String> query() throws IOException {
        Map<String, Integer> resultScores = new ConcurrentHashMap<>();
        Map<String, String> urlMap = new ConcurrentHashMap<>();
        LinkedHashMap<String, String> sortedResults = new LinkedHashMap<>();
        Set<String> processedUrls = Collections.synchronizedSet(new HashSet<>());

        // 只抓取第一頁的搜尋結果
        String content = fetchContent(url);
        Document doc = Jsoup.parse(content);
        Elements lis = doc.select("div.kCrYT");

        // 建立執行緒池
        int threadCount = Math.min(20, Runtime.getRuntime().availableProcessors() * 2);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<Void>> futures = new ArrayList<>();

        // 處理每個搜尋結果
        for (Element li : lis) {
            try {
                String citeUrl = li.select("a").get(0).attr("href").replace("/url?q=", "");
                String cleanUrl = removeTrackingParameters(citeUrl);

                if (processedUrls.contains(cleanUrl)) continue; // 跳過已處理過的 URL
                processedUrls.add(cleanUrl);

                // 提交任務來處理主頁和子網頁
                futures.add(executor.submit(() -> {
                    try {
                        String title = li.select("a").get(0).select(".vvjwJb").text();
                        if (title.isEmpty()) return null;

                        // 計算主頁分數
                        int mainPageScore = calculatePageScore(cleanUrl);

                        // 計算子網頁分數
                        int subPageScore = 0;
                        List<String> subPages = fetchSubPages(cleanUrl);
                        for (String subPage : subPages) {
                            if (processedUrls.contains(subPage)) continue;
                            processedUrls.add(subPage);
                            subPageScore += calculatePageScore(subPage);
                        }

                        // 總分 = 主頁分數 + 子網頁分數
                        int totalScore = mainPageScore + subPageScore;

                        // 如果有有效分數，將結果存入 map
                        if (totalScore > 0) {
                            resultScores.put(title, totalScore);
                            urlMap.put(title, cleanUrl);
                            System.out.println(cleanUrl + " Total Score " + totalScore);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }));
            } catch (IndexOutOfBoundsException e) {
                continue;
            }
        }

        for (Future<Void> future : futures) {
            try {
                future.get(10, TimeUnit.SECONDS); // 設定超時為 10 秒
            } catch (TimeoutException e) {
                System.out.println("計算分數任務超時，跳過該頁面");
                future.cancel(true); // 中止超時的任務
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 關閉執行緒池
        executor.shutdown();

        // 按照分數排序
        resultScores.entrySet()
            .stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // 分數降序
            .forEach(entry -> sortedResults.put(entry.getKey(), urlMap.get(entry.getKey())));
        return sortedResults; // 返回排序後的結果
    }

    // 去除 URL 中的追蹤參數
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
