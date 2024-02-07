package com.project.crawl.service.dto;

import lombok.Data;

import java.util.List;

@Data
public class SkuProp {
    public String prop_name;
    public String pid;
    public List<PropValue> propValues;
}
