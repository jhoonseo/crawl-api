package com.project.crawl.service.dto;

import lombok.Data;

import java.util.List;

@Data
public class SkuPriceRange {
    public int begin_num;
    public int stock;
    public String sell_unit;
    public List<SkuParam> sku_param;
    public MixParam mix_param;
}
