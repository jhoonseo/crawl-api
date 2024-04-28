package com.project.crawl.service.dto;

import lombok.Data;

@Data
public class KeywordShopScoreInfo {
    public double composite_new_score;
    public double composite_score;
    public double consultation_score;
    public double dispute_score;
    public double logistics_score;
    public double return_score;
    private Integer shop_idx;
    private String shop_member_id;
    // ... Add other fields as necessary
}