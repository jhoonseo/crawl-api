package com.project.crawl.controller;

import com.project.crawl.controller.dto.Category;
import com.project.crawl.controller.dto.CategoryInfo;
import com.project.crawl.controller.dto.CostcoProduct;
import com.project.crawl.service.CategoryService;
import com.project.crawl.service.CostcoProductService;
import com.project.crawl.service.CrawlService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/crawl/category")
@Tag(name = "코스트코 카테고리 크롤링")
public class CategoryController {
    private final CategoryService categoryService;
    private final CostcoProductService costcoProductService;
    private final CrawlService crawlService;

    @GetMapping("/renew")
    public String renewCategory() {
        List<String> pageParams = List.of("", "&page=1", "&page=2", "&page=3", "&page=4", "&page=5");
        // 0. 상품의 갯수 변수
        int totalProductItems = 0;
        // 1. 카테고리 불러오기
        List<Category> categoryList = categoryService.getCostcoCategoryList();
        // 2. 크롤링 데이터와 대조할 DB 데이터 가져오기
        List<Integer> dbCostcoProductCodeList = costcoProductService.getAllCostcoProductCodeList();
        Set<Integer> updatedCostcoProductCodeSet = new HashSet<>();
        Set<Integer> insertCostcoProductCodeSet = new HashSet<>();
        HashMap<Integer, CostcoProduct> insertCostcoProductMap = new HashMap<>();

        // WebDriver 설정
        crawlService.setDriverProperty();
        WebDriver driver = crawlService.createWebDriver();
        WebDriverWait webDriverWait = crawlService.createWebDriverWait(driver, 10);

        for (Category category : categoryList) {
            CategoryInfo categoryInfo = new CategoryInfo(category.getIdx(), category.getCategory());
            Map<Integer, CostcoProduct> crawledCostcoProductMap = new HashMap<>();

            // 3. 카테고리 selenium 으로 크롤링
            for (String pageParam : pageParams) {
                categoryInfo.setUrl(pageParam);
                Set<CostcoProduct> pageCostcoProductSet = crawlService.crawlFromCategory(driver, webDriverWait, categoryInfo);
                Map<Integer, CostcoProduct> pageCostcoProductMap = pageCostcoProductSet.stream()
                        .collect(Collectors.toMap(CostcoProduct::getProductCode, Function.identity()));
                crawledCostcoProductMap.putAll(pageCostcoProductMap);

                // 총 크롤링 된 상품의 갯수를 구하기 위한 부분
                int pageItemCount = categoryInfo.getProductItemCountPage();
                totalProductItems += pageItemCount;
                if (pageItemCount < 48) {
                    break;
                }
            }

            if (crawledCostcoProductMap.isEmpty()) {
                continue;
            }

            // 4. check crawledCostcoProduct exists among costcoProductMap and check whether it needs UPDATE or INSERT
            crawledCostcoProductMap.forEach((crawledProductCode, crawledCostcoProduct) -> {
                boolean isAlreadyInDB = dbCostcoProductCodeList.contains(crawledProductCode);
                boolean isAlreadyProcessed = updatedCostcoProductCodeSet.contains(crawledProductCode) || insertCostcoProductCodeSet.contains(crawledProductCode);
                if (isAlreadyInDB && !isAlreadyProcessed) {
                    // update 진행
                    costcoProductService.updateCostcoProduct(crawledCostcoProduct);
                    // updatedCodeSet 에 추가
                    updatedCostcoProductCodeSet.add(crawledProductCode);
                } else if (!isAlreadyInDB && !isAlreadyProcessed) {
                    // insertMap 에 추가, crawledCostcoProduct 가 카테고리에 중복 존재할 수 있음
                    insertCostcoProductMap.put(crawledProductCode, crawledCostcoProduct);
                    // dbCodeSet && insertCodeSet 에 추가
                    dbCostcoProductCodeList.add(crawledProductCode);
                    insertCostcoProductCodeSet.add(crawledProductCode);
                }
            });
        }

        // insert 일괄 진행
        costcoProductService.insertCostcoProductCollection(insertCostcoProductMap.values());

        // dbSet - (updatedSet + insertSet) 은 disable (품절 상품은 카테고리에서 노출되지 않아, dbSet 에만 존재)
        dbCostcoProductCodeList.removeAll(updatedCostcoProductCodeSet);
        dbCostcoProductCodeList.removeAll(insertCostcoProductCodeSet);
        // disable 진행
        costcoProductService.updateCostcoProductListStatus(dbCostcoProductCodeList, 0);

        driver.quit();
        return "renewed costco_product count is : " + totalProductItems;
    }

    @GetMapping("/rename")
    public String renameCategory() {
        int totalUpdatedCategoryName = 0;
        // 1. 카테고리 불러오기
        List<Category> categoryList = categoryService.getAllCostcoCategoryList();

        // WebDriver 설정
        crawlService.setDriverProperty();
        WebDriver driver = crawlService.createWebDriver();
        WebDriverWait webDriverWait = crawlService.createWebDriverWait(driver, 10);

        for (Category category : categoryList) {
            crawlService.crawlCategoryName(driver, webDriverWait, category);

            if (!category.getName().isBlank()) {
                totalUpdatedCategoryName += categoryService.updateCostcoCategoryName(category);
            }
        }

        driver.quit();
        return "updated costco_category's name column count is : " + totalUpdatedCategoryName;
    }
}
