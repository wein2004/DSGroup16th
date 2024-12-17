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
             BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"))) { // 使用UTF-8編碼處理中文等字符
    
            StringBuilder retVal = new StringBuilder();
            String line;
    
            while ((line = br.readLine()) != null) {
                retVal.append(line).append("\n"); // 保留所有字符
            }
            return retVal.toString();
    
        } catch (FileNotFoundException e) {
            return ""; // 返回空內容
        } catch (IOException e) {
            throw e; // 重新拋出以便調試其他問題
        }
    }
    

    public int BoyerMoore(String T, String P) {
        int n = T.length();
        int m = P.length();
        if (m == 0) return 0; // 關鍵字為空時直接返回 0

        int[] L = new int[Character.MAX_VALUE]; // 支援所有Unicode字符
        for (int k = 0; k < Character.MAX_VALUE; k++) {
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
    
        // 使用UTF-8編碼，並進行不區分大小寫的搜尋
        String upperContent = content.toLowerCase();  // 使用小寫來進行不區分大小寫的比較
        String upperKeyword = keyword.toLowerCase();
    
        int count = 0;
        int position = 0;
    
        // 使用 Boyer-Moore 演算法計算關鍵字次數
        while (position < upperContent.length()) {
            int foundAt = BoyerMoore(upperContent.substring(position), upperKeyword);  // 從當前位置開始搜尋
            if (foundAt == -1) {
                break;  // 沒有找到則退出循環
            }
            
            count++;  // 找到一個匹配
            position += foundAt + upperKeyword.length();  // 移動到下一個搜尋起點
        }
    
        return count;
    }
    
}
