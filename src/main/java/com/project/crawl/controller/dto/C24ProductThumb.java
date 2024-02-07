package com.project.crawl.controller.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Data
public class C24ProductThumb {
    private List<String> thumbUrlList;
    private boolean isRequiredUpdate = false;
    private C24Product commonC24Product = new C24Product();
}
