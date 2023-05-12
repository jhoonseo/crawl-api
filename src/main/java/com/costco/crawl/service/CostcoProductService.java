package com.costco.crawl.service;
import com.costco.crawl.controller.dto.CostcoProduct;
import com.costco.crawl.dao.CostcoProductDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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

    public Map<Object, CostcoProduct> getAllCostcoProductMap() {
        return costcoProductDao.getAllCostcoProductMap();
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

    public void updateCostcoProductsStatus(Set<Object> costcoProductCodeSet, Integer status) {
        costcoProductDao.updateCostcoProductsStatus(costcoProductCodeSet, status);
    }


    public void insertCostcoProduct(CostcoProduct costcoProduct) {
        costcoProductDao.insertCostcoProduct(costcoProduct);
    }

}
