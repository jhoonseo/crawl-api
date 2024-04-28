package com.project.crawl.controller;

import com.project.crawl.controller.dto.*;
import com.project.crawl.exceptions.CrawlException;
import com.project.crawl.service.C24ProductService;
import com.project.crawl.service.ProductService;
import com.project.crawl.service.CrawlService;
import com.project.crawl.util.CommonUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/product")
@Tag(name = "상품상세 크롤링 컨트롤러")
public class CrawlProductController {
    private final ProductService productService;
    private final CrawlService crawlService;
    private final C24ProductService c24ProductService;
    private final CommonUtil commonUtil;

    @PostMapping("/crawl-costco")
    public void renewCostcoProduct(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate today
    ) {
        // <c24_product 에만 존재하고, costco_product 에는 존재하지 않는 costco_product_code 는 없다고 가정>
        // Available C24CostcoProducts : costco_product.status==1
        List<C24Product> c24ProductList = c24ProductService.getAvailableC24ProductCostcoList();

        // retrieve latest C24Code
        String lastC24Code = c24ProductService.getLastC24CodeCostco();
        C24Code c24Code = new C24Code();
        c24Code.setCharsByCode(lastC24Code);

        // directory 체크 && 만들기
        String formatToday = today.format(DateTimeFormatter.ofPattern("MMdd"));
        try {
            commonUtil.generateDailyDirectoriesCostco(formatToday);
        } catch (IOException e) {
            throw new CrawlException(CrawlException.Type.BAD_REQUEST, String.format("directory creation failed. %s", e.getMessage()));
        }

        // c24_product 정보가 1. 없는 경우(새상품), 2. 누락된 경우(크롤링 이슈), 3. 정상의 경우 에 따라서 알맞게 처리

        // 새 상품 | productCode 를 기준으로 오름차순 정렬
        List<C24Product> newC24ProductList = c24ProductList.stream()
                .filter(p -> p.getC24Idx() == 0)
                .sorted(Comparator.comparing(C24Product::getProductCode))
                .toList();

        // 기존 상품 | productCode key 를 기준으로 오름차순으로 정렬
        // c24Idx 가 0이 아니고, c24Code 가 있는 c24CostcoProduct 를 productCode 로 grouping
        Map<Long, List<C24Product>> existingC24ProductsMap = c24ProductList.stream()
                .filter(p -> p.getC24Idx() != 0)
                .filter(p -> (!Objects.isNull(p.getC24Code()) && !p.getC24Code().isEmpty()))
                .sorted(Comparator.comparing(C24Product::getProductCode))
                .collect(Collectors.groupingBy(C24Product::getProductCode,
                        LinkedHashMap::new, Collectors.toList()));

        // c24ProductList 더 이상 사용하지 않으므로 초기화
        c24ProductList = null;
        // Exception 발생한 상품을 Group 으로 관리하기 위한 List
        List<C24ProductExceptionGroup> c24ExceptionGroupList = new ArrayList<>();

        // WebDriver 설정
        crawlService.setDriverProperty();
        WebDriver driver = crawlService.createWebDriver();
        WebDriverWait webDriverWait = crawlService.createWebDriverWait(driver, 10);

        // 새 상품 crawl && set new C24Code -> insert or disable
        try {
            for (C24Product c24Product : newC24ProductList) {
                try {
                    crawlService.crawlProductCostco(driver, webDriverWait, c24Product, formatToday);
                } catch (Exception e) {
                    if (e instanceof CrawlException && ((CrawlException) e).getType() == CrawlException.Type.FORBIDDEN) {
                        // CrawlException.Type.FORBIDDEN 일 경우, 그대로 throw
                        throw e;
                    }
                    C24ProductExceptionGroup c24ExceptionGroup = new C24ProductExceptionGroup(c24Product.getProductCode(), true);
                    c24ExceptionGroupList.add(c24ExceptionGroup);
                    continue;
                }
                c24ProductService.manageC24Code(c24Code);
                c24Product.setC24Code(c24Code.getC24Code());

                c24ProductService.insertC24ProductCostco(c24Product);

                // c24Product.getC24Code() == 0 인 경우, costco_product.status 를 0 으로 업데이트
                if (c24Product.getC24Status() == 0) {
                    productService.updateCostcoProductStatus(c24Product.getProductCode(), 0);
                }
            }

            for (Map.Entry<Long, List<C24Product>> entry : existingC24ProductsMap.entrySet()) {
                Long productCode = entry.getKey();
                List<C24Product> c24List = entry.getValue();

                boolean areObjectsSame = c24ProductService.checkForSameObjects(c24List);
                boolean checkRandomMustAttributes = c24List.get(0).checkForC24ProductMustAttributes();
                boolean isStatusUpdateRequired = c24List.get(0).isC24StatusDisabled() && c24List.get(0).getIsOption() == 0;

                if (!(areObjectsSame && checkRandomMustAttributes)) {
                    C24ProductGroup c24Group = new C24ProductGroup();
                    c24Group.setProductCode(productCode);
                    C24Product c24Product;
                    try {
                        c24Product = crawlService.crawlProductCostco(driver, webDriverWait, productCode, formatToday);
                    } catch (Exception e) {
                        if (e instanceof CrawlException && ((CrawlException) e).getType() == CrawlException.Type.FORBIDDEN) {
                            // CrawlException.Type.FORBIDDEN 일 경우, 그대로 throw
                            throw e;
                        }
                        C24ProductExceptionGroup c24ExceptionGroup = new C24ProductExceptionGroup(productCode, false);
                        c24ExceptionGroupList.add(c24ExceptionGroup);
                        continue;
                    }

                    // update 전 필수 속성 검사하여 status setC24Status 변경 여부 결정
                    if (!c24Product.checkForC24ProductMustAttributes()) {
                        c24Product.setC24Status(0);
                    }
                    c24Group.setCommonC24Product(c24Product);
                    c24ProductService.updateC24Group(c24Group);
                } else if (isStatusUpdateRequired) {
                    c24ProductService.updateStatusByProductCode(productCode, 1);
                }
            }

            // Exception 발생 c24CostcoProduct 에 대해서 다시 crawl 시도 후, 에러 발생할 경우 상품 비활성화
            for (C24ProductExceptionGroup c24ExceptionGroup : c24ExceptionGroupList) {
                // insert
                if (c24ExceptionGroup.isInsert()) {
                    C24Product c24Product;
                    try {
                        c24Product = crawlService.crawlProductCostco(driver, webDriverWait, c24ExceptionGroup.getProductCode(), formatToday);
                    } catch (Exception e) {
                        if (e instanceof CrawlException && ((CrawlException) e).getType() == CrawlException.Type.FORBIDDEN) {
                            // CrawlException.Type.FORBIDDEN 일 경우, 그대로 throw
                            throw e;
                        }
                        continue;
                    }
                    c24ProductService.manageC24Code(c24Code);
                    c24Product.setC24Code(c24Code.getC24Code());

                    c24ProductService.insertC24ProductCostco(c24Product);
                    // update
                } else {
                    C24Product c24Product;
                    try {
                        c24Product = crawlService.crawlProductCostco(driver, webDriverWait, c24ExceptionGroup.getProductCode(), formatToday);
                    } catch (Exception e) {
                        if (e instanceof CrawlException && ((CrawlException) e).getType() == CrawlException.Type.FORBIDDEN) {
                            // CrawlException.Type.FORBIDDEN 일 경우, 그대로 throw
                            throw e;
                        }
                        c24ProductService.updateStatusByProductCode(c24ExceptionGroup.getProductCode(), 0);
                        continue;
                    }

                    // update 전 필수 속성 검사하여 status setC24Status 변경 여부 결정
                    if (!c24Product.checkForC24ProductMustAttributes()) {
                        c24Product.setC24Status(0);
                    }
                    c24ExceptionGroup.setCommonC24Product(c24Product);
                    c24ProductService.updateC24Group(c24ExceptionGroup);
                }
            }
        } catch (Exception e) {
            throw new CrawlException(CrawlException.Type.BAD_REQUEST, e.getMessage());
        } finally {
            driver.quit();
        }

        // disabling(update c24_product.status 0) : (c24_product.status == 1 && costco_product.status == 0)
        List<Integer> disablingIdxList = c24ProductService.getDisablingIdxList();
        c24ProductService.updateStatusByIdxList(disablingIdxList, 0);
    }

