package com.project.crawl.service;
import com.project.crawl.controller.dto.CostcoProduct;
import com.project.crawl.dao.CostcoProductDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CostcoProductService {
    private final CostcoProductDao costcoProductDao;

    public LocalDate checkParams(LocalDate crawlDate) {
        if (Objects.isNull(crawlDate)) {
            return LocalDate.now();
        } else {
            return crawlDate;
        }
    }

    public Map<Integer, CostcoProduct> getAllCostcoProductMap() {
        return costcoProductDao.getAllCostcoProductMap();
    }

    public List<Integer> getAllCostcoProductCodeList() {
        return costcoProductDao.getAllCostcoProductCodeList();
    }

    public List<CostcoProduct> getCostcoProductListAfterDate(LocalDate crawlDate) {
        return costcoProductDao.getCostcoProductListAfterDate(crawlDate);
    }

    public CostcoProduct getCostcoProductByProductCode(Integer productCode) {
        return costcoProductDao.getCostcoProductByProductCode(productCode);
    }

    public Integer getCostcoProductIdxByProductCode(Integer productCode) {
        return costcoProductDao.getCostcoProductIdxByProductCode(productCode);
    }

    public void updateCostcoProduct(CostcoProduct costcoProduct) {
        costcoProductDao.updateCostcoProduct(costcoProduct);
    }

    public void updateCostcoProductListStatus(List<Integer> costcoProductCodeSet, Integer status) {
        costcoProductDao.updateCostcoProductListStatus(costcoProductCodeSet, status);
    }

    public void updateCostcoProductStatus(Object productCode, Integer status) {
        costcoProductDao.updateCostcoProductStatus(productCode, status);
    }

    public void insertCostcoProduct(CostcoProduct costcoProduct) {
        costcoProductDao.insertCostcoProduct(costcoProduct);
    }

    public void insertCostcoProductCollection(Collection<CostcoProduct> costcoProductCollection) {
        costcoProductDao.insertCostcoProductCollection(costcoProductCollection);
    }

}
