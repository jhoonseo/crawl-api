package com.project.crawl.dao;

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
public class RestrictedKeywordDao {
    private final DSLContext context;

    public List<String> getResetrictedKeywordList() {
        return context.select(field("keyword"))
                .from(table("restricted_keyword"))
                .where(field("status").eq(1))
                .fetchInto(String.class);
    }
}
