package com.costco.crawl.controller;

import com.costco.crawl.service.CategoryService;
import com.costco.crawl.service.CostcoProductService;
import com.costco.crawl.service.CrawlService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1/costco/crawl/product")
@Tag(name = "코스트코 상품상세 크롤링")
public class ProductController {
    private final CategoryService categoryService;
    private final CostcoProductService costcoProductService;
    private final CrawlService crawlService;

    @PostMapping("/renew")
    public void renewProduct() {
        // 1. product_code
        List<Integer> costcoProductCodeList = costcoProductService.getAllCostcoProductCodeList();
        // 2. 상품을 기준으로 상세 크롤링하여 상품 조회

    }


}
