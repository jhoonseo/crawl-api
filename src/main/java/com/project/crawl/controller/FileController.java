package com.project.crawl.controller;

import com.project.crawl.controller.dto.C24CostcoProductChunk;
import com.project.crawl.controller.dto.C24CostcoProductXlsx;
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
        String formatToday = today.format(DateTimeFormatter.ofPattern("MMdd"));

        // todo 에러 파일을 가지고 있는 상품을 가져와서 파일명만 대조 후, 일괄 비활성화
        return resizeService.resizeDailyDirectoryImages(formatToday);
    }

    @GetMapping("/images/resizeAll")
    public List<String> resizeAllImages(LocalDate today) throws IOException {
        String formatToday = today.format(DateTimeFormatter.ofPattern("MMdd"));

        // todo 에러 파일을 가지고 있는 상품을 가져와서 파일명만 대조 후, 일괄 비활성화
        return resizeService.resizeEntireDirectoryImages(formatToday);
    }

    @PostMapping("/images/ftp/daily")
    public void ftpUploadDailyImages(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate today
    ) throws IOException {
        String formatToday = today.format(DateTimeFormatter.ofPattern("MMdd"));

        String[][] pathArray = ftpService.getPathArray(formatToday);

        for (String[] path : pathArray) {
            ftpService.uploadDirectoryFiles(path[0], path[1]);
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
        String formatToday = today.format(DateTimeFormatter.ofPattern("MMdd"));
        String path = String.join("/", "daily", formatToday, "images");
        s3Service.uploadDirectoryFilesPublic(path);
    }

    @PostMapping("/excel/export")
    public void exportExcel(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate today
    ) {
        String formatToday = today.format(DateTimeFormatter.ofPattern("MMdd"));
        // 판매 가능 상품 조회
        List<C24CostcoProductXlsx> availableList = c24XlsxService.getAvailableC24CostcoProductXlsxList();
        // 판매 불가능 상품 조회
        List<C24CostcoProductXlsx> unavailableList = c24XlsxService.getUnavailableC24CostcoProductXlsxList();
        // restricted keywords 가져오기
        List<String> restrictedKeywordList = restrictedKeywordService.getResetrictedKeywordList();
        // 판매 가능 상품 중, restricted keyword filter 하여 판매 불가능 상품에 추가
        Iterator<C24CostcoProductXlsx> iterator = availableList.iterator();
        while (iterator.hasNext()) {
            C24CostcoProductXlsx product = iterator.next();
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
        List<C24CostcoProductChunk> availableChunks = excelService.divideList(availableList, 799);
        for (C24CostcoProductChunk chunk : availableChunks) {
            excelService.generateC24ProductExcels(chunk, formatToday, true, "costco");
        }

        // 판매 불가능 상품 엑셀 만들기
        List<C24CostcoProductChunk> unavailableChunks = excelService.divideList(unavailableList, 799);
        for (C24CostcoProductChunk chunk : unavailableChunks) {
            excelService.generateC24ProductExcels(chunk, formatToday, false, "costco");
        }
    }


}
