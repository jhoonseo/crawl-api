package com.project.crawl.service.dto;

import lombok.Data;

import java.util.List;

@Data
public class SearchDeliveryInfo {
    public List<String> area_from;
    public double weight;
    public double suttle_weight;
    public boolean free_postage;
    // ... Add other fields as necessary

    public String getArea_from() {
        return String.join("|", area_from);
    }
}