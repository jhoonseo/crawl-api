package com.project.crawl.service;

import com.project.crawl.controller.dto.C24Code;
import com.project.crawl.controller.dto.C24CostcoProduct;
import com.project.crawl.controller.dto.C24CostcoProductGroup;
import com.project.crawl.dao.C24ProductDao;
import com.project.crawl.exceptions.CrawlException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class C24ProductService {
    private final C24ProductDao c24ProductDao;

    public List<C24CostcoProduct> getAvailableC24CostcoProductList() {
        return c24ProductDao.getAvailableC24CostcoProductList();
    }

    public String getLastC24Code() {
        return c24ProductDao.getLastC24Code();
    }

    public List<Integer> getDisablingIdxList() {
        return c24ProductDao.getDisablingIdxList();
    }

    public List<C24CostcoProduct> getC24CostcoProductListForExcel() {
        return c24ProductDao.getC24CostcoProductListForExcel();
    }

    public void updateC24Group(C24CostcoProductGroup c24Group) {
        c24ProductDao.updateC24Group(c24Group);
    }

    public void updateStatusByProductCode(Integer productCode, Integer status) {
        c24ProductDao.updateStatusByProductCode(productCode, status);
    }

    public void updateStatusByIdxList(List<Integer> idxList, Integer status) {
        if (idxList.size() == 0) {
            return;
        }
        c24ProductDao.updateStatusByIdxList(idxList, status);
    }

    public void insertC24Product(C24CostcoProduct c24P) {
        c24ProductDao.insertC24Product(c24P);
    }

    public void manageC24Code(C24Code c24Code) throws Exception {
        char a = c24Code.getA();
        char b = c24Code.getB();
        char c = c24Code.getC();
        char d = c24Code.getD();

        if (a < 'Z') {
            a = (char) (a + 1);
        } else if (a == 'Z' && (b == '0' || b == 0)) {
            a = 'A';
            b = 'B';
        } else if (a == 'Z' && b != 'Z') {
            a = 'A';
            b = (char) (b + 1);
        } else if (a == 'Z' && (c == '0' || c == 0)) {
            a = 'A';
            b = 'A';
            c = 'B';
        } else if (a == 'Z' && c != 'Z') {
            a = 'A';
            b = 'A';
            c = (char) (c + 1);
        } else if (a == 'Z' && (d == '0' || d == 0)) {
            a = 'A';
            b = 'A';
            c = 'A';
            d = 'B';
        } else if (a == 'Z' && d != 'Z') {
            a = 'A';
            b = 'A';
            c = 'A';
            d = (char) (d + 1);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(d).append(c).append(b).append(a);
            log.error("manageC24Code fail {}", sb);
            throw new CrawlException(CrawlException.Type.BAD_REQUEST, String.format("manageC24Code fail %s", sb));
        }

        c24Code.setCharsByChars(a, b, c, d);
    }

    public boolean checkForSameObjects(List<C24CostcoProduct> productList) {
        if (productList.size() <= 1) {
            return true; // 하나 이하의 객체이므로 모두 같다고 판단
        }

        C24CostcoProduct firstProduct = productList.get(0);

        for (int i = 1; i < productList.size(); i++) {
            C24CostcoProduct currentProduct = productList.get(i);

            // 객체가 서로 다른지 비교
            if (!firstProduct.equals(currentProduct)) {
                return false; // 객체가 서로 다름
            }
        }

        return true; // 모든 객체가 동일함
    }

}
