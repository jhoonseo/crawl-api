package com.project.crawl.controller.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Data
@Slf4j
public class C24CostcoProductXlsx extends C24CostcoProduct {
    private String searchKeyWords;
    private String categoryName;
    private int c24CateNo;

    // 미분류 카테고리 코드 76
    public int getC24CateNo() {
        if (c24CateNo == 0) {
            return 76;
        }
        return c24CateNo;
    }

    public String getCategoryName() {
        return Objects.isNull(categoryName) ? "코스트코/코코모" : categoryName;
    }
    public boolean checkForC24ProductMustAttributes() {
        return super.getC24Idx() != 0
                && isNotEmpty(super.getName())
                && isNotEmpty(super.getNameEn())
                && super.getPrice() != 0
                && isNotEmpty(super.getC24Code())
                && isNotEmpty(super.getThumbMain());
    }
}
