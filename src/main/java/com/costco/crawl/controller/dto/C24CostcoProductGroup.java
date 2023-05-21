package com.costco.crawl.controller.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Data
public class C24CostcoProductGroup {
    private int productCode;
    private C24CostcoProduct commonC24CostcoProduct;
}
