package com.project.crawl.controller.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Data
public class CrawlProduct {
    private String name;
    private String nameEn;
    private int isSale;
    private int price;

//    category_idx: int = 0
//    product_idx: int = 0
//    category_status: str = None
//    product_status: str = None
//    category_price: int = 0
//    product_price: int = 0
//    category_created_date_time: str = None
//    category_updated_date_time: str = None
//    product_created_date_time: str = None
//    product_updated_date_time: str = None
//    c24_p_code: str = None
//    product_url: str = None
//    cate_sale_amount: int = 0
//    cate_sale_period: str = None
//    crawl_date_time: str = None
//    db_thumb_main: str = None
//    db_thumb_extra: str = None
//    product_is_sale: int = 0
//    category_is_sale: int = 0
//    product_sale_amount: int = 0
//    category_sale_amount: int = 0
//    category_name: str = None
//    product_is_option: int = 0
//    product_is_color_option: int = 0
//    product_is_multi_option: int = 0
//    product_options: str = None
//    product_name: str = ''
//    c_product_name: str = None
//
//    category_no: str = ''
//    sort_appear: str = ''
//    product_type: str = ''
//    is_restricted: bool = False


    private static final Map<Character, Character> specialCharacterMap =
            Map.of(
                    '&', '+',
                    ',', '-'
            );
    public void setNames(String name, String nameEn) {
        Map<Character, Character> specialCharacterMap = new HashMap<>();
        specialCharacterMap.put('&', '+');
        specialCharacterMap.put(',', '-');

        for (Map.Entry<Character, Character> entry : specialCharacterMap.entrySet()) {
            this.name = name.replace(entry.getKey(), entry.getValue());
            this.nameEn = nameEn.replace(entry.getKey(), entry.getValue());
        }
    }

}
