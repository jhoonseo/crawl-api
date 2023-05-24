package com.costco.crawl.controller;


import com.costco.crawl.service.S3Service;
import com.costco.crawl.service.FtpService;
import com.costco.crawl.service.ResizeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1/costco/file")
@Tag(name = "파일 처리")
public class FileController {

    private final ResizeService resizeService;
    private final FtpService ftpService;
    private final S3Service s3Service;

    @GetMapping("/images/resize")
    public List<String> resizeDirectoryImages() throws IOException {
        LocalDate today = LocalDate.now();
        String formatToday = today.format(DateTimeFormatter.ofPattern("MMdd"));

        // 에러 파일을 가지고 있는 상품을 가져와서 파일명만 대조 후, 일괄 비활성화
        return resizeService.resizeDailyDirectoryImages(formatToday);
    }

    @GetMapping("/images/ftp/upload")
    public void ftpUploadImages() throws IOException {
        LocalDate today = LocalDate.now();
        String formatToday = today.format(DateTimeFormatter.ofPattern("MMdd"));

        String[][] pathArray = ftpService.getPathArray(formatToday);

        for (String[] path : pathArray) {
            ftpService.uploadDirectoryFiles(path[0], path[1]);
        }
    }

    @GetMapping("/images/s3/upload")
    public void s3UploadImages() {
        LocalDate today = LocalDate.now();
        String formatToday = today.format(DateTimeFormatter.ofPattern("MMdd"));

        s3Service.uploadDirectoryFiles(formatToday);
    }

}
