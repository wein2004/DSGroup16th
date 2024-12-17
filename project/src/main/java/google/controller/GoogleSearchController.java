package google.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import google.model.Keyword;
import google.service.GoogleQuery;

@RestController
public class GoogleSearchController {

    @GetMapping("/search")
    public Map<String, String> search(@RequestParam String keyword) {

        // List<Keyword> keywords = new ArrayList<>();
        //     keywords.add(new Keyword("台灣", 1.5));  // 關鍵字 "台灣" 並設置權重 1.5
        //     keywords.add(new Keyword("HBL", 2.0));   // 關鍵字 "HBL" 並設置權重 2.0
        //     keywords.add(new Keyword("CPBL", 2.5));  // 關鍵字 "CPBL" 並設置權重 2.5
        //     keywords.add(new Keyword("籃球", 1.2));  // 關鍵字 "籃球" 並設置權重 1.2
        //     keywords.add(new Keyword("棒球", 1.8));  // 關鍵字 "棒球" 並設置權重 1.8
        //     keywords.add(new Keyword("運動", 1.0));  // 關鍵字 "運動" 並設置權重 1.0
        //     keywords.add(new Keyword("比賽", 1.3));  // 關鍵字 "比賽" 並設置權重 1.3
        //     keywords.add(new Keyword("選手", 1.7));  // 關鍵字 "選手" 並設置權重 1.7
        //     keywords.add(new Keyword("台北", 1.4));  // 關鍵字 "台北" 並設置權重 1.4
        //     keywords.add(new Keyword("體育", 1.6));  // 關鍵字 "體育" 並設置權重 1.6

        
        GoogleQuery googleQuery = new GoogleQuery(keyword);
        try {
            return googleQuery.query();
        } catch (IOException e) {
            e.printStackTrace();
            // Return an error message in case of an exception
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch search results");
            return errorResponse;
        }
    }
}