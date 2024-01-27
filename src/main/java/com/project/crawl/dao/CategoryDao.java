package com.project.crawl.dao;

import com.project.crawl.controller.dto.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@RequiredArgsConstructor
@Repository
@Slf4j
public class CategoryDao {
    private final DSLContext context;

    public List<String> getCostcoCategoryKeyList() {
        return context
                .select(field("category"))
                .from("category_costco")
                .where(field("status").eq(1))
                .fetchInto(String.class);
    }

    public List<Category> getCostcoCategoryList() {
        return context
                .select(field("idx"), field("category"))
                .from("category_costco")
                .where(field("status").eq(1))
                .fetchInto(Category.class);
    }

    public List<Category> getAllCostcoCategoryList() {
        return context
                .select(field("idx"), field("category"))
                .from("category_costco")
                .fetchInto(Category.class);
    }


    public int updateCostcoCategoryName(Category category) {
        return context.update(table("category_costco"))
                .set(field("name"), category.getName())
                .where(field("idx").eq(category.getIdx()))
                .execute();
    }
}
