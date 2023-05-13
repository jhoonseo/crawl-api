package com.costco.crawl.service;

import com.costco.crawl.controller.dto.Category;
import com.costco.crawl.controller.dto.CategoryInfo;
import com.costco.crawl.controller.dto.CostcoProduct;
import com.costco.crawl.dao.CostcoProductDao;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
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
    private final CommonService commonService;
    private final CostcoProductDao costcoProductDao;

    @Value("${web.driver.id:}")
    private String webDriverId;
    @Value("${web.driver.path:}")
    private String webDriverPath;

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
        if (commonService.checkTagFrom("a", lastCategory)) {
            String lastCategoryTitle = categories.get(categories.size() - 1)
                    .findElement(By.tagName("a"))
                    .getAttribute("title");

            category.setName(lastCategoryTitle);
        }
    }

    public Set<CostcoProduct> crawlFromCategory(CategoryInfo categoryInfo) {
        driver.get(categoryInfo.getUrl());
        Set<CostcoProduct> costcoProductSet = new HashSet<>();

        if (!(waitUntilTitleBool("코스트코 코리아") || waitUntilVisibilityByClassBool("breadcrumb-section") || waitUntilVisibilityByClassBool("d-block"))) {
            return costcoProductSet;
        }

        List<WebElement> productItems = driver.findElements(By.className("product-list-item"));
        categoryInfo.setProductItemCountPage(productItems.size());

        if (categoryInfo.getProductItemCountPage() == 0) {
            return costcoProductSet;
        }

        productItems.forEach(
                (productItem) -> {
                    CostcoProduct costcoProduct = new CostcoProduct();
                    costcoProduct.setCostcoCategoryIdx(categoryInfo.getCostcoCategoryIdx());
                    String productUrl = productItem
                            .findElement(By.className("lister-name"))
                            .getAttribute("href");
                    costcoProduct.setProductUrlAndProductCode(productUrl);

                    // Early Morning Delivery 비활성화
                    if (commonService.checkClassFrom("product-list-delivery", productItem)) {
                        costcoProduct.setStatus(0);
                    }

                    // 상품 가격 || 멤버 전용 상품 설정
                    if (commonService.checkClassFrom("product-price-amount", productItem)) {
                        WebElement productPriceAmountElement = productItem.findElement(By.className("product-price-amount"));
                        if (commonService.checkClassFrom("notranslate", productPriceAmountElement)) {
                            costcoProduct.setProductPrice(productPriceAmountElement
                                    .findElement(By.className("notranslate")).getText());
                        }
                    } else {
                        if (commonService.checkClassFrom("price-panel-login", productItem)) {
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
                    if (commonService.checkClassFrom("discount-row-message", productItem)) {
                        costcoProduct.setIsSale(1);
                        costcoProduct.setSaleAmount(
                                productItem.findElement(By.className("discount-row-message")).getText()
                        );

                        if (commonService.checkClassFrom("discount-date", productItem)) {
                            costcoProduct.setSalePeriod(
                                    productItem.findElement(By.className("discount-date")).getText()
                            );
                        }
                    }

                    // 최소 최대 구매수량 설정
                    if (commonService.checkClassFrom("min-qty-status", productItem)) {
                        costcoProduct.setMinQty(
                                productItem.findElement(By.className("min-qty-status")).getText()
                        );
                    }
                    if (commonService.checkClassFrom("max-qty-status", productItem)) {
                        costcoProduct.setMaxQty(
                                productItem.findElement(By.className("max-qty-status")).getText()
                        );
                    }

                    // 옵션 상품 비활성화
                    if (!commonService.checkClassFrom("add-to-cart-wrapper", productItem)) {
                        costcoProduct.setIsOption(1);
                        costcoProduct.setStatus(0);
                    }

                    // 상품 크롤링 시간 입력
                    costcoProduct.setUpdatedDateTime(commonService.getCurrentTimestamp());

                    costcoProductSet.add(costcoProduct);
                });
        return costcoProductSet;
    }

    public void crawlProduct(CostcoProduct costcoProduct) {
        driver.get(costcoProduct.getProductUrl());

        if (!(waitUntilTitleBool("코스트코 코리아") || waitUntilVisibilityByClassBool("view-more__button") || waitUntilVisibilityByClassBool("breadcrumb"))) {
            return;
        }

        driver.findElement(By.className("product-name")).getText();



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
