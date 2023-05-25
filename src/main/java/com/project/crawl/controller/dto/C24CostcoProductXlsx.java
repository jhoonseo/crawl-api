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
    public boolean checkForMustAttributes() {
        return super.getC24Idx() != 0
                && (!Objects.isNull(super.getName()) && !super.getName().isEmpty())
                && (!Objects.isNull(super.getNameEn()) && !super.getNameEn().isEmpty())
                && super.getPrice() != 0
                && (!Objects.isNull(super.getC24Code()) && !super.getC24Code().isEmpty())
                && (!Objects.isNull(super.getThumbMain()) && !super.getThumbMain().isEmpty());

    }
}
