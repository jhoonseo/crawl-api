package com.costco.crawl.controller.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
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

    // ----------- no db column -----------
    private List<String> thumbExtraList;
    // ----------- no db column -----------

    public C24CostcoProduct(Integer productCode) {
        this.productCode = productCode;
    }

    public boolean checkForMustAttributes() {
        boolean isValid = true;
        if (cpIdx == 0
                || c24Idx == 0
                || (Objects.isNull(name) || name.isEmpty())
                || (Objects.isNull(nameEn) || nameEn.isEmpty())
                || costcoCategoryIdx == 0
                || price == 0
                || (Objects.isNull(c24Code) || c24Code.isEmpty())
                || (Objects.isNull(thumbDetail) || thumbDetail.isEmpty())
                || (Objects.isNull(thumbMain) || thumbMain.isEmpty())
        ) {
            isValid = false;
            log.error("mustAttributes are not set for productCode: {}", productCode);
        }
        return isValid;
    }

    public String getProductUrl() {
        return "https://www.costco.co.kr/p/" + productCode;
    }

    public String getThumbMainFilename() {
        String[] splitPath = thumbMain.split("/");
        return splitPath[splitPath.length - 1];
    }
}