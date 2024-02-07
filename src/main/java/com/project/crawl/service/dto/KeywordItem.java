package com.project.crawl.service.dto;

import lombok.Data;

import java.util.List;

@Data
public class KeywordItem {
    public long item_id; // changed from String to long based on the JSON provided
    private String keyword;
    public String product_url;
    public String title;
    public String img;
    public List<String> category_path;
    public String price;
    public PriceInfo price_info;
    public int quantity_begin; // changed from String to int based on the JSON provided
    public List<QuantityPrice> quantity_prices;
    public SaleInfo sale_info;
    public String type;
    public String unit;
    public SearchDeliveryInfo delivery_info;
    public double item_repurchase_rate; // changed from String to double based on the JSON provided
    public double goods_score; // changed from String to double based on the JSON provided
    public double image_dsm_score; // changed from String to double based on the JSON provided
    public int primary_rank_score; // changed from String to int based on the JSON provided
    public List<String> buyer_protections; // Assuming it's a list of strings
    public boolean super_new_product;
    public int byr_inquiry_uv; // changed from String to int based on the JSON provided
    public KeywordShopInfo shop_info;
    // ... Add other fields as necessary
}
