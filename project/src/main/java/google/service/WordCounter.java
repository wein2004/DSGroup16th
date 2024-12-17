package google.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class WordCounter {
    private String urlStr;
    private String content;

    public WordCounter(String urlStr) {
        this.urlStr = urlStr;
    }

    private String fetchContent() throws IOException {
        URL url = new URL(this.urlStr);
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36");
    
        try (InputStream in = conn.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
    
            StringBuilder retVal = new StringBuilder();
            String line;
    
            while ((line = br.readLine()) != null) {
                line = line.replaceAll("[^\\x00-\\x7F]", ""); // 過濾非 ASCII 字符
                retVal.append(line).append("\n");
            }
            return retVal.toString();
    
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + this.urlStr);
            return ""; // 返回空內容
        } catch (IOException e) {
            System.out.println("Error fetching content: " + e.getMessage());
            throw e; // 重新拋出以便調試其他問題
        }
    }
    

    public int BoyerMoore(String T, String P) {
        int n = T.length();
        int m = P.length();
        if (m == 0) return 0; // 關鍵字為空時直接返回 0

        int[] L = new int[256]; // 支援 ASCII 字符
        for (int k = 0; k < 256; k++) {
            L[k] = -1;
        }

        for (int t = 0; t < m; t++) {
            L[P.charAt(t)] = t;
        }

        int i = m - 1; // 文本指針
        int j = m - 1; // 模式指針

        while (i < n) {
            if (T.charAt(i) == P.charAt(j)) {
                if (j == 0) {
                    return i; // 匹配成功
                }
                i--;
                j--;
            } else {
                int l = L[T.charAt(i)];
                i += m - Math.min(j, 1 + l);
                j = m - 1;
            }
        }
        return -1; // 匹配失敗
    }

    public int countKeyword(String keyword) throws IOException {
        if (content == null) {
            content = fetchContent();
        }

        // 將內容與關鍵字轉換為大寫，進行不區分大小寫的搜尋
        String upperContent = content.toUpperCase();
        String upperKeyword = keyword.toUpperCase();

        int count = 0;
        int position = 0;

        // 使用 Boyer-Moore 演算法計算關鍵字次數
        while ((position = BoyerMoore(upperContent, upperKeyword)) != -1) {
            count++;
            position += upperKeyword.length(); // 移動到下一個搜尋起點
            upperContent = upperContent.substring(position); // 避免重新創建大字串
        }
        return count;
    }
}
