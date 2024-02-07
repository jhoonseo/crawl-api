package com.project.crawl.controller.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class C24ProductGroup {
    private long productCode;
    private C24Product commonC24Product = new C24Product();
}
