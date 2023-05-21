package com.costco.crawl.controller;

import com.costco.crawl.controller.dto.Category;
import com.costco.crawl.controller.dto.CategoryInfo;
import com.costco.crawl.controller.dto.CostcoProduct;
import com.costco.crawl.service.CategoryService;
import com.costco.crawl.service.CostcoProductService;
import com.costco.crawl.service.CrawlService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1/costco/crawl/category")
@Tag(name = "코스트코 카테고리 크롤링")
public class CategoryController {
    private final CategoryService categoryService;
    private final CostcoProductService costcoProductService;
    private final CrawlService crawlService;
    private int totalProductItems;
    private int totalUpdatedCategoryName;

    @GetMapping("/renew")
    public String renewCategory() {
        List<String> pageParams = List.of("", "&page=1", "&page=2", "&page=3", "&page=4", "&page=5");
        // 0. 상품의 갯수 변수
        totalProductItems = 0;
        // 1. 카테고리 불러오기
        List<Category> categoryList = categoryService.getCostcoCategoryList();
        // 2. 크롤링 데이터와 대조할 DB 데이터 가져오기
        List<Integer> dbCostcoProductCodeSet = costcoProductService.getAllCostcoProductCodeList();
        Set<Integer> updatedCostcoProductCodeSet = new HashSet<>();
        Set<Integer> insertCostcoProductCodeSet = new HashSet<>();
        Set<CostcoProduct> insertCostcoProductSet = new HashSet<>();

        crawlService.create();
        categoryList.forEach(category -> {
            CategoryInfo categoryInfo = new CategoryInfo(category.getIdx(), category.getCategory());
            Set<CostcoProduct> crawledCostcoProductSet = new HashSet<>();

            // 3. 카테고리 selenium 으로 크롤링
            for (String pageParam : pageParams) {
                categoryInfo.setUrl(pageParam);
                try {
                    crawledCostcoProductSet.addAll(crawlService.crawlFromCategory(categoryInfo));
                } catch (Exception e) {
                    throw e;
                }

                // 총 크롤링 된 상품의 갯수를 구하기 위한 부분
                totalProductItems += categoryInfo.getProductItemCountPage();
                if (categoryInfo.getProductItemCountPage() < 48) {
                    break;
                }
            }

            if (crawledCostcoProductSet.isEmpty()) {
                return;
            }

            // 4. check crawledCostcoProduct exists among costcoProductMap and check whether it needs UPDATE or INSERT
            crawledCostcoProductSet.forEach((crawledCostcoProduct) -> {
                Integer crawledProductCode = crawledCostcoProduct.getProductCode();
                // already in db
                if (dbCostcoProductCodeSet.contains(crawledProductCode)) {
                    if (updatedCostcoProductCodeSet.contains(crawledProductCode) || insertCostcoProductCodeSet.contains(crawledProductCode)) {
                        return;
                    }
                    // update 진행
                    costcoProductService.updateCostcoProduct(crawledCostcoProduct);
                    // updatedCodeSet 에 추가
                    updatedCostcoProductCodeSet.add(crawledProductCode);
                } else {
                    // costcoProductService.insertCostcoProduct(crawledCostcoProduct); // 방법1
                    // insertSet 에 추가
                    insertCostcoProductSet.add(crawledCostcoProduct); // 방법2
                    // dbCodeSet && insertCodeSet 에 추가
                    dbCostcoProductCodeSet.add(crawledProductCode);
                    insertCostcoProductCodeSet.add(crawledProductCode);
                }
            });
        });

        // insert 일괄 진행
        costcoProductService.insertCostcoProductSet(insertCostcoProductSet); // 방법2

        // dbSet - (updatedSet + insertingSet) 은 disable (품절 상품은 카테고리에서 노출되지 않아, dbSet 에만 존재)
        dbCostcoProductCodeSet.removeAll(updatedCostcoProductCodeSet);
        dbCostcoProductCodeSet.removeAll(insertCostcoProductCodeSet);
        // disable 진행
        costcoProductService.updateCostcoProductListStatus(dbCostcoProductCodeSet, 0);

        crawlService.quit();
        return "renewed costco_product count is : " + totalProductItems;
    }

    @GetMapping("/rename")
    public String renameCategory() {
        totalUpdatedCategoryName = 0;
        // 1. 카테고리 불러오기
        List<Category> categoryList = categoryService.getAllCostcoCategoryList();

        crawlService.create();
        categoryList.forEach(category -> {
            try {
                crawlService.crawlCategoryName(category);
            } catch (Exception e) {
                throw e;
            }

            if (!category.getName().isBlank()) {
                totalUpdatedCategoryName += categoryService.updateCostcoCategoryName(category);
            }
        });

        crawlService.quit();
        return "updated costco_category's name column count is : " + totalUpdatedCategoryName;
    }
}
