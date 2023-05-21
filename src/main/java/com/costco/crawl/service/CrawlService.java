package com.costco.crawl.service;

import com.costco.crawl.controller.dto.C24CostcoProduct;
import com.costco.crawl.controller.dto.Category;
import com.costco.crawl.controller.dto.CategoryInfo;
import com.costco.crawl.controller.dto.CostcoProduct;
import com.costco.crawl.dao.CostcoProductDao;
import com.costco.crawl.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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

import java.time.Duration;
import java.util.*;


@Service
@RequiredArgsConstructor
public class CrawlService {
    private final CommonUtil commonUtil;
    private final CostcoProductDao costcoProductDao;

    @Value("${web.driver.id:}")
    private String webDriverId;
    @Value("${web.driver.path:}")
    private String webDriverPath;
    @Value("${cdn.base.url:")
    private String cdnBaseUrl;

    private final ChromeOptions options = new ChromeOptions();
    private WebDriver driver;
    private WebDriverWait webDriverWait;


    private boolean waitUntilPresenceByClassBool(String className) {
        try {
            webDriverWait.until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.className(className)
                    )
            );
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void waitUntilPresenceByClass(String className) {
        webDriverWait.until(
            ExpectedConditions.presenceOfElementLocated(
                    By.className(className)
            )
        );
    }

