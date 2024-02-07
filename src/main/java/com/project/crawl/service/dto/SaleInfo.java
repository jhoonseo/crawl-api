package com.project.crawl.service.dto;

import lombok.Data;

@Data
public class SaleInfo {
    public String gmv_30days;
    public double gmv_30days_cb;
    public int sale_quantity;
    public int orders_count;
    // ... Add other fields as necessary
}