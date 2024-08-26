package com.project.crawl.controller;

import com.project.crawl.controller.dto.C24ProductChunk;
import com.project.crawl.controller.dto.C24ProductXlsx;
import com.project.crawl.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/file")
@Tag(name = "파일 처리")
public class FileController {

    private final ResizeService resizeService;
    private final FtpService ftpService;
    private final S3Service s3Service;
    private final ExcelService excelService;
    private final RestrictedKeywordService restrictedKeywordService;
    private final C24XlsxService c24XlsxService;

    @PostMapping("/images/resize")
    public List<String> resizeDailyImages(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate today
    ) throws IOException {
        String formatToday = today.format(DateTimeFormatter.ofPattern("yyMMdd"));

        // todo 에러 파일을 가지고 있는 상품을 가져와서 파일명만 대조 후, 일괄 비활성화
        return resizeService.resizeDailyDirectoryImages(formatToday);
    }

    @GetMapping("/images/resizeAll")
    public List<String> resizeAllImages(LocalDate today) throws IOException {
        String formatToday = today.format(DateTimeFormatter.ofPattern("yyMMdd"));

        // todo 에러 파일을 가지고 있는 상품을 가져와서 파일명만 대조 후, 일괄 비활성화
        return resizeService.resizeEntireDirectoryImages(formatToday);
    }

    @PostMapping("/images/ftp/daily")
    public void ftpUploadDailyImages(@RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate today) throws IOException {
        String formatToday = today.format(DateTimeFormatter.ofPattern("yyMMdd"));
        String[][] pathArray = ftpService.getPathArray(formatToday);

        ftpService.connectFtp(); // 연결은 한 번만 실행

        try {
            for (String[] path : pathArray) {
                ftpService.uploadDirectoryFiles(path[0], path[1]);
            }
        } finally {
            ftpService.quitFtp(); // 모든 업로드가 끝난 후 연결 해제
        }
    }

    @GetMapping("/images/ftp/all")
    public void ftpUploadAllImages() throws IOException {
        String[][] pathArray = ftpService.getTotalPathArray();

        for (String[] path : pathArray) {
            ftpService.uploadDirectoryFiles(path[0], path[1]);
        }
    }

    @PostMapping("/images/s3/daily")
    public void s3UploadImagesPublic(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate today
    ) {
        String formatToday = today.format(DateTimeFormatter.ofPattern("yyMMdd"));
        String path = String.join("/", "daily", formatToday, "images");
        s3Service.uploadDirectoryFilesPublic(path);
    }

    @PostMapping("/excel/export")
    public void exportExcel(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate today
    ) {
        String formatToday = today.format(DateTimeFormatter.ofPattern("yyMMdd"));
        // 판매 가능 상품 조회
        List<C24ProductXlsx> availableList = c24XlsxService.getAvailableC24CostcoProductXlsxList();
        // 판매 불가능 상품 조회
        // List<C24ProductXlsx> unavailableList = c24XlsxService.getUnavailableC24CostcoProductXlsxList();
        List<C24ProductXlsx> unavailableList = c24XlsxService.getEntireUnavailableC24CostcoProductXlsxList();
        // restricted keywords 가져오기
        List<String> restrictedKeywordList = restrictedKeywordService.getResetrictedKeywordList();
        // 판매 가능 상품 중, restricted keyword filter 하여 판매 불가능 상품에 추가
        Iterator<C24ProductXlsx> iterator = availableList.iterator();
        while (iterator.hasNext()) {
            C24ProductXlsx product = iterator.next();
            String productName = product.getName();

            // restricted keyword filter
            for (String restrictedKeyword : restrictedKeywordList) {
                if (productName.contains(restrictedKeyword)) {
                    unavailableList.add(product); // 판매 불가능 상품에 추가
                    iterator.remove(); // availableList 에서 제거
                    break;
                }
            }
        }

        // 판매 가능 상품 엑셀 만들기
        List<C24ProductChunk> availableChunks = excelService.divideList(availableList, 800);
        for (C24ProductChunk chunk : availableChunks) {
            excelService.generateC24ProductExcels(chunk, formatToday, true, "costco");
        }

        // todo : 24.01.30 판매불가능 상품 엑셀에서 아래의 상품들 누락 이슈
        //P000BKIW 동원 명품혼합 V10 x 4
        //P000BCEY AMT 샤프 시리즈 스테인리스 양수웍 20cm
        //P000BLDB 백종원의 빽햄 선물세트 x 4세트
        //P000BLCZ 백종원의 빽햄 세트 200g x 9개
        //품절되었습니다. 카페 24에서 판매안함 진열안함 처리하였습니다
        // 판매 불가능 상품 엑셀 만들기
        List<C24ProductChunk> unavailableChunks = excelService.divideList(unavailableList, 800);
        for (C24ProductChunk chunk : unavailableChunks) {
            excelService.generateC24ProductExcels(chunk, formatToday, false, "costco");
        }
    }

    @PostMapping("/excel/unavailable/export")
    public void exportUnavailableExcel(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate today
    ) {
        String formatToday = today.format(DateTimeFormatter.ofPattern("yyMMdd"));
        // 판매 불가능 상품 조회
        List<C24ProductXlsx> availableList = c24XlsxService.getAvailableC24CostcoProductXlsxList();
        // 판매 불가능 상품 조회
        List<C24ProductXlsx> unavailableList = c24XlsxService.getEntireUnavailableC24CostcoProductXlsxList();
        // restricted keywords 가져오기
        List<String> restrictedKeywordList = restrictedKeywordService.getResetrictedKeywordList();
        // 판매 가능 상품 중, restricted keyword filter 하여 판매 불가능 상품에 추가
        Iterator<C24ProductXlsx> iterator = availableList.iterator();
        while (iterator.hasNext()) {
            C24ProductXlsx product = iterator.next();
            String productName = product.getName();

            // restricted keyword filter
            for (String restrictedKeyword : restrictedKeywordList) {
                if (productName.contains(restrictedKeyword)) {
                    unavailableList.add(product); // 판매 불가능 상품에 추가
                    iterator.remove(); // availableList 에서 제거
                    break;
                }
            }
        }

        // 판매 불가능 상품 엑셀 만들기
        List<C24ProductChunk> unavailableChunks = excelService.divideList(unavailableList, 800);
        for (C24ProductChunk chunk : unavailableChunks) {
            excelService.generateC24ProductExcels(chunk, formatToday, false, "costco");
        }
    }

}
