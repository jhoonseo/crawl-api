package com.project.crawl.service.dto;

import lombok.Data;

@Data
public class QuantityPrice {
    public String begin_num;
    public String end_num;
    public String price;
    // ... Add other fields as necessary
    public Integer getEnd_num() {
        if (end_num.isEmpty()) {
            return null;
        } else {
            return Integer.parseInt(end_num);
        }
    }
}
