package com.project.crawl.controller.dto;

import lombok.Data;

import java.util.List;

@Data
public class CategoryInfo {
    private String category;
    private Integer costcoCategoryIdx;
    private String url;
    private String categoryName;

    private int productItemCountPage = 0;

    public CategoryInfo(Integer categoryIdx, String category) {
        costcoCategoryIdx = categoryIdx;
        this.category = category;
    }

    public String getUrl(String pageParam) {
        return "https://www.costco.co.kr/c/" + category + "?sort=ratings-desc" + pageParam;
    }

    public void setUrl(String pageParam) {
        url =  "https://www.costco.co.kr/c/" + category + "?sort=ratings-desc" + pageParam;
    }

}
