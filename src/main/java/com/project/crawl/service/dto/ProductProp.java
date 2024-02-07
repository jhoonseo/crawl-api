package com.project.crawl.service.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ProductProp {
    private Map<String, String> properties = new HashMap<>();

    @JsonAnySetter
    public void addProperty(String key, String value) {
        properties.put(key, value);
    }
}