package com.project.crawl.controller.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@Data
public class C24CostcoProduct {
    private int cpIdx; // must
    private int c24Idx; // must
    private int productCode; // not null
    private String name; // must
    private String nameEn; // must
    private int minQty = 0;
    private int maxQty = 0;

    private String c24Code; // must | c24_product

    private String thumbMain; // must | c24_product
    private String thumbExtra; // c24_product
    private String thumbExtraFilenames; // c24_product
    private String thumbDetail; // must | c24_product

    private String deliveryInfo; // c24_product
    private String refundInfo; // c24_product
    private String specInfoTable; // c24_product
    private String descriptionDetail; // c24_product

    private int costcoCategoryIdx; // must
    private int price = 0; // must
    private int saleAmount = 0;
    private String salePeriod = "";
    private int isSale = 0;
    private int isOption = 0;
    private int isMemberOnly = 0;

    private int c24Status = 1; // c24_product

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        C24CostcoProduct that = (C24CostcoProduct) o;
        return productCode == that.productCode &&
                Objects.equals(name, that.name) &&
                Objects.equals(nameEn, that.nameEn) &&
                Objects.equals(thumbMain, that.thumbMain) &&
                Objects.equals(thumbDetail, that.thumbDetail) &&
                costcoCategoryIdx == that.costcoCategoryIdx &&
                price == that.price;
    }

    public int getQtyPrice() {
        if (minQty > 1) {
            return price * minQty;
        }
        return price;
    }

    public String getQtyName() {
        if (minQty > 1 && name.contains("최소구매 ")) {
            return name.replace("최소구매 ", "") + "세트";
        } else if (minQty > 1 && name.contains("최소구매")) {
            return name.replace("최소구매", "") + "세트";
        } else if (minQty > 1) {
            return name + " / " + minQty + " 세트";
        }

        return name;
    }

    public boolean checkForC24ProductMustAttributes() {
        return isNotEmpty(thumbMain);
    }

    public boolean isNotEmpty(String value) {
        return !Objects.isNull(value) && !value.isEmpty();
    }

    public String getProductUrl() {
        return "https://www.costco.co.kr/p/" + productCode;
    }

    public String getThumbMainFilename() {
        String[] splitPath = thumbMain.split("/");
        return splitPath[splitPath.length - 1];
    }
}