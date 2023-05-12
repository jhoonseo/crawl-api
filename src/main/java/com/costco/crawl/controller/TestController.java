package com.costco.crawl.controller;

import com.costco.crawl.controller.dto.CostcoProduct;
import com.costco.crawl.service.CategoryService;
import com.costco.crawl.service.CostcoProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1/costco/crawl")
@Tag(name = "코스트코 크롤링")
public class TestController {
    private final CategoryService categoryService;
    private final CostcoProductService costcoProductService;

//    todo 배치잡 만들기

    @GetMapping("/test")
    public String test() {
        return "Test String Returned";
    }

    @GetMapping("/urlCategories")
    public List<String> categoryUrls() {
        return categoryService.getCostcoCategoryKeyList();
    }

    @GetMapping("/export")
    public void exportUpdateXlsx() {


    }

    @GetMapping("/testCostcoProduct")
    public CostcoProduct testCP(
            @RequestParam("productCode") Integer productCode
    ) {
        return costcoProductService.getCostcoProductByProductCode(productCode);
    }

    @GetMapping("/testCostcoProductIdx")
    public Integer testCPI(
            @RequestParam("productCode") Integer productCode
    ) {
        return costcoProductService.getCostcoProductIdxByProductCode(productCode);
    }

}
