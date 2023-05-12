package com.costco.crawl.dao;

import com.costco.crawl.controller.dto.Category;
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
                .from("costco_category")
                .where(field("status").eq(1))
                .fetchInto(String.class);
    }

    public List<Category> getCostcoCategoryList() {
        return context
                .select(field("idx"), field("category"))
                .from("costco_category")
                .where(field("status").eq(1))
                .fetchInto(Category.class);
    }

    public int updateCostcoCategoryName(Category category) {
        return context.update(table("costco_category"))
                .set(field("name"), category.getName())
                .where(field("idx").eq(category.getIdx()))
                .and((field("name").notEqual(category.getName())).or(field("name").isNull()))
                .execute();
    }
}
