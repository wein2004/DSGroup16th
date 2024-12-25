package google.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import google.model.Keyword;
import google.service.GoogleQuery;


@RestController
public class GoogleSearchController {

    

    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam String keyword) {
        Map<String, Object> response = new HashMap<>();

        List<Keyword> keywords = new ArrayList<>();  
        keywords.add(new Keyword("HBL", 12));  
        keywords.add(new Keyword("CPBL", 12)); 
        keywords.add(new Keyword("中華職棒", 12));
        keywords.add(new Keyword("PLG", 12)); 
        keywords.add(new Keyword("T1", 12));
        keywords.add(new Keyword("經典賽", 6)); 
        keywords.add(new Keyword("12強", 6));
        keywords.add(new Keyword("台灣籃球", 13));  
        keywords.add(new Keyword("台灣棒球", 14));  
        keywords.add(new Keyword("運動賽事", 8));  
        keywords.add(new Keyword("體育", 6));  
        keywords.add(new Keyword("台灣體育新聞", 10));  
        keywords.add(new Keyword("運動員", 7));  
        keywords.add(new Keyword("台灣奧運選手", 7));   
        keywords.add(new Keyword("台灣羽球", 8));  
        keywords.add(new Keyword("台灣游泳", 7));  
        keywords.add(new Keyword("台灣足球", 6));  
        keywords.add(new Keyword("台灣乒乓球", 6));  
        keywords.add(new Keyword("台灣武術", 5));  
        keywords.add(new Keyword("台灣滑雪", 4));  
        keywords.add(new Keyword("台灣馬拉松", 9));  
        keywords.add(new Keyword("國際賽事", 8));  
        keywords.add(new Keyword("台灣青少年運動", 6));  
        keywords.add(new Keyword("全國運動會", 5));  
        keywords.add(new Keyword("台灣健身", 7));  
        keywords.add(new Keyword("台灣運動明星", 11));  
        keywords.add(new Keyword("球隊文化", 7));  
        keywords.add(new Keyword("體育賽事直播", 8));  
        keywords.add(new Keyword("體育賽事票券", 6));  
        keywords.add(new Keyword("運動品牌", 5));  
        keywords.add(new Keyword("台灣健身房", 7));  
        keywords.add(new Keyword("運動醫學", 6));  
        keywords.add(new Keyword("運動科學", 5));  


        
        GoogleQuery googleQuery = new GoogleQuery(keyword, keywords);
        try {
            LinkedHashMap<String, String> mainResults = googleQuery.query();
            List<String> relatedSearches = googleQuery.relatedSearch();
            response.put("mainResults", mainResults);
            response.put("relatedSearches", relatedSearches);
        } catch (IOException e) {
            e.printStackTrace();
            // Return an error message in case of an exception
            
        }
        return response;
    }
}


