package com.project.crawl.service.dto;

import lombok.Data;

import java.util.List;

@Data
public class DetailData {
    public long item_id;
    public String title;
    public int category_id;
    public String root_category_id;
    public String currency;
    public String offer_unit;
    public List<ProductProp> product_props;
    public List<String> main_imgs;
    public String video_url;
    public String detail_url;
    public String sale_count;
    public DetailShopInfo shop_info;
    public DetailDeliveryInfo delivery_info;
    public String sku_price_scale;
    public String sku_price_scale_original;
    public SkuPriceRange sku_price_range;
    public List<SkuProp> sku_props;
    public List<Sku> skus;
    private int status;
    private int idx;
    // Assuming other fields from the initial SearchData class are also included
}
