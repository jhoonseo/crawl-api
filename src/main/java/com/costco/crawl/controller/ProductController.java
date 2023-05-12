package com.costco.crawl.controller;

import com.costco.crawl.controller.dto.CostcoProduct;
import com.costco.crawl.service.CategoryService;
import com.costco.crawl.service.CostcoProductService;
import com.costco.crawl.service.CrawlService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
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
    public void renewProduct(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate crawlDate
    ) {
        // 0. 파라미터 체크
        costcoProductService.checkParams(crawlDate);

        // 1. 조건에 맞는 상품 가져오기
        List<CostcoProduct> costcoProductList = costcoProductService.getCostcoProductListAfterDate(crawlDate);

        // 2. 상품을 기준으로 상세 크롤링하여 상품 조회

    }


}
