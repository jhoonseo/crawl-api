package com.costco.crawl.controller.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Data
public class CostcoProduct {
    private int idx;
    private int productCode;
    private int costcoCategoryIdx;
    private String name;
    private String nameEn;
    private int price = 0;
    private int saleAmount = 0;
    private String salePeriod = "";
    private int isSale = 0;
    private int isOption = 0;
    private int minQty = 0;
    private int maxQty = 0;
    private int status = 1;
    private int isMemberOnly = 0;
    private Timestamp updatedDateTime;

    private String productUrl;
    private static final Map<Character, Character> specialCharacterMap =
            Map.of(
                    '&', '+',
                    ',', '-'
            );
    public void setNames(String name, String nameEn) {
        for (Map.Entry<Character, Character> entry : specialCharacterMap.entrySet()) {
            this.name = name.replace(entry.getKey(), entry.getValue());
            this.nameEn = nameEn.replace(entry.getKey(), entry.getValue());
        }
    }

    public void setProductUrlAndProductCode(String url) {
        productUrl = url;
        String[] urlSplit = url.split("/");
        String codeString = urlSplit[urlSplit.length - 1];
        productCode = Integer.parseInt(codeString);
    }

    public String getProductUrl() {
        if (Objects.isNull(productUrl)) {
            return "https://www.costco.co.kr/p/" + productCode;
        } else {
            return productUrl;
        }
    }

    public void setProductPrice(String price) {
        this.price = Integer.parseInt(
                price.replaceAll("[^0-9]", "")
        );
    }

    public void setSaleAmount(String saleAmount) {
        this.saleAmount = Integer.parseInt(
                saleAmount.replaceAll("[^0-9]", "")
        );
    }

    public void setMinQty(String sMinQty) {
        minQty = Integer.parseInt(sMinQty.replaceAll("[^0-9]", ""));

    }

    public void setMaxQty(String sMaxQty) {
        maxQty = Integer.parseInt(sMaxQty.replaceAll("[^0-9]", ""));
    }

    public boolean isRequiredUpdate(CostcoProduct costcoProduct) {
        return price != costcoProduct.getPrice() ||
                isSale != costcoProduct.getIsSale() ||
                saleAmount != costcoProduct.getSaleAmount() ||
                !Objects.equals(salePeriod, costcoProduct.getSalePeriod()) ||
                isOption != costcoProduct.getIsOption() ||
                status != costcoProduct.getStatus() ||
                costcoCategoryIdx != costcoProduct.getCostcoCategoryIdx() ||
                isMemberOnly != costcoProduct.getIsMemberOnly() ||
                minQty != costcoProduct.getMinQty() ||
                maxQty != costcoProduct.getMaxQty() ||
                !Objects.equals(name, costcoProduct.getName()) ||
                !Objects.equals(nameEn, costcoProduct.getNameEn());
    }
}