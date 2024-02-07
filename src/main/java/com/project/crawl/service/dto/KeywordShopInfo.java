package com.project.crawl.service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class KeywordShopInfo {
    public String login_id;
    public String member_id;
    public String biz_type;
    public String company_name;
    public List<String> service_tags; // Assuming it's a list of strings
    public boolean tp_member;
    public int tp_year;
    public boolean factory_inspection;
    public String shop_repurchase_rate;
    public KeywordShopScoreInfo sore_info;
    // ... Add other fields as necessary
    public BigDecimal getShop_repurchase_rate() {
        // '%' 문자를 제거합니다.
        String numericString = shop_repurchase_rate.replace("%", "");

        // 문자열을 BigDecimal로 변환합니다.
        BigDecimal rate = new BigDecimal(numericString);

        // 반환하기 전에 100으로 나누지 않고 그대로 반환합니다.
        // 호출하는 측에서는 이 값을 그대로 decimal(5,2) 타입의 컬럼에 삽입할 수 있습니다.
        return rate;
    }

}