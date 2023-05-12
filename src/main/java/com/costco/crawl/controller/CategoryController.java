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
        Map<Object, CostcoProduct> existingCostcoProductMap = costcoProductService.getAllCostcoProductMap();
        Set<Object> existingCostcoProductCodeSet = existingCostcoProductMap.keySet();
        Set<Object> crawledCostcoProductCodeSet = new HashSet<>();

        crawlService.create();
        categoryList.forEach(category -> {
            CategoryInfo categoryInfo = new CategoryInfo(category.getIdx(), category.getCategory());
            Set<CostcoProduct> newCostcoProductSet = new HashSet<>();
            for (String pageParam : pageParams) {
                categoryInfo.setUrl(pageParam);
                // 3. 카테고리 selenium 으로 크롤링
                try {
                    newCostcoProductSet.addAll(crawlService.crawlFromCategory(categoryInfo));
                } catch (Exception e) {
                    throw e;
                }

                // 4. 크롤링 데이터와 oldCostcoProductMap 과 대조
                if (!newCostcoProductSet.isEmpty()) {
                    newCostcoProductSet.forEach((newCostcoProduct) -> {
                        // TODO: check newCostcoProduct exists among oldCostcoProductMap and check whether it needs UPDATE or INSERT
                        Integer newProductCode = newCostcoProduct.getProductCode();
                        if (existingCostcoProductCodeSet.contains(newProductCode)) {
                            // product code already exists in oldCostcoProductMap
                            CostcoProduct existingCostcoProduct = existingCostcoProductMap.get(newProductCode);
                            // if update is needed, replace oldCostcoProductMap with newCostcoProduct
                            if (existingCostcoProduct.isRequiredUpdate(newCostcoProduct)) {
                                existingCostcoProductMap.replace(newProductCode, newCostcoProduct);
                            } else {
                                // else, update crawlDateTime only
                                existingCostcoProduct.setCrawlDateTime(commonService.getCurrentTimestamp());
                                existingCostcoProductMap.replace(newProductCode, existingCostcoProduct);
                            }
                            // 이후, 크롤링 되지 않은 상품의 집합을 구하기 위해 업데이트 된 상품 코드를 추가
                            crawledCostcoProductCodeSet.add(newProductCode);
                        } else {
                            // insert is needed
                            costcoProductService.insertCostcoProduct(newCostcoProduct);
                            // existingCostcoProductMap, existingCostcoProductCodeSet 에 newCostcoProduct 추가
                            existingCostcoProductMap.put(newProductCode, newCostcoProduct);
                            existingCostcoProductCodeSet.add(newProductCode);
                        }
                    });
                }
                // 총 크롤링 된 상품의 갯수를 구하기 위한 부분
                totalProductItems += categoryInfo.getProductItemCountPage();
                if (categoryInfo.getProductItemCountPage() < 48) {
                    break;
                }
            }
        });
        // TODO: existingCostcoProductCodeSet crawledCostcoProductCodeSet 이용하여
        //  crawledCostcoProductCodeSet 에 포함된 부분은 업데이트 진행하고,
        //  existingCostcoProductCodeSet 에서 crawledCostcoProductCodeSet 을 제외한 값은 status 를 0 으로 업데이트 하기

        // crawledCostcoProductCodeSet 에 포함된 부분은 업데이트 진행


        // existingCostcoProductCodeSet 에서 crawledCostcoProductCodeSet 을 제외한 값은 status 를 0 으로 업데이트 하기
        existingCostcoProductCodeSet.removeAll(crawledCostcoProductCodeSet);
        costcoProductService.updateCostcoProductsStatus(existingCostcoProductCodeSet, 0);
        crawlService.quit();
        return "renewed costco_product count is : " + totalProductItems;
    }
                /*
                CostcoProduct existCostcoProduct = costcoProductDao.getCostcoProductByProductCode(costcoProduct.getProductCode());
                if (Objects.isNull(existCostcoProduct)) {
                    costcoProductDao.insertCostcoProduct(costcoProduct);
                } else if (costcoProduct.isRequiredUpdate(existCostcoProduct)) {
                    costcoProductDao.updateInfoByIdx(existCostcoProduct.getIdx(), costcoProduct);
                } else {
                    costcoProductDao.updateCrawlDateTimeByIdx(costcoProduct.getIdx(), commonService.getCurrentTime());
                }
                 */

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
