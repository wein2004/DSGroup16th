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