    private boolean waitUntilVisibilityByClassBool(String className) {
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

    private boolean waitUntilTitleBool(String titleName) {
        try {
            webDriverWait.until(
                    ExpectedConditions.titleContains(titleName)
            );
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void crawlCategoryName(Category category) {
        driver.get("https://www.costco.co.kr/c/" + category.getCategory());
        if (!(waitUntilTitleBool("코스트코 코리아") || !waitUntilVisibilityByClassBool("breadcrumb"))) {
            return;
        }
        List<WebElement> categories = driver.findElement(By.className("breadcrumb")).findElements(By.tagName("li"));
        WebElement lastCategory = categories.get(categories.size() - 1);
        if (commonUtil.checkTagFrom("a", lastCategory)) {
            String lastCategoryTitle = lastCategory.findElement(By.tagName("a")).getAttribute("title");
            category.setName(lastCategoryTitle);
        }
    }

    public Set<CostcoProduct> crawlFromCategory(CategoryInfo categoryInfo) {
        driver.get(categoryInfo.getUrl());
        Set<CostcoProduct> costcoProductSet = new HashSet<>();

        if (!waitUntilTitleBool("코스트코 코리아")
                || !waitUntilVisibilityByClassBool("breadcrumb-section")
                || !waitUntilVisibilityByClassBool("d-block")) {
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

    public C24CostcoProduct crawlProduct(Integer productCode) {
        C24CostcoProduct c24CostcoProduct = new C24CostcoProduct(productCode);
        crawlProduct(c24CostcoProduct);
        return c24CostcoProduct;
    }

    public void crawlProduct(C24CostcoProduct c24CostcoProduct) {
        driver.get(c24CostcoProduct.getProductUrl());

        if (!(waitUntilTitleBool("코스트코 코리아")
                || waitUntilVisibilityByClassBool("view-more__button")
                || waitUntilVisibilityByClassBool("breadcrumb"))) {
            return;
        }

        // ------------------- 썸네일 관련 -------------------
        processThumbsAndGenerateThumbDetailInfo(c24CostcoProduct);

        // 배송 및 환불정보에 img tag 가 있는 경우 깨지는 이슈 제거
        if (commonUtil.checkClassExist("product-delivery-refund", driver)) {
            runJsScriptRemoveDeliveryImage();
        }
        // ------------------- 배송정보 관련 -------------------
        generateDeliveryInfo(c24CostcoProduct);
        // ------------------- 환불정보 관련 -------------------
        generateRefundInfo(c24CostcoProduct);
        // ------------------- 스펙정보 관련 -------------------
        generateSpecInfo(c24CostcoProduct);
        // ------------------- descriptionDetail 관련 -------------------
        generateDescriptionDetail(c24CostcoProduct);
    }

    private void generateDescriptionDetail(C24CostcoProduct c24CostcoProduct) {
        if (commonUtil.checkClassExist("product-details-content-wrapper", driver)) {
            runJsScriptRemoveDetailImage();
            runJsScriptRemoveStyleTag();
            // 상품 상세에 a tag href 가 있는경우 클릭 시 redirection 404 error 막기 위해 추가
            runJsScriptRemoveHrefTarget();

            String descriptionDetailInfo = driver.findElement(By.className("product-details-content-wrapper"))
                    .getAttribute("innerHTML");

            c24CostcoProduct.setDescriptionDetail(descriptionDetailInfo);
        }
    }

    public void runJsScriptRemoveHrefTarget() {
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        jsExecutor.executeScript(
                "let n = 0;\n" +
                        "let count = document.getElementById('product_details').getElementsByTagName('a').length;\n" +
                        "while (n < count) {\n" +
                        "    let i = document.getElementById('product_details').getElementsByTagName('a')[0];\n" +
                        "    i.removeAttribute('href');\n" +
                        "    i.removeAttribute('target');\n" +
                        "    n = n + 1;\n" +
                        "}"
        );
    }

    public void runJsScriptRemoveDetailImage() {
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        jsExecutor.executeScript(
                "let n = 0;\n" +
                        "let count = document.getElementById('product_details').getElementsByTagName('img').length;\n" +
                        "while (n < count) {\n" +
                        "    let i = document.getElementById('product_details').getElementsByTagName('img')[0];\n" +
                        "    i.parentNode.removeChild(i);\n" +
                        "    n = n + 1;\n" +
                        "}"
        );
    }

    public void runJsScriptRemoveStyleTag() {
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        jsExecutor.executeScript(
                "let n = 0;\n" +
                        "let count = document.getElementById('product_details').getElementsByTagName('style').length;\n" +
                        "while (n < count) {\n" +
                        "    let i = document.getElementById('product_details').getElementsByTagName('style')[0];\n" +
                        "    i.parentNode.removeChild(i);\n" +
                        "    n = n + 1;\n" +
                        "}"
        );
    }

    private void generateSpecInfo(C24CostcoProduct c24CostcoProduct) {
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

    private void generateDeliveryInfo(C24CostcoProduct c24CostcoProduct) {
        if (commonUtil.checkClassExist("delivery-info", driver)) {
            String deliveryInfo = driver.findElement(By.className("delivery-info")).getAttribute("innerHTML");
            c24CostcoProduct.setDeliveryInfo(deliveryInfo);
        }
    }

    private void generateRefundInfo(C24CostcoProduct c24CostcoProduct) {
        if (commonUtil.checkClassExist("return-info", driver)) {
            String refundInfo = driver.findElement(By.className("return-info")).getAttribute("innerHTML");
            c24CostcoProduct.setRefundInfo(refundInfo);
        }
    }



    public void runJsScriptRemoveDeliveryImage() {
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        jsExecutor.executeScript(
                "let n = 0;\n" +
                        "let count = document.getElementById('product_delivery').getElementsByTagName('img').length;\n" +
                        "while (n < count) {\n" +
                        "    let i = document.getElementById('product_delivery').getElementsByTagName('img')[0];\n" +
                        "    i.parentNode.removeChild(i);\n" +
                        "    n = n + 1;\n" +
                        "}"
        );
    }

    private void processThumbsAndGenerateThumbDetailInfo(C24CostcoProduct c24CostcoProduct) {
        // setThumbDetail && setThumbExtraFilenames && setThumbMain && setThumbExtra
        // 적절한 이미지가 1개도 없는 경우 setC24Status(0)
        List<WebElement> thumbElementList = driver.findElement(By.className("image-panel")).findElements(By.className("thumb"));
        StringBuilder thumbDetailInfo = new StringBuilder();
        List<String> thumbUrlList = new ArrayList<>();
        List<String> thumbFilenameList = new ArrayList<>();

        for (WebElement thumb : thumbElementList) {
            String url = null;
            if (commonUtil.checkTagFrom("picture", thumb)) {
                WebElement picture = thumb.findElement(By.tagName("picture"));
                if (commonUtil.checkTagFrom("img", picture)) {
                    url = picture.findElement(By.tagName("img")).getAttribute("src");
                }
            }

            if (!Objects.isNull(url) && !url.isEmpty()
                    && !url.endsWith(".webp")
                    && commonUtil.isNukkiImage(url)) {
                thumbUrlList.add(url);
                thumbFilenameList.add(url.split("/")[url.split("/").length - 1]);
            }
        }

        if (thumbUrlList.size() == 0) {
            // 누끼 이미지가 없는 경우, 상품을 비활성화
            c24CostcoProduct.setC24Status(0);
        } else {
            for (int i = 0; i < thumbUrlList.size(); i++) {
                String thumbUrl = thumbUrlList.get(i);
                String thumbFilename = thumbUrl.split("/")[thumbUrl.split("/").length - 1];
                String src = cdnBaseUrl + thumbFilename;
                thumbDetailInfo.append("<img src=").append(src)
                        .append(" alt=").append(c24CostcoProduct.getName())
                        .append(" style='width: 100%; margin-bottom:60px;' />\n ");
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

    public void create() {
        if (Objects.isNull(driver)) {
            System.setProperty(webDriverId, webDriverPath);
            options.addArguments("--remote-allow-origins=*");
            driver = new ChromeDriver(options);
            webDriverWait = new WebDriverWait(driver, Duration.ofSeconds(10));
        }
    }

    public void quit() {
        if (!Objects.isNull(driver)) {
            driver.quit();
            driver = null;
        }
    }







}
