package com.project.crawl.service;

import com.project.crawl.controller.dto.Category;
import com.project.crawl.dao.CategoryDao;
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

    public List<Category> getAllCostcoCategoryList() {
        return categoryDao.getAllCostcoCategoryList();
    }

    public int updateCostcoCategoryName(Category category) {
        if (category.getName() != null) {
            return categoryDao.updateCostcoCategoryName(category);
        } else {
            return 0;
        }
    }


}
