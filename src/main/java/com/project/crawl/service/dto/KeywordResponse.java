package com.project.crawl.service.dto;

import lombok.Data;

@Data
public class KeywordResponse {
    public int code;
    public String msg;
    public SearchData data;
}
