package com.project.crawl.service.dto;

import lombok.Data;

@Data
public
class DetailResponse {
    public int code;
    public String msg;
    public DetailData data;
}
