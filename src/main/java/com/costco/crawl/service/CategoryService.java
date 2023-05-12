package com.costco.crawl.service;

import com.costco.crawl.controller.dto.Category;
import com.costco.crawl.dao.CategoryDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryDao categoryDao;

    public List<String> getCostcoCategoryKeyList() {
        return categoryDao.getCostcoCategoryKeyList();
    }

    public List<Category> getCostcoCategoryList() {
        return categoryDao.getCostcoCategoryList();
    }

    public int updateCostcoCategoryName(Category category) {
        if (category.getName() != null) {
            return categoryDao.updateCostcoCategoryName(category);
        } else {
            return 0;
        }
    }


}
