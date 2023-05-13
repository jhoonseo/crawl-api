package com.costco.crawl.controller.dto;

import lombok.Data;

@Data
public class Category {
    private Integer idx;
    private String category;
    private String name = "";
}
