package com.project.crawl.service;

import com.project.crawl.controller.dto.C24Product;
import com.project.crawl.controller.dto.Category;
import com.project.crawl.controller.dto.CategoryInfo;
import com.project.crawl.controller.dto.CostcoProduct;
import com.project.crawl.exceptions.CrawlException;
import com.project.crawl.util.CommonUtil;
import com.project.crawl.util.LanguageCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlService {
    private final CommonUtil commonUtil;
    private final TranslateService translateService;

    @Value("${web.driver.id:}")
    private String webDriverId;
    @Value("${web.driver.path:}")
    private String webDriverPath;
    @Value("${cdn.base.url.costco:}")
    private String cdnBaseUrlCostco;

    @Value("${cdn.base.url.1688:}")
    private String cdnBaseUrl1688;

    @Value("${local.daily.directory.1688}")
    private String localDailyDirectory1688;


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

        // 로딩 대기를 위해 5초 중단
        sleepMilliSec(5000);
        if (!waitUntilTitleBool(webDriverWait, "코스트코 코리아")
                || !waitUntilVisibilityByClassBool(webDriverWait, "breadcrumb-section")
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

            String nameEn = commonUtil.getTextOfClassFrom("price-panel-login", productItem);

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

    public C24Product crawlProductCostco(WebDriver driver, WebDriverWait webDriverWait, Long productCode, String formatToday) throws IOException {
        C24Product c24Product = new C24Product();
        c24Product.setProductCode(productCode);
        crawlProductCostco(driver, webDriverWait, c24Product, formatToday);
        return c24Product;
    }

    public void crawlProductCostco(WebDriver driver, WebDriverWait webDriverWait, C24Product c24Product, String formatToday) throws IOException {
        log.debug(c24Product.getProductUrlCostco()); // TODO remove after test
        driver.get(c24Product.getProductUrlCostco());

        // 로딩 대기를 위해 5초 중단
        sleepMilliSec(5000);

        if (!waitUntilTitleBool(webDriverWait, "코스트코 코리아")
                || !waitUntilPageLoad(webDriverWait)
                || !waitUntilVisibilityByClassBool(webDriverWait, "image-panel")
                || !waitUntilPresenceOfNestedAllByClass(webDriverWait, "thumb", By.tagName("picture"))
                || !waitUntilPresenceOfNestedAllByClass(webDriverWait, "thumb", By.tagName("img"))
        ) {
            throwExceptionIfHttpStatus403(driver);
            c24Product.setC24Status(0);
            return;
        }

        // ------------------- 썸네일 관련 -------------------
        processThumbsAndGenerateThumbDetailInfoCostco(driver, c24Product, formatToday);

        // 배송 및 환불정보에 img tag 가 있는 경우 깨지는 이슈 제거
        if (commonUtil.checkClassExist("product-delivery-refund", driver)) {
            runJsScriptRemoveDeliveryImageCostco(driver);
        }
        // ------------------- 배송정보 관련 -------------------
        generateDeliveryInfoCostco(driver, c24Product);
        // ------------------- 환불정보 관련 -------------------
        generateRefundInfoCostco(driver, c24Product);
        // ------------------- 스펙정보 관련 -------------------
        generateSpecInfoCostco(driver, c24Product);
        // ------------------- descriptionDetail 관련 -------------------
        generateDescriptionDetailCostco(driver, c24Product);
    }


    public void crawlProduct1688(WebDriver driver, WebDriverWait webDriverWait, C24Product c24Product, String formatToday) throws IOException {
        if (!waitUntilPageLoad(webDriverWait)) {
            throwExceptionIfHttpStatus403(driver);
            c24Product.setC24Status(0);
            return;
        }

        // todo : 1. 상품명 상품가격 배송비 옵션정보(옵션 종류별) 크롤링 / 2. 상품명 상품상세(이미지) 상품썸네일(이미지) 옵션명(옵션a|옵션b...) specInfo 번역 / 3. 일자별 환율 db에 입력하고 상품 가격 계산 (185원 per CNY)
        // 다양한 옵션 형태 https://detail.1688.com/offer/699265952257.html / https://detail.1688.com/offer/750401695002.html / https://detail.1688.com/offer/686998944186.html
        // 옵션 종류 sku-module-wrapper > sku-item-wrapper > sku-item-name & discountPrice-price

        // ------------------- 썸네일 관련 -------------------
        processThumbsAndGenerateThumbDetailInfo1688(driver, c24Product, formatToday);
        // ------------------- 스펙정보 관련 -------------------
        generateSpecInfo1688(driver, c24Product);
        // ------------------- descriptionDetail 관련 -------------------
        generateDescriptionDetail1688(driver, c24Product, formatToday);
        // ------------------- 상품명 관련 -------------------
        setProductName1688(driver, c24Product);
        // ------------------- 상품 가격 관련 -------------------
        c24Product.setPrice(getProductMaxPriceKRW1688(driver));
        // ------------------- 옵션 정보 -------------------
        // todo : 옵션을 관리가 가능하도록 별도의 옵션테이블에 가격과 함께 관리 (cafe24 재고관리에서 상품가격 업데이트 가능하도록)
        String optionNames = getProductOptionNames1688(driver);
        translateService.translateText(optionNames, LanguageCode.SimplifiedChinese.getCode(), LanguageCode.Korean.getCode());
    }

    private String getProductOptionNames1688(WebDriver driver) {
        List<WebElement> optionNameElementList = driver.findElements(By.className("sku-item-name"));

        // Stream API를 사용하여 각 요소의 텍스트를 추출하고, '|' 문자를 제거한 뒤, '|'를 사용하여 합칩니다.
        String optionNames = optionNameElementList.stream()
                .map(WebElement::getText) // 각 요소의 텍스트를 가져옵니다.
                .map(text -> text.replace("|", "")) // 텍스트에서 '|' 문자를 제거합니다.
                .collect(Collectors.joining("|")); // 결과를 '|'로 구분하여 하나의 문자열로 합칩니다.

        return optionNames;
    }

    private Integer getProductMaxPriceKRW1688(WebDriver driver) {
        List<WebElement> priceBoxes = driver.findElements(By.className("price-box"));
        WebElement lastPriceBox = priceBoxes.get(priceBoxes.size() - 1);
        String CNYPrice = lastPriceBox.findElement(By.className("price-text")).getText();
        return (int) Math.round(Integer.parseInt(CNYPrice) * 185 / 100.0) * 100;
    }

    private void setProductName1688(WebDriver driver, C24Product c24Product) {
        String originalName = driver.findElement(By.className("title-first-column")).findElement(By.className("title-text")).getText();
        String koName = translateService.translateText(originalName, LanguageCode.SimplifiedChinese.getCode(), LanguageCode.Korean.getCode());
        String enName = translateService.translateText(originalName, LanguageCode.SimplifiedChinese.getCode(), LanguageCode.English.getCode());
        c24Product.setName(koName);
        c24Product.setNameEn(enName);
    }

    private void generateDescriptionDetailCostco(WebDriver driver, C24Product c24Product) {
        if (commonUtil.checkClassExist("product-details-content-wrapper", driver)) {
            runJsScriptRemoveDetailImage(driver);
            runJsScriptRemoveStyleTag(driver);
            // 상품 상세에 a tag href 가 있는경우 클릭 시 redirection 404 error 막기 위해 추가
            runJsScriptRemoveHrefTarget(driver);

            String descriptionDetailInfo = driver.findElement(By.className("product-details-content-wrapper"))
                    .getAttribute("innerHTML");

            c24Product.setDescriptionDetail(descriptionDetailInfo);
        }
    }

    private void generateDescriptionDetail1688(WebDriver driver, C24Product c24Product, String formatToday) {
        // lazy loading 상세 이미지 모두 불러오도록 최하단 이동을 체크
        checkFullyScrolled1688(driver);
        // 1688 의 경우에는 text 는 모두 제거하고 이미지로만 이뤄진 상세를 만들어야한다
        WebElement detailContainer = commonUtil.getWebElementByClassFromDriver("content-detail", driver);
        List<WebElement> detailImgList = detailContainer.findElements(By.tagName("img"));
        // img 태그의 src 속성을 취합할 리스트 생성
        List<String> imgUrlList = new ArrayList<>(), imgFilenameList = new ArrayList<>();

        // detailImgList 각 img 태그의 src 속성을 가져와서 imgSrcList 에 추가
        for (WebElement imgElement : detailImgList) {
            String url = imgElement.getAttribute("src");
            String fileName = url.split("/")[url.split("/").length - 1];
            if (!Objects.isNull(url) && !url.isEmpty()
                    && !url.endsWith(".webp")
                    && commonUtil.isImageDownloaded1688(url, fileName, formatToday)
            ) {
                imgUrlList.add(url);
                imgFilenameList.add(fileName);
            }
        }

        // todo : put translation here?
        StringBuilder descriptionDetailInfo = new StringBuilder();
        if (imgUrlList.size() == 0) {
            c24Product.setC24Status(0);
        } else {
            for (int i = 0; i < imgUrlList.size(); i++) {
                String thumbUrl = imgUrlList.get(i);
                String thumbFilename = thumbUrl.split("/")[thumbUrl.split("/").length - 1];
                // 번역된 파일은 {cdnBaseUrl1688}/kr 경로에 저장된다
                String src = String.join("/", cdnBaseUrl1688, "ko", thumbFilename);
                descriptionDetailInfo.append("<img src='").append(src)
                        .append("' style='width: 100%;'/>\n ");
            }
            c24Product.setDescriptionDetail(descriptionDetailInfo.toString());
        }
    }

    public void checkFullyScrolled1688(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // 이전에 로드된 이미지의 수
        int prevLoadedImagesCount = 0;

        while (true) {
            // 페이지 최하단으로 스크롤
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");

            // 동적 로딩 대기
            sleepMilliSec(2000);

            // 로딩된 이미지의 수 확인
            List<WebElement> loadedImages = driver.findElements(By.className("desc-img-loaded"));
            int loadedImagesCount = loadedImages.size();

            // 더 이상 로딩되는 이미지가 없으면 반복 중지
            if (loadedImagesCount == prevLoadedImagesCount) {
                break;
            }

            prevLoadedImagesCount = loadedImagesCount;
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

    private void generateSpecInfoCostco(WebDriver driver, C24Product c24Product) {
        if (commonUtil.checkClassExist("product-classification-wrapper", driver)) {
            WebElement specInfoContainer = driver.findElement(By.className("product-classification-wrapper"));
            if (commonUtil.checkTagFrom("table", specInfoContainer)) {
                String specInfo = specInfoContainer.findElement(By.tagName("table")).getAttribute("outerHTML");
                c24Product.setSpecInfoTable(specInfo);
            } else {
                String specInfo = specInfoContainer.getAttribute("innerHTML");
                c24Product.setSpecInfoTable(specInfo);
            }
        }
    }

    private void generateSpecInfo1688(WebDriver driver, C24Product c24Product) {
        // offer-attr-wrapper & offer-attr-list
        String specInfo = commonUtil.getOuterHTMLFromDriver("offer-attr-wrapper", driver);
        String transSpecInfo = translateService.translateText(specInfo, LanguageCode.SimplifiedChinese.getCode(), LanguageCode.Korean.getCode());
        c24Product.setSpecInfoTable(transSpecInfo);
    }


    private void generateDeliveryInfoCostco(WebDriver driver, C24Product c24Product) {
        if (commonUtil.checkClassExist("delivery-info", driver)) {
            String deliveryInfo = driver.findElement(By.className("delivery-info")).getAttribute("innerHTML");
            c24Product.setDeliveryInfo(deliveryInfo);
        }
    }

    private void generateRefundInfoCostco(WebDriver driver, C24Product c24Product) {
        if (commonUtil.checkClassExist("return-info", driver)) {
            String refundInfo = driver.findElement(By.className("return-info")).getAttribute("innerHTML");
            c24Product.setRefundInfo(refundInfo);
        }
    }

    public void runJsScriptRemoveDeliveryImageCostco(WebDriver driver) {
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

    private void processThumbsAndGenerateThumbDetailInfoCostco(WebDriver driver, C24Product c24Product, String formatToday) throws IOException {
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
                    && commonUtil.isImageDownloadedCostco(url, fileName, formatToday)
            ) {
                thumbUrlList.add(url);
                thumbFilenameList.add(fileName);
            }
        }

        if (thumbUrlList.size() == 0) {
            // 누끼 이미지가 없는 경우, 상품을 비활성화
            c24Product.setC24Status(0);
        } else {
            for (int i = 0; i < thumbUrlList.size(); i++) {
                String thumbUrl = thumbUrlList.get(i);
                String thumbFilename = thumbFilenameList.get(i);
                String src = String.join("/", cdnBaseUrlCostco, thumbFilename);
                thumbDetailInfo.append("<img src='").append(src)
                        .append("' style='width: 100%; margin-bottom:60px;'/>\n ");
            }
            c24Product.setThumbDetail(thumbDetailInfo.toString());
            c24Product.setThumbMain(thumbUrlList.get(0));
            // 첫번째 이미지는 추가 이미지 리스트에서 제외
            thumbUrlList.remove(0);
            thumbFilenameList.remove(0);
            c24Product.setThumbExtraFilenames(String.join("|", thumbFilenameList));
            c24Product.setThumbExtra(String.join("|", thumbUrlList));
        }
    }

    private void processThumbsAndGenerateThumbDetailInfo1688(WebDriver driver, C24Product c24Product, String formatToday) throws IOException {
        // setThumbDetail && setThumbExtraFilenames && setThumbMain && setThumbExtra
        // 적절한 이미지가 1개도 없는 경우 setC24Status(0)
        List<WebElement> thumbWrapperList = driver.findElements(By.className("detail-gallery-turn-wrapper"));
        StringBuilder thumbDetailInfo = new StringBuilder();
        List<String> thumbUrlList = new ArrayList<>();
        List<String> thumbFilenameList = new ArrayList<>();

        for (WebElement thumbWrapper : thumbWrapperList) {
            String url = null, fileName = null;
            // className : detail-gallery-img 을 가진 요소들 중에서 video 인 경우에는 className : video-icon 과 함께있다.->equals(2)
            if (commonUtil.getTagCountFrom("img", thumbWrapper).equals(1)) {
                WebElement picture = thumbWrapper.findElement(By.className("detail-gallery-img"));
                url = picture.getAttribute("src");
                fileName = url.split("/")[url.split("/").length - 1];
            }

            if (!Objects.isNull(url) && !url.isEmpty()
                    && !url.endsWith(".webp")
                    && commonUtil.isImageDownloaded1688(url, fileName, formatToday)
            ) {
                thumbUrlList.add(url);
                thumbFilenameList.add(fileName);
            }
        }

        if (thumbUrlList.size() == 0) {
            // 이미지가 없는 경우, 상품을 비활성화
            c24Product.setC24Status(0);
        } else {
            for (int i = 0; i < thumbUrlList.size(); i++) {
                String thumbFilename = thumbFilenameList.get(i);
                String src = String.join("/", cdnBaseUrl1688, "ko", thumbFilename);
                thumbDetailInfo.append("<img src='").append(src)
                        .append("' style='width: 100%;'/>\n ");
            }
            c24Product.setThumbDetail(thumbDetailInfo.toString());
            c24Product.setThumbMain(thumbUrlList.get(0));
            // 첫번째 이미지는 추가 이미지 리스트에서 제외
            thumbUrlList.remove(0);
            thumbFilenameList.remove(0);
            c24Product.setThumbExtraFilenames(String.join("|", thumbFilenameList));
            c24Product.setThumbExtra(String.join("|", thumbUrlList));
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

    public void sleepMilliSec(Integer millis) {
        try {
            Thread.sleep(millis); // 5초 동안 일시 정지
        } catch (InterruptedException e) {
            log.error("Thread sleep interrupted", e);
        }
    }

    public WebDriver createWebDriver() {
        setDriverProperty();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");

        return new ChromeDriver(options);
    }

    public WebDriver createWebDriver1688() {
        setDriverProperty();

//        ChromeDriverService service = new ChromeDriverService.Builder()
//                .withLogFile(new File(localDailyDirectory1688, "logfile.log")) // 로그 파일 경로 지정
//                .withVerbose(true) // 상세 로그 활성화
//                .build();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
        options.addArguments("--disable-blink-features=AutomationControlled");
        // Third-party 쿠키 허용 설정
        options.addArguments("--disable-web-security"); // 웹 보안 비활성화
        options.addArguments("--allow-running-insecure-content"); // 안전하지 않은 콘텐츠 실행 허용
        options.addArguments("--disable-features=SameSiteByDefaultCookies"); // SameSite 쿠키 정책 비활성화

//        return new ChromeDriver(service, options);
        return new ChromeDriver(options);
    }


    public WebDriverWait createWebDriverWait(WebDriver driver, int timeoutSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }

    public void setDriverProperty() {
            System.setProperty(webDriverId, webDriverPath);
    }

}
