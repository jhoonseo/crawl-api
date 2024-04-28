package com.project.crawl.service.dto;

import lombok.Data;

@Data
public class DescriptionResponse {
    public int code;
    public String msg;
    public DescriptionData data;
}
