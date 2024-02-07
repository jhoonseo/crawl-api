package com.project.crawl.service.dto;

import java.util.List;

@lombok.Data
public class SearchData {
    public int page;
    public int page_size;
    public String total_count;
    public String keyword;
    public String sort;
    public String price_start;
    public String price_end;
    public List<KeywordItem> items;
}