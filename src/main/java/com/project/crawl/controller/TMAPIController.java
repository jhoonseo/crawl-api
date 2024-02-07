package com.project.crawl.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.crawl.service.TMAPIService;
import com.project.crawl.service.dto.DetailResponse;
import com.project.crawl.service.dto.KeywordItem;
import com.project.crawl.service.dto.KeywordResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/tmapi")
public class TMAPIController {
    private final TMAPIService tmapiService;

    @GetMapping("/1688/search-keyword")
    public KeywordResponse searchItemsByKeyword(
            String keyword,
            Integer page
    ) throws IOException, InterruptedException {
        KeywordResponse keywordApiKeywordResponse = tmapiService.getKeywordRelatedItemsResponse(keyword, page);

        List<KeywordItem> keywordItemList = keywordApiKeywordResponse.getData().getItems();
        tmapiService.insertKeywordItemInformation(keywordItemList, keyword);
        return keywordApiKeywordResponse;
    }

    @GetMapping("/1688/search-detail-all")
    public void searchDetailAllKeywordItem() {
        List<Long> keywordItemIdList = tmapiService.getKeywordItemIdList();
        System.out.println(keywordItemIdList);
        List<Long> detailItemIdList = tmapiService.getDetailItemIdList();
        System.out.println(detailItemIdList);
        keywordItemIdList.removeAll(detailItemIdList);
        System.out.println(keywordItemIdList);
    }

    @GetMapping("/1688/search-detail")
    public void searchDetailByItemId(
            Long itemId
    ) throws IOException, InterruptedException {
        DetailResponse detailApiResponse = tmapiService.getDetailItemResponse(itemId);
        System.out.println(detailApiResponse);
/*
        tmapiService.insertDetailInfo1688(detailApiResponse.getData());
 */
    }

}
