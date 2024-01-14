package com.project.crawl.dao;

import com.project.crawl.controller.dto.C24CostcoProductXlsx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@RequiredArgsConstructor
@Repository
@Slf4j
public class C24XlsxDao {
    private final DSLContext context;

    public List<C24CostcoProductXlsx> getAvailableC24CostcoProductXlsxList() {
        return context.select(
                field("cp.idx").as("cp_idx"),
                field("c24.idx").as("c24_idx"),
                field("product_code"),
                field("cp.name").as("name"),
                field("name_en"),
                field("min_qty"),
                field("max_qty"),
                field("c24_code"),
                field("thumb_detail"),
                field("description_detail"),
                field("spec_info_table"),
                field("delivery_info"),
                field("refund_info"),
                field("thumb_main"),
                field("thumb_extra"),
                field("thumb_extra_filenames"),
                field("cp.costco_category_idx").as("costco_category_idx"),
                field("price"),
                field("sale_amount"),
                field("sale_period"),
                field("is_sale"),
                field("is_option"),
                field("is_member_only"),
                field("c24.status").as("c24_status"),
                field("c24c.c24_cate_no").as("c24_cate_no"),
                field("c24c.name").as("category_name")
        ).from(table("costco_product").as("cp"))
                .leftJoin(table("c24_product_test2").as("c24")).on(field("cp.product_code").eq(field("c24.costco_product_code")))
                .leftJoin(table("c24_category").as("c24c")).on(field("cp.costco_category_idx").eq(field("c24c.costco_category_idx")).and(field("c24c.status").eq(1)))
                .where(field("cp.status").eq(1).and(field("c24.status").eq(1)))
                .fetchInto(C24CostcoProductXlsx.class);
    }

    public List<C24CostcoProductXlsx> getFilteredUnavailableC24CostcoProductXlsxList() {
        Stream<C24CostcoProductXlsx> unavailableStream = context.select(
                        field("cp.idx").as("cp_idx"),
                        field("c24.idx").as("c24_idx"),
                        field("product_code"),
                        field("cp.name").as("name"),
                        field("name_en"),
                        field("min_qty"),
                        field("max_qty"),
                        field("c24_code"),
                        field("thumb_detail"),
                        field("description_detail"),
                        field("spec_info_table"),
                        field("delivery_info"),
                        field("refund_info"),
                        field("thumb_main"),
                        field("thumb_extra"),
                        field("thumb_extra_filenames"),
                        field("cp.costco_category_idx").as("costco_category_idx"),
                        field("price"),
                        field("sale_amount"),
                        field("sale_period"),
                        field("is_sale"),
                        field("is_option"),
                        field("is_member_only"),
                        field("c24.status").as("c24_status"),
                        field("c24c.c24_cate_no").as("c24_cate_no"),
                        field("c24c.name").as("category_name")
                ).from(table("costco_product").as("cp"))
                .leftJoin(table("c24_product_test2").as("c24")).on(field("cp.product_code").eq(field("c24.costco_product_code")))
                .leftJoin(table("c24_category").as("c24c")).on(field("cp.costco_category_idx").eq(field("c24c.costco_category_idx")).and(field("c24c.status").eq(1)))
                .where(field("cp.status").eq(0).or(field("c24.status").eq(0)))
                .and(field("c24.c24_code").isNotNull())
                .fetchStreamInto(C24CostcoProductXlsx.class);
        // filtered using checkForC24ProductMustAttributes()
        return unavailableStream.filter(C24CostcoProductXlsx::checkForC24ProductMustAttributes).collect(Collectors.toList());
    }

    public List<C24CostcoProductXlsx> getEntireUnavailableC24CostcoProductXlsxList() {
        Stream<C24CostcoProductXlsx> unavailableStream = context.select(
                        field("cp.idx").as("cp_idx"),
                        field("c24.idx").as("c24_idx"),
                        field("product_code"),
                        field("cp.name").as("name"),
                        field("name_en"),
                        field("min_qty"),
                        field("max_qty"),
                        field("c24_code"),
                        field("thumb_detail"),
                        field("description_detail"),
                        field("spec_info_table"),
                        field("delivery_info"),
                        field("refund_info"),
                        field("thumb_main"),
                        field("thumb_extra"),
                        field("thumb_extra_filenames"),
                        field("cp.costco_category_idx").as("costco_category_idx"),
                        field("price"),
                        field("sale_amount"),
                        field("sale_period"),
                        field("is_sale"),
                        field("is_option"),
                        field("is_member_only"),
                        field("c24.status").as("c24_status"),
                        field("c24c.c24_cate_no").as("c24_cate_no"),
                        field("c24c.name").as("category_name")
                ).from(table("costco_product").as("cp"))
                .leftJoin(table("c24_product_test2").as("c24")).on(field("cp.product_code").eq(field("c24.costco_product_code")))
                .leftJoin(table("c24_category").as("c24c")).on(field("cp.costco_category_idx").eq(field("c24c.costco_category_idx")))
                .where(field("cp.status").notEqual(1).or(field("c24.status").notEqual(1)))
                .and(field("c24.c24_code").isNotNull())
                .fetchStreamInto(C24CostcoProductXlsx.class);
        // filtered using checkForC24ProductMustAttributes()
        return unavailableStream.collect(Collectors.toList());
    }

    public List<C24CostcoProductXlsx> getAllC24CostcoProductXlsxList() {
        return context.select(
                        field("cp.idx").as("cp_idx"),
                        field("c24.idx").as("c24_idx"),
                        field("product_code"),
                        field("cp.name").as("name"),
                        field("name_en"),
                        field("min_qty"),
                        field("max_qty"),
                        field("c24_code"),
                        field("thumb_detail"),
                        field("description_detail"),
                        field("spec_info_table"),
                        field("delivery_info"),
                        field("refund_info"),
                        field("thumb_main"),
                        field("thumb_extra"),
                        field("thumb_extra_filenames"),
                        field("cp.costco_category_idx").as("costco_category_idx"),
                        field("price"),
                        field("sale_amount"),
                        field("sale_period"),
                        field("is_sale"),
                        field("is_option"),
                        field("is_member_only"),
                        field("c24.status").as("c24_status"),
                        field("c24c.c24_cate_no").as("c24_cate_no"),
                        field("c24c.name").as("category_name")
                ).from(table("costco_product").as("cp"))
                .leftJoin(table("c24_product_test2").as("c24")).on(field("cp.product_code").eq(field("c24.costco_product_code")))
                .leftJoin(table("c24_category").as("c24c")).on(field("cp.costco_category_idx").eq(field("c24c.costco_category_idx")).and(field("c24c.status").eq(1)))
                .fetchInto(C24CostcoProductXlsx.class);
    }
}
