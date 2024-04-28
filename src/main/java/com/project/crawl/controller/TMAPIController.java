package com.project.crawl.controller;

import com.project.crawl.service.TMAPIService;
import com.project.crawl.service.dto.*;
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

    // todo 1 ✅: running sequence : keyword -> detail & description
    // todo 2 ✅: -> image download and image translation
    // todo 3 : -> text translate and insert as c24 format
    // todo 4 : -> s3 upload & ftp upload -> excel export

    @GetMapping("/1688/search-keyword")
    public KeywordResponse searchItemsByKeyword(
            String keyword,
            Integer page
    ) throws IOException, InterruptedException {
        KeywordResponse keywordResponse = tmapiService.getKeywordRelatedItemsResponse(keyword, page);
        // todo item 존재 여부 파악 후 api 결과 insert 여부 결정. 현재는 inserted 여부만 체크함.
        List<Long> existingKeywordItemIdList = tmapiService.getKeywordItemIdList1688();
        List<KeywordItem> sortedKeywordItemList = tmapiService
                .getSortedKeywordItemList(keywordResponse.getData().getItems(), existingKeywordItemIdList);
        tmapiService.insertKeywordItemInformation(sortedKeywordItemList, keyword);
        return keywordResponse;
    }

    @GetMapping("/1688/search-detail")
    public DetailData searchDetailByItemId(
            Long itemId
    ) throws IOException, InterruptedException {
        // todo item 존재 여부 파악 후 api call 여부 결정. 나중에는 update 진행 여부도 여기서 결정할 필요가 있다. (batch)
        DetailData detailData = tmapiService.getExistingDetailDataByItemId1688(itemId);
        if (detailData != null) {
            return detailData;
        }
        DetailResponse detailApiResponse = tmapiService.getDetailItemResponse1688(itemId);
        tmapiService.insertDetailInfo1688(detailApiResponse.getData());
        return detailApiResponse.getData();
    }

    @GetMapping("/1688/search-description")
    public DescriptionData searchDescriptionByItemId(
            Long itemId
    ) throws IOException, InterruptedException {
        DescriptionData descriptionData = tmapiService.getExistingDescriptionDataByItemId1688(itemId);
        if (descriptionData != null) {
            return descriptionData;
        }
        DescriptionResponse descriptionResponse = tmapiService.getDescriptionItemResponse1688(itemId);
        tmapiService.insertDescriptionInfo1688(descriptionResponse.getData());
        return descriptionResponse.getData();
    }

    @GetMapping("/1688/search-detail-description")
    public void searchNewDetailAndDescription() {
        // todo Keyword item 중에서 Detail item 에 존재하지 않는 item 만 api call
        // todo ShopInfo detail & keyword 클래스 및 테이블 통합하기 shop
        List<Long> keywordItemIdList = tmapiService.getKeywordItemIdList1688();
        List<Long> detailItemIdList = tmapiService.getDetailItemIdList1688();
        List<Long> descriptionItemIdList = tmapiService.getDescriptionItemIdList1688();

        // Filter out items in keywordItemIdList that are not in detailItemIdList
        List<Long> filteredKeywordItemIdList = keywordItemIdList.stream()
                .filter(itemId -> !detailItemIdList.contains(itemId))
                .toList();

        // Filter out items in keywordItemIdList that are not in descriptionItemIdList
        List<Long> filteredDescriptionItemIdList = keywordItemIdList.stream()
                .filter(itemId -> !descriptionItemIdList.contains(itemId))
                .toList();

        // Process each filtered keyword item ID
        filteredKeywordItemIdList.forEach(keywordItemId -> {
            DetailResponse detailApiResponse = null;
            try {
                detailApiResponse = tmapiService.getDetailItemResponse1688(keywordItemId);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            tmapiService.insertDetailInfo1688(detailApiResponse.getData());
        });

        // Process each filtered description item ID
        filteredDescriptionItemIdList.forEach(keywordItemId -> {
            DescriptionResponse descriptionResponse = null;
            try {
                descriptionResponse = tmapiService.getDescriptionItemResponse1688(keywordItemId);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            tmapiService.insertDescriptionInfo1688(descriptionResponse.getData());
        });
    }
}
