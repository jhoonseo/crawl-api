package com.project.crawl.controller;

import com.project.crawl.controller.dto.C24Code;
import com.project.crawl.controller.dto.C24CostcoProduct;
import com.project.crawl.controller.dto.C24CostcoProductGroup;
import com.project.crawl.service.C24ProductService;
import com.project.crawl.service.CostcoProductService;
import com.project.crawl.service.CrawlService;
import com.project.crawl.util.CommonUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1/project/crawl/product")
@Tag(name = "코스트코 상품상세 크롤링")
public class ProductController {
    private final CostcoProductService costcoProductService;
    private final CrawlService crawlService;
    private final C24ProductService c24ProductService;
    private final CommonUtil commonUtil;

    @GetMapping("/renew")
    public void renewProduct() throws IOException {
        // <c24_product 에만 존재하고, costco_product 에는 존재하지 않는 costco_product_code 는 없다고 가정>
        // Available C24CostcoProducts : costco_product.status==1
        List<C24CostcoProduct> c24CostcoProductList = c24ProductService.getAvailableC24CostcoProductList();

        // retrieve latest C24Code
        String lastC24Code = c24ProductService.getLastC24Code();
        C24Code c24Code = new C24Code();
        c24Code.setCharsByCode(lastC24Code);

        // directory 체크 && 만들기
        LocalDate today = LocalDate.now();
        String formatToday = today.format(DateTimeFormatter.ofPattern("MMdd"));
        commonUtil.generateDailyDirectories(formatToday);

        // c24_product 정보가 1. 없는 경우(새상품), 2. 누락된 경우(크롤링 이슈), 3. 정상의 경우 에 따라서 알맞게 처리

        // 새 상품
        Set<C24CostcoProduct> newC24CostcoProductsSet = c24CostcoProductList.stream()
                .filter(p -> p.getC24Idx() == 0)
                .collect(Collectors.toSet());

        // 새 상품 crawl && set new C24Code -> insert or disable
        crawlService.setDriverProperty();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait webDriverWait = new WebDriverWait(driver, Duration.ofSeconds(10));

        newC24CostcoProductsSet.forEach(c24CostcoProduct -> {

            // 새 상품 crawl
            try {
                crawlService.crawlProduct(driver, webDriverWait, c24CostcoProduct, formatToday);
                c24ProductService.manageC24Code(c24Code);
            } catch (Exception e) {
                crawlService.quit(driver);
                throw new RuntimeException(e);
            }

            // set new C24Code
            c24CostcoProduct.setC24Code(c24Code.getC24Code());

            // c24_product 에 insert
            c24ProductService.insertC24Product(c24CostcoProduct);

            // c24CostcoProduct.getC24Code() == 0 인 경우, costco_product.status 를 0 으로 업데이트
            if (c24CostcoProduct.getC24Status() == 0) {
                costcoProductService.updateCostcoProductStatus(c24CostcoProduct.getProductCode(), 0);
            }
        });

        // 기존 상품
        // c24Idx 가 0이 아니고, c24Code 가 있는 c24CostcoProduct 를 productCode 로 grouping
        Map<Integer, List<C24CostcoProduct>> existingC24CostcoProductsMap = c24CostcoProductList.stream()
                .filter(p -> p.getC24Idx() != 0)
                .filter(p -> (!Objects.isNull(p.getC24Code()) && !p.getC24Code().isEmpty()))
                .collect(Collectors.groupingBy(C24CostcoProduct::getProductCode));

        // c24CostcoProductList 더 이상 사용하지 않으므로 초기화
        c24CostcoProductList = null;

        existingC24CostcoProductsMap.forEach((productCode, c24List) -> {
            // 객체가 서로 일치하는지 && 객체에서 필수 정보가 빠진게 없는지 체크
            boolean isSameObjects = c24ProductService.checkForSameObjects(c24List);
            boolean hasMustAttributes = c24List.get(0).checkForMustAttributes();

            // false 의 경우, List<c24_product.idx>, C24CostcoProduct 를 속성으로 가지는 클래스에 List<c24_product.idx> 를 셋 하여 return
            if (!(isSameObjects && hasMustAttributes)) {
                C24CostcoProductGroup c24Group = new C24CostcoProductGroup();

                c24Group.setProductCode(productCode);
                C24CostcoProduct c24CostcoProduct;
                // crawl product
                try {
                    c24CostcoProduct = crawlService.crawlProduct(driver, webDriverWait, productCode, formatToday);
                } catch (Exception e) {
                    crawlService.quit(driver);
                    throw new RuntimeException(e);
                }
                c24Group.setCommonC24CostcoProduct(c24CostcoProduct);

                // update with c24Group
                c24ProductService.updateC24Group(c24Group);
            }
        });
        crawlService.quit(driver);

        // disabling(update c24_product.status 0) : (c24_product.status == 1 && costco_product.status == 0)
        List<Integer> disablingIdxList = c24ProductService.getDisablingIdxList();
        c24ProductService.updateStatusByIdxList(disablingIdxList, 0);
    }


}
