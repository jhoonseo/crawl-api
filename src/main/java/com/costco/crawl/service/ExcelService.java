package com.costco.crawl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelService {
    private Workbook workbook;
    private Sheet sheet;
    public void createWorkbook() {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Sheet1");
    }

    public void setHeader() {

    }

    public void closeWorkbook() {
        try {
            workbook.close();
        } catch (Exception ignored) {
        }
    }
}