    @PostMapping("/crawl-1688")
    public Long crawlCurrentPage1688(
            String formatToday,
            WebDriver driver
    ) throws Exception {
        // 현재 driver 의 페이지를 가져와서 productCode 를 구합니다.
        String currentUrl = driver.getCurrentUrl();
        Pattern pattern = Pattern.compile("/offer/(\\d+).html");
        Matcher matcher = pattern.matcher(currentUrl);
        long productCode = 0;

        if (matcher.find()) {
            // 첫 번째 그룹에 해당하는 상품 코드 출력
            productCode = Long.parseLong(matcher.group(1));
        } else {
            return productCode;
        }

        // WebDriver 를 사용해 빈 페이지로 이동
        C24Product c24Product = new C24Product();
        c24Product.setProductCode(productCode);

        WebDriverWait webDriverWait = crawlService.createWebDriverWait(driver, 10);

        // retrieve latest C24Code
        String lastC24Code = c24ProductService.getLastC24Code1688();
        C24Code c24Code = new C24Code();
        c24Code.setCharsByCode(lastC24Code);

        try {
            crawlService.crawlProduct1688(driver, webDriverWait, c24Product, formatToday);
        } catch (Exception e) {
            if (e instanceof CrawlException && ((CrawlException) e).getType() == CrawlException.Type.FORBIDDEN) {
                // CrawlException.Type.FORBIDDEN 일 경우, 그대로 throw
                throw e;
            }
        }
        c24ProductService.manageC24Code(c24Code);
        c24Product.setC24Code(c24Code.getC24Code());

        c24ProductService.insertC24Product1688(c24Product);

        // c24Product.getC24Code() == 0 인 경우, costco_product.status 를 0 으로 업데이트
        if (c24Product.getC24Status() == 0) {
            productService.updateCostcoProductStatus(c24Product.getProductCode(), 0);
        }

        return productCode;
    }

}
