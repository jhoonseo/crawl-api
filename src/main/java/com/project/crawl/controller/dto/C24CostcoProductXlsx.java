package com.project.crawl.controller.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Data
@Slf4j
public class C24CostcoProductXlsx extends C24CostcoProduct {
    private String searchKeyWords;
    private String categoryName;

    // 미분류 카테고리 코드 76
    private int c24CateNo = 76;

    public String getCategoryName() {
        return Objects.isNull(categoryName) ? "코스트코/코코모" : categoryName;
    }
    public boolean checkForC24ProductMustAttributes() {
        return super.getC24Idx() != 0
                && isNotEmpty(super.getName())
                && isNotEmpty(super.getNameEn())
                && super.getCostcoCategoryIdx() != 0
                && super.getPrice() != 0
                && isNotEmpty(super.getC24Code())
                && isNotEmpty(super.getThumbMain());
    }
}
