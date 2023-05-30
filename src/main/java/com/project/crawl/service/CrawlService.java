package com.project.crawl.service;

import com.project.crawl.controller.dto.C24CostcoProduct;
import com.project.crawl.controller.dto.Category;
import com.project.crawl.controller.dto.CategoryInfo;
import com.project.crawl.controller.dto.CostcoProduct;
import com.project.crawl.exceptions.CrawlException;
import com.project.crawl.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlService {
    private final CommonUtil commonUtil;

    @Value("${web.driver.id:}")
    private String webDriverId;
    @Value("${web.driver.path:}")
    private String webDriverPath;
    @Value("${cdn.base.url:}")
    private String cdnBaseUrl;


    private boolean waitUntilPresenceOfNestedAllByClass(WebDriverWait webDriverWait, String className, By byChild) {
        try {
            List<WebElement> elements = webDriverWait.until(
                    ExpectedConditions.presenceOfAllElementsLocatedBy(By.className(className))
            );

            for (WebElement element : elements) {
                // 각 요소의 자식 요소가 로드될 때까지 대기
                webDriverWait.until(
                        ExpectedConditions.presenceOfAllElementsLocatedBy(byChild)
                );
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean waitUntilVisibilityByClassBool(WebDriverWait webDriverWait, String className) {
        try {
            webDriverWait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.className(className)
                    )
            );
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean waitUntilTitleBool(WebDriverWait webDriverWait, String titleName) {
        try {
            webDriverWait.until(
                    ExpectedConditions.titleContains(titleName)
            );
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void crawlCategoryName(WebDriver driver, WebDriverWait webDriverWait, Category category) {
        driver.get("https://www.costco.co.kr/c/" + category.getCategory());
        if (!waitUntilTitleBool(webDriverWait, "코스트코 코리아")
                || !waitUntilVisibilityByClassBool(webDriverWait, "breadcrumb")
                || !waitUntilPageLoad(webDriverWait)
        ) {
            return;
        }
        List<WebElement> categories = driver.findElement(By.className("breadcrumb")).findElements(By.tagName("li"));
        WebElement lastCategory = categories.get(categories.size() - 1);
        if (commonUtil.checkTagFrom("a", lastCategory)) {
            String lastCategoryTitle = lastCategory.findElement(By.tagName("a")).getAttribute("title");
            category.setName(lastCategoryTitle);
        }
    }

    public Set<CostcoProduct> crawlFromCategory(WebDriver driver, WebDriverWait webDriverWait, CategoryInfo categoryInfo) {
        driver.get(categoryInfo.getUrl());
        Set<CostcoProduct> costcoProductSet = new HashSet<>();

        // 로딩 대기를 위해 3초 중단
        sleepMilliSec(3000);
        if (!waitUntilTitleBool(webDriverWait, "코스트코 코리아")
                || !waitUntilVisibilityByClassBool(webDriverWait, "breadcrumb-section")
                || !waitUntilVisibilityByClassBool(webDriverWait, "d-block")
                || !waitUntilPageLoad(webDriverWait)
        ) {
            return costcoProductSet;
        }

        List<WebElement> productItems = driver.findElements(By.className("product-list-item"));
        categoryInfo.setProductItemCountPage(productItems.size());

        if (categoryInfo.getProductItemCountPage() == 0) {
            return costcoProductSet;
        }

        for (WebElement productItem : productItems) {
            CostcoProduct costcoProduct = new CostcoProduct();
            costcoProduct.setCostcoCategoryIdx(categoryInfo.getCostcoCategoryIdx());
            String productUrl = productItem
                    .findElement(By.className("lister-name"))
                    .getAttribute("href");
            costcoProduct.setProductUrlAndProductCode(productUrl);

            // Early Morning Delivery 비활성화
            if (commonUtil.checkClassFrom("product-list-delivery", productItem)) {
                costcoProduct.setStatus(0);
            }

            // 상품 가격 || 멤버 전용 상품 설정
            if (commonUtil.checkClassFrom("product-price-amount", productItem)) {
                WebElement productPriceAmountElement = productItem.findElement(By.className("product-price-amount"));
                if (commonUtil.checkClassFrom("notranslate", productPriceAmountElement)) {
                    costcoProduct.setProductPrice(productPriceAmountElement
                            .findElement(By.className("notranslate")).getText());
                }
            } else {
                if (commonUtil.checkClassFrom("price-panel-login", productItem)) {
                    costcoProduct.setIsMemberOnly(1);
                    costcoProduct.setStatus(0);
                }
            }

            // 상품명 한글 영어 설정
            String name = productItem
                    .findElement(By.className("lister-name"))
                    .findElement(By.className("notranslate"))
                    .getText();
            String nameEn = productItem
                    .findElement(By.className("lister-name-en"))
                    .getText();

            costcoProduct.setNames(name, nameEn);

            // 할인 정보 설정
            if (commonUtil.checkClassFrom("discount-row-message", productItem)) {
                costcoProduct.setIsSale(1);
                costcoProduct.setSaleAmount(
                        productItem.findElement(By.className("discount-row-message")).getText()
                );

                if (commonUtil.checkClassFrom("discount-date", productItem)) {
                    costcoProduct.setSalePeriod(
                            productItem.findElement(By.className("discount-date")).getText()
                    );
                }
            }

            // 최소 최대 구매수량 설정
            if (commonUtil.checkClassFrom("min-qty-status", productItem)) {
                costcoProduct.setMinQty(
                        productItem.findElement(By.className("min-qty-status")).getText()
                );
            }
            if (commonUtil.checkClassFrom("max-qty-status", productItem)) {
                costcoProduct.setMaxQty(
                        productItem.findElement(By.className("max-qty-status")).getText()
                );
            }

            // 옵션 상품 비활성화
            if (!commonUtil.checkClassFrom("add-to-cart-wrapper", productItem)) {
                costcoProduct.setIsOption(1);
                costcoProduct.setStatus(0);
            }

            // 상품 크롤링 시간 입력
            costcoProduct.setUpdatedDateTime(commonUtil.getCurrentTimestamp());

            costcoProductSet.add(costcoProduct);
        }
        return costcoProductSet;
    }

    public C24CostcoProduct crawlProduct(WebDriver driver, WebDriverWait webDriverWait, Integer productCode, String formatToday) throws IOException {
        C24CostcoProduct c24CostcoProduct = new C24CostcoProduct();
        c24CostcoProduct.setProductCode(productCode);
        crawlProduct(driver, webDriverWait, c24CostcoProduct, formatToday);
        return c24CostcoProduct;
    }

    public void crawlProduct(WebDriver driver, WebDriverWait webDriverWait, C24CostcoProduct c24CostcoProduct, String formatToday) throws IOException {
        log.debug(c24CostcoProduct.getProductUrl()); // TODO remove after test
        driver.get(c24CostcoProduct.getProductUrl());

        // 로딩 대기를 위해 5초 중단
        sleepMilliSec(5000);

        if (!waitUntilTitleBool(webDriverWait, "코스트코 코리아")
                || !waitUntilPageLoad(webDriverWait)
                || !waitUntilVisibilityByClassBool(webDriverWait, "image-panel")
                || !waitUntilPresenceOfNestedAllByClass(webDriverWait, "thumb", By.tagName("picture"))
                || !waitUntilPresenceOfNestedAllByClass(webDriverWait, "thumb", By.tagName("img"))
            ) {
            throwExceptionIfHttpStatus403(driver);
            c24CostcoProduct.setC24Status(0);
            return;
        }

        // ------------------- 썸네일 관련 -------------------
        processThumbsAndGenerateThumbDetailInfo(driver, c24CostcoProduct, formatToday);

        // 배송 및 환불정보에 img tag 가 있는 경우 깨지는 이슈 제거
        if (commonUtil.checkClassExist("product-delivery-refund", driver)) {
            runJsScriptRemoveDeliveryImage(driver);
        }
        // ------------------- 배송정보 관련 -------------------
        generateDeliveryInfo(driver, c24CostcoProduct);
        // ------------------- 환불정보 관련 -------------------
        generateRefundInfo(driver, c24CostcoProduct);
        // ------------------- 스펙정보 관련 -------------------
        generateSpecInfo(driver, c24CostcoProduct);
        // ------------------- descriptionDetail 관련 -------------------
        generateDescriptionDetail(driver, c24CostcoProduct);
    }

    private void generateDescriptionDetail(WebDriver driver, C24CostcoProduct c24CostcoProduct) {
        if (commonUtil.checkClassExist("product-details-content-wrapper", driver)) {
            runJsScriptRemoveDetailImage(driver);
            runJsScriptRemoveStyleTag(driver);
            // 상품 상세에 a tag href 가 있는경우 클릭 시 redirection 404 error 막기 위해 추가
            runJsScriptRemoveHrefTarget(driver);

            String descriptionDetailInfo = driver.findElement(By.className("product-details-content-wrapper"))
                    .getAttribute("innerHTML");

            c24CostcoProduct.setDescriptionDetail(descriptionDetailInfo);
        }
    }

    public void runJsScriptRemoveHrefTarget(WebDriver driver) {
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        jsExecutor.executeScript(
                """
                        let n = 0;
                        let count = document.getElementById('product_details').getElementsByTagName('a').length;
                        while (n < count) {
                            let i = document.getElementById('product_details').getElementsByTagName('a')[0];
                            i.removeAttribute('href');
                            i.removeAttribute('target');
                            n = n + 1;
                        }"""
        );
    }

    public void runJsScriptRemoveDetailImage(WebDriver driver) {
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        jsExecutor.executeScript(
                """
                        let n = 0;
                        let count = document.getElementById('product_details').getElementsByTagName('img').length;
                        while (n < count) {
                            let i = document.getElementById('product_details').getElementsByTagName('img')[0];
                            i.parentNode.removeChild(i);
                            n = n + 1;
                        }"""
        );
    }

    public void runJsScriptRemoveStyleTag(WebDriver driver) {
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        jsExecutor.executeScript(
                """
                        let n = 0;
                        let count = document.getElementById('product_details').getElementsByTagName('style').length;
                        while (n < count) {
                            let i = document.getElementById('product_details').getElementsByTagName('style')[0];
                            i.parentNode.removeChild(i);
                            n = n + 1;
                        }"""
        );
    }

    private void generateSpecInfo(WebDriver driver, C24CostcoProduct c24CostcoProduct) {
        if (commonUtil.checkClassExist("product-classification-wrapper", driver)) {
            WebElement specInfoContainer = driver.findElement(By.className("product-classification-wrapper"));
            if (commonUtil.checkTagFrom("table", specInfoContainer)) {
                String specInfo = specInfoContainer.findElement(By.tagName("table")).getAttribute("outerHTML");
                c24CostcoProduct.setSpecInfoTable(specInfo);
            } else {
                String specInfo = specInfoContainer.getAttribute("innerHTML");
                c24CostcoProduct.setSpecInfoTable(specInfo);
            }
        }
    }

    private void generateDeliveryInfo(WebDriver driver, C24CostcoProduct c24CostcoProduct) {
        if (commonUtil.checkClassExist("delivery-info", driver)) {
            String deliveryInfo = driver.findElement(By.className("delivery-info")).getAttribute("innerHTML");
            c24CostcoProduct.setDeliveryInfo(deliveryInfo);
        }
    }

    private void generateRefundInfo(WebDriver driver, C24CostcoProduct c24CostcoProduct) {
        if (commonUtil.checkClassExist("return-info", driver)) {
            String refundInfo = driver.findElement(By.className("return-info")).getAttribute("innerHTML");
            c24CostcoProduct.setRefundInfo(refundInfo);
        }
    }



    public void runJsScriptRemoveDeliveryImage(WebDriver driver) {
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        jsExecutor.executeScript(
                """
                        let n = 0;
                        let count = document.getElementById('product_delivery').getElementsByTagName('img').length;
                        while (n < count) {
                            let i = document.getElementById('product_delivery').getElementsByTagName('img')[0];
                            i.parentNode.removeChild(i);
                            n = n + 1;
                        }"""
        );
    }

    private void processThumbsAndGenerateThumbDetailInfo(WebDriver driver, C24CostcoProduct c24CostcoProduct, String formatToday) throws IOException {
        // setThumbDetail && setThumbExtraFilenames && setThumbMain && setThumbExtra
        // 적절한 이미지가 1개도 없는 경우 setC24Status(0)
        List<WebElement> thumbElementList = driver.findElement(By.className("image-panel")).findElements(By.className("thumb"));
        StringBuilder thumbDetailInfo = new StringBuilder();
        List<String> thumbUrlList = new ArrayList<>();
        List<String> thumbFilenameList = new ArrayList<>();

        for (WebElement thumb : thumbElementList) {
            String url = null, fileName = null;
            if (commonUtil.checkTagFrom("picture", thumb)) {
                WebElement picture = thumb.findElement(By.tagName("picture"));
                if (commonUtil.checkTagFrom("img", picture)) {
                    url = picture.findElement(By.tagName("img")).getAttribute("src");
                    fileName = url.split("/")[url.split("/").length - 1];
                }
            }

            if (!Objects.isNull(url) && !url.isEmpty()
                    && !url.endsWith(".webp")
                    && commonUtil.isNukkiImage(url)
                    && commonUtil.isImageDownloaded(url, fileName, formatToday)
            ) {
                thumbUrlList.add(url);
                thumbFilenameList.add(fileName);
            }
        }

        if (thumbUrlList.size() == 0) {
            // 누끼 이미지가 없는 경우, 상품을 비활성화
            c24CostcoProduct.setC24Status(0);
        } else {
            for (int i = 0; i < thumbUrlList.size(); i++) {
                String thumbUrl = thumbUrlList.get(i);
                String thumbFilename = thumbUrl.split("/")[thumbUrl.split("/").length - 1];
                String src = String.join("/", cdnBaseUrl, thumbFilename);
                thumbDetailInfo.append("<img src='").append(src)
                        .append("' style='width: 100%; margin-bottom:60px;'/>\n ");
                if (i > 0) {
                    thumbFilenameList.add(thumbFilename);
                }
            }
            c24CostcoProduct.setThumbDetail(thumbDetailInfo.toString());
            c24CostcoProduct.setThumbExtraFilenames(String.join("|", thumbFilenameList));

            c24CostcoProduct.setThumbMain(thumbUrlList.get(0));
            thumbUrlList.remove(0);
            c24CostcoProduct.setThumbExtra(String.join("|", thumbUrlList));
        }
    }

    public void throwExceptionIfHttpStatus403(WebDriver driver) throws CrawlException {
        Long statusCode = checkHttpStatus(driver);
        if (statusCode == 403) {
            throw new CrawlException(CrawlException.Type.FORBIDDEN);
        }
    }

    public void throwExceptionIfHttpStatus4xx(WebDriver driver) throws CrawlException {
        Long statusCode = checkHttpStatus(driver);
        if (statusCode >= 400 && statusCode < 500) {
            throw new CrawlException(CrawlException.Type.BAD_REQUEST);
        }
    }

    public Long checkHttpStatus(WebDriver driver) {
        return (Long) ((JavascriptExecutor) driver).executeScript(
                "var xhr = new XMLHttpRequest(); " +
                        "xhr.open('GET', window.location.href, false); " +
                        "xhr.send(); " +
                        "return xhr.status;");
    }

    private boolean waitUntilPageLoad(WebDriverWait webDriverWait) {
        try {
            webDriverWait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void sleepMilliSec(Integer millis) {
        try {
            Thread.sleep(millis); // 5초 동안 일시 정지
        } catch (InterruptedException e) {
            log.error("Thread sleep interrupted", e);
        }
    }

    public void setDriverProperty() {
            System.setProperty(webDriverId, webDriverPath);
    }

}
