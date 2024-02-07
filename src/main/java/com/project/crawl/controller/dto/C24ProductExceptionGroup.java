package com.project.crawl.controller.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class C24ProductExceptionGroup extends C24ProductGroup {
    // Exception 발생한 C24Product 을 취합하고, insert 또는 update 여부를 체크하기 위해 추가
    private boolean isInsert;

    public C24ProductExceptionGroup(long productCode, boolean isInsert) {
        super.setProductCode(productCode);
        this.isInsert = isInsert;
    }
}
