package com.costco.crawl.controller;

import com.costco.crawl.controller.dto.Category;
import com.costco.crawl.controller.dto.CategoryInfo;
import com.costco.crawl.controller.dto.CostcoProduct;
import com.costco.crawl.service.CategoryService;
import com.costco.crawl.service.CommonService;
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
    private final CommonService commonService;
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
        Map<Object, CostcoProduct> costcoProductMap = costcoProductService.getAllCostcoProductMap();
        Set<Object> costcoProductCodeSet = costcoProductMap.keySet();
        Set<Integer> updatingCostcoProductCodeSet = new HashSet<>();
        Set<Integer> insertingCostcoProductCodeSet = new HashSet<>();

        crawlService.create();
        categoryList.forEach(category -> {
            CategoryInfo categoryInfo = new CategoryInfo(category.getIdx(), category.getCategory());
            Set<CostcoProduct> newCostcoProductSet = new HashSet<>();

            // 3. 카테고리 selenium 으로 크롤링
            for (String pageParam : pageParams) {
                categoryInfo.setUrl(pageParam);
                try {
                    newCostcoProductSet.addAll(crawlService.crawlFromCategory(categoryInfo));
                } catch (Exception e) {
                    throw e;
                }

                // 총 크롤링 된 상품의 갯수를 구하기 위한 부분
                totalProductItems += categoryInfo.getProductItemCountPage();
                if (categoryInfo.getProductItemCountPage() < 48) {
                    break;
                }
            }

            if (newCostcoProductSet.isEmpty()) {
                return;
            }

            // 4. newCostcoProductSet 와 costcoProductMap 과 대조
            newCostcoProductSet.forEach((newCostcoProduct) -> {
                // TODO: check newCostcoProduct exists among costcoProductMap and check whether it needs UPDATE or INSERT
                Integer newProductCode = newCostcoProduct.getProductCode();
                if (costcoProductCodeSet.contains(newProductCode)) {
                    // product code already exists in costcoProductMap
                    CostcoProduct existingCostcoProduct = costcoProductMap.get(newProductCode);
                    // if update is needed, replace costcoProductMap with newCostcoProduct
                    if (existingCostcoProduct.isRequiredUpdate(newCostcoProduct)) {
                        costcoProductMap.replace(newProductCode, newCostcoProduct);
                    } else {
                        // else, update crawlDateTime only
                        existingCostcoProduct.setCrawlDateTime(commonService.getCurrentTimestamp());
                        costcoProductMap.replace(newProductCode, existingCostcoProduct);
                    }
                    // 이후, 크롤링 되지 않은 상품의 집합을 구하기 위해 업데이트 된 상품 코드를 추가
                    updatingCostcoProductCodeSet.add(newProductCode);
                } else {
                    // insert is needed
                    insertingCostcoProductCodeSet.add(newProductCode);
                    // costcoProductMap, costcoProductCodeSet 에 newCostcoProduct 추가
                    costcoProductMap.put(newProductCode, newCostcoProduct);
                    costcoProductCodeSet.add(newProductCode);
                }
            });
        });

        // TODO: existingCostcoProductCodeSet crawledCostcoProductCodeSet 이용하여
        //  updatingCostcoProductCodeSet 에 포함된 부분은 update,
        //  insertingCostcoProductCodeSet 에 포함된 부분은 insert,
        //  이외의 경우에 status 가 1 인 경우 disable
        costcoProductMap.forEach((k, v) -> {
            if (updatingCostcoProductCodeSet.contains(k)) {
                // update
                costcoProductService.updateCostcoProduct(v);
            } else if (insertingCostcoProductCodeSet.contains(k)) {
                // insert
                costcoProductService.insertCostcoProduct(v);
            } else if (v.getStatus() == 1){
                // disable costcoProduct
                costcoProductService.updateCostcoProductStatus(k, 0);
            }
        });

        crawlService.quit();
        return "renewed costco_product count is : " + totalProductItems;
    }

    @GetMapping("/rename")
    public String renameCategory() {
        List<String> pageParams = List.of("", "&page=1", "&page=2", "&page=3", "&page=4", "&page=5");
        totalUpdatedCategoryName = 0;
        // 1. 카테고리 불러오기
        List<Category> categoryList = categoryService.getCostcoCategoryList();

        crawlService.create();
        categoryList.forEach(category -> {
            try {
                crawlService.crawlCategoryName(category);
            } catch (Exception e) {
                throw e;
            }
            totalUpdatedCategoryName += categoryService.updateCostcoCategoryName(category);
        });

        crawlService.quit();
        return "updated costco_category's name column count is : " + totalUpdatedCategoryName;
    }
}
