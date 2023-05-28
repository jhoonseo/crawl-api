package com.project.crawl.controller.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class C24CostcoProductExceptionGroup extends  C24CostcoProductGroup{
    // Exception 발생한 C24CostcoProduct 을 취합하고, insert 또는 update 여부를 체크하기 위해 추가
    private boolean isInsert;

    public C24CostcoProductExceptionGroup(int productCode, boolean isInsert) {
        super.setProductCode(productCode);
        this.isInsert = isInsert;
    }
}
