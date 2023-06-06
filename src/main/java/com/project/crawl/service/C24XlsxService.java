package com.project.crawl.service;

import com.project.crawl.controller.dto.C24CostcoProductXlsx;
import com.project.crawl.dao.C24XlsxDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class C24XlsxService {
    private final C24XlsxDao c24XlsxDao;
    public List<C24CostcoProductXlsx> getAvailableC24CostcoProductXlsxList() {
        return c24XlsxDao.getAvailableC24CostcoProductXlsxList();
    }

    public List<C24CostcoProductXlsx> getUnavailableC24CostcoProductXlsxList() {
        return c24XlsxDao.getFilteredUnavailableC24CostcoProductXlsxList();
    }

    public List<C24CostcoProductXlsx> getAllC24CostcoProductXlsxList() {
        return c24XlsxDao.getAllC24CostcoProductXlsxList();
    }
}
