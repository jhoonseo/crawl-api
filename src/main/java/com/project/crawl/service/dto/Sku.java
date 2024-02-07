package com.project.crawl.service.dto;

import lombok.Data;

@Data
public class Sku {
    public String skuid;
    public String specid;
    public double sale_price;
    public double origin_price;
    public int stock;
    public int sale_count;
    public String props_ids;
    public String props_names;
}