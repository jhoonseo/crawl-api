package com.project.crawl.service.dto;

import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class DescriptionData {
    private long item_id;
    private List<String> detail_imgs;
    private String detail_html;

    private int idx;
    private String img_url_text;
    private String img_filename_text;
    private String img_description_detail;
    private String status;

    private List<String> img_filename_list;

    public List<String> getDetailImgFilenames() {
        return this.detail_imgs.stream()
                .map(url -> {
                    String[] parts = url.split("/");
                    return parts[parts.length - 1]; // URL의 마지막 부분(파일명)을 반환
                })
                .collect(Collectors.toList());
    }
}
