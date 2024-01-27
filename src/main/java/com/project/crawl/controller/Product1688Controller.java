package com.project.crawl.controller;

import com.project.crawl.controller.dto.*;
import com.project.crawl.exceptions.CrawlException;
import com.project.crawl.service.C24Product1688Service;
import com.project.crawl.service.CostcoProductService;
import com.project.crawl.service.CrawlService;
import com.project.crawl.util.CommonUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/crawl/product-1688")
@Tag(name = "1688 상품상세 크롤링")
public class Product1688Controller {
    private final CostcoProductService costcoProductService;
    private final CrawlService crawlService;
    private final C24Product1688Service c24Product1688Service;
    private final CommonUtil commonUtil;

    @PostMapping("/renew")
    public void renewProduct(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate today,
            @RequestParam("productCode") Integer productCode
    ) {

        List<C241688Product> c241688ProductList = c24Product1688Service.getAvailableC241688ProductList();

        // retrieve latest C24Code
        String lastC24Code = c24Product1688Service.getLastC24Code();
        C24Code c24Code = new C24Code();
        c24Code.setCharsByCode(lastC24Code);

        // directory 체크 && 만들기
        String formatToday = today.format(DateTimeFormatter.ofPattern("MMdd"));
        try {
            commonUtil.generateDailyDirectories(formatToday);
        } catch (IOException e) {
            throw new CrawlException(CrawlException.Type.BAD_REQUEST, String.format("directory creation failed. %s", e.getMessage()));
        }

        // c24_product 정보가 1. 없는 경우(새상품), 2. 누락된 경우(크롤링 이슈), 3. 정상의 경우 에 따라서 알맞게 처리

        // 새 상품 | productCode 를 기준으로 오름차순 정렬
        List<C24CostcoProduct> newC24CostcoProductList = c24CostcoProductList.stream()
                .filter(p -> p.getC24Idx() == 0)
                .sorted(Comparator.comparing(C24CostcoProduct::getProductCode))
                .toList();

        // 기존 상품 | productCode key 를 기준으로 오름차순으로 정렬
        // c24Idx 가 0이 아니고, c24Code 가 있는 c24CostcoProduct 를 productCode 로 grouping
        Map<Integer, List<C24CostcoProduct>> existingC24CostcoProductsMap = c24CostcoProductList.stream()
                .filter(p -> p.getC24Idx() != 0)
                .filter(p -> (!Objects.isNull(p.getC24Code()) && !p.getC24Code().isEmpty()))
                .sorted(Comparator.comparing(C24CostcoProduct::getProductCode))
                .collect(Collectors.groupingBy(C24CostcoProduct::getProductCode,
                        LinkedHashMap::new, Collectors.toList()));

        // c24CostcoProductList 더 이상 사용하지 않으므로 초기화
        c24CostcoProductList = null;
        // Exception 발생한 상품을 Group 으로 관리하기 위한 List
        List<C24CostcoProductExceptionGroup> c24ExceptionGroupList = new ArrayList<>();

        // WebDriver 설정
        crawlService.setDriverProperty();
        WebDriver driver = crawlService.createWebDriver();
        WebDriverWait webDriverWait = crawlService.createWebDriverWait(driver, 10);

        // 새 상품 crawl && set new C24Code -> insert or disable
        try {
            for (C24CostcoProduct c24CostcoProduct : newC24CostcoProductList) {
                try {
                    crawlService.crawlProduct(driver, webDriverWait, c24CostcoProduct, formatToday);
                } catch (Exception e) {
                    if (e instanceof CrawlException && ((CrawlException) e).getType() == CrawlException.Type.FORBIDDEN) {
                        // CrawlException.Type.FORBIDDEN 일 경우, 그대로 throw
                        throw e;
                    }
                    C24CostcoProductExceptionGroup c24ExceptionGroup = new C24CostcoProductExceptionGroup(c24CostcoProduct.getProductCode(), true);
                    c24ExceptionGroupList.add(c24ExceptionGroup);
                    continue;
                }
                c24Product1688Service.manageC24Code(c24Code);
                c24CostcoProduct.setC24Code(c24Code.getC24Code());

                c24Product1688Service.insertC24Product(c24CostcoProduct);

                // c24CostcoProduct.getC24Code() == 0 인 경우, costco_product.status 를 0 으로 업데이트
                if (c24CostcoProduct.getC24Status() == 0) {
                    costcoProductService.updateCostcoProductStatus(c24CostcoProduct.getProductCode(), 0);
                }
            }

            for (Map.Entry<Integer, List<C24CostcoProduct>> entry : existingC24CostcoProductsMap.entrySet()) {
                Integer productCode = entry.getKey();
                List<C24CostcoProduct> c24List = entry.getValue();

                boolean areObjectsSame = c24Product1688Service.checkForSameObjects(c24List);
                boolean checkRandomMustAttributes = c24List.get(0).checkForC24ProductMustAttributes();
                boolean isStatusUpdateRequired = c24List.get(0).isC24StatusDisabled() && c24List.get(0).getIsOption() == 0;

                if (!(areObjectsSame && checkRandomMustAttributes)) {
                    C24CostcoProductGroup c24Group = new C24CostcoProductGroup();
                    c24Group.setProductCode(productCode);
                    C24CostcoProduct c24CostcoProduct;
                    try {
                        c24CostcoProduct = crawlService.crawlProduct(driver, webDriverWait, productCode, formatToday);
                    } catch (Exception e) {
                        if (e instanceof CrawlException && ((CrawlException) e).getType() == CrawlException.Type.FORBIDDEN) {
                            // CrawlException.Type.FORBIDDEN 일 경우, 그대로 throw
                            throw e;
                        }
                        C24CostcoProductExceptionGroup c24ExceptionGroup = new C24CostcoProductExceptionGroup(productCode, false);
                        c24ExceptionGroupList.add(c24ExceptionGroup);
                        continue;
                    }

                    // update 전 필수 속성 검사하여 status setC24Status 변경 여부 결정
                    if (!c24CostcoProduct.checkForC24ProductMustAttributes()) {
                        c24CostcoProduct.setC24Status(0);
                    }
                    c24Group.setCommonC24CostcoProduct(c24CostcoProduct);
                    c24Product1688Service.updateC24Group(c24Group);
                } else if (isStatusUpdateRequired) {
                    c24Product1688Service.updateStatusByProductCode(productCode, 1);
                }
            }

            // Exception 발생 c24CostcoProduct 에 대해서 다시 crawl 시도 후, 에러 발생할 경우 상품 비활성화
            for (C24CostcoProductExceptionGroup c24ExceptionGroup : c24ExceptionGroupList) {
                // insert
                if (c24ExceptionGroup.isInsert()) {
                    C24CostcoProduct c24CostcoProduct;
                    try {
                        c24CostcoProduct = crawlService.crawlProduct(driver, webDriverWait, c24ExceptionGroup.getProductCode(), formatToday);
                    } catch (Exception e) {
                        if (e instanceof CrawlException && ((CrawlException) e).getType() == CrawlException.Type.FORBIDDEN) {
                            // CrawlException.Type.FORBIDDEN 일 경우, 그대로 throw
                            throw e;
                        }
                        continue;
                    }
                    c24Product1688Service.manageC24Code(c24Code);
                    c24CostcoProduct.setC24Code(c24Code.getC24Code());

                    c24Product1688Service.insertC24Product(c24CostcoProduct);
                    // update
                } else {
                    C24CostcoProduct c24CostcoProduct;
                    try {
                        c24CostcoProduct = crawlService.crawlProduct(driver, webDriverWait, c24ExceptionGroup.getProductCode(), formatToday);
                    } catch (Exception e) {
                        if (e instanceof CrawlException && ((CrawlException) e).getType() == CrawlException.Type.FORBIDDEN) {
                            // CrawlException.Type.FORBIDDEN 일 경우, 그대로 throw
                            throw e;
                        }
                        c24Product1688Service.updateStatusByProductCode(c24ExceptionGroup.getProductCode(), 0);
                        continue;
                    }

                    // update 전 필수 속성 검사하여 status setC24Status 변경 여부 결정
                    if (!c24CostcoProduct.checkForC24ProductMustAttributes()) {
                        c24CostcoProduct.setC24Status(0);
                    }
                    c24ExceptionGroup.setCommonC24CostcoProduct(c24CostcoProduct);
                    c24Product1688Service.updateC24Group(c24ExceptionGroup);
                }
            }
        } catch (Exception e) {
            throw new CrawlException(CrawlException.Type.BAD_REQUEST, e.getMessage());
        } finally {
            driver.quit();
        }

        // disabling(update c24_product.status 0) : (c24_product.status == 1 && costco_product.status == 0)
        List<Integer> disablingIdxList = c24Product1688Service.getDisablingIdxList();
        c24Product1688Service.updateStatusByIdxList(disablingIdxList, 0);
    }


}
