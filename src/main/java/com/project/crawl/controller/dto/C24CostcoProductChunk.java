package com.project.crawl.controller.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Data
public class C24CostcoProductChunk {
    private List<C24CostcoProductXlsx> c24CostcoProductList;
    private Integer startIndex;
    private Integer endIndex;
}
