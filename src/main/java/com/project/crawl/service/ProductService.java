package com.project.crawl.service;
import com.project.crawl.controller.dto.CostcoProduct;
import com.project.crawl.dao.ProductDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductDao productDao;

    public LocalDate checkParams(LocalDate crawlDate) {
        if (Objects.isNull(crawlDate)) {
            return LocalDate.now();
        } else {
            return crawlDate;
        }
    }

    public Map<Integer, CostcoProduct> getAllCostcoProductMap() {
        return productDao.getAllCostcoProductMap();
    }

    public List<Integer> getAllCostcoProductCodeList() {
        return productDao.getAllCostcoProductCodeList();
    }

    public List<CostcoProduct> getCostcoProductListAfterDate(LocalDate crawlDate) {
        return productDao.getCostcoProductListAfterDate(crawlDate);
    }

    public CostcoProduct getCostcoProductByProductCode(Integer productCode) {
        return productDao.getCostcoProductByProductCode(productCode);
    }

    public Integer getCostcoProductIdxByProductCode(Integer productCode) {
        return productDao.getCostcoProductIdxByProductCode(productCode);
    }

    public void updateCostcoProduct(CostcoProduct costcoProduct) {
        productDao.updateCostcoProduct(costcoProduct);
    }

    public void updateCostcoProductListStatus(List<Integer> costcoProductCodeSet, Integer status) {
        productDao.updateCostcoProductListStatus(costcoProductCodeSet, status);
    }

    public void updateCostcoProductStatus(Object productCode, Integer status) {
        productDao.updateCostcoProductStatus(productCode, status);
    }

    public void insertCostcoProduct(CostcoProduct costcoProduct) {
        productDao.insertCostcoProduct(costcoProduct);
    }

    public void insertCostcoProductCollection(Collection<CostcoProduct> costcoProductCollection) {
        productDao.insertCostcoProductCollection(costcoProductCollection);
    }


}
