package com.costco.crawl.dao;

import com.costco.crawl.controller.dto.C24CostcoProduct;
import com.costco.crawl.controller.dto.C24CostcoProductGroup;
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
public class C24ProductDao {
    private final DSLContext context;


    public List<C24CostcoProduct> getAvailableC24CostcoProductList() {
        return context.select(
                field("cp.idx").as("cp_idx"),
                field("c24.idx").as("c24_idx"),
                field("product_code"),
                field("name"),
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
                field("costco_category_idx"),
                field("price"),
                field("sale_amount"),
                field("sale_period"),
                field("is_sale"),
                field("is_option"),
                field("is_member_only"),
                field("c24.status").as("c24_status")
        ).from(table("costco_product").as("cp"))
                .leftJoin(table("c24_product_test").as("c24")).on(field("cp.product_code").eq(field("c24.costco_product_code")))
                .where(field("cp.status").eq(1))
                .fetchInto(C24CostcoProduct.class);
    }

    public String getLastC24Code() {
        return context.select(field("c24_code"))
                .from(table("c24_product_test"))
                .orderBy(field("c24_code").desc())
                .limit(1)
                .fetchOneInto(String.class);
    }
    public List<Integer> getDisablingIdxList() {
        return context.select(field("c24.idx"))
                .from(table("c24_product_test").as("c24"))
                .leftJoin(table("costco_product").as("cp"))
                .on(field("c24.costco_product_code").eq("cp.product_code"))
                .where(
                        field("c24.status").eq(1)
                                .and(field("cp.status").eq(0)))
                .fetchInto(Integer.class);
    }

    public void updateC24Group(C24CostcoProductGroup c24Group) {
        C24CostcoProduct c24P = c24Group.getCommonC24CostcoProduct();
        context.update(table("c24_product_test"))
                .set(field("thumb_main"), c24P.getThumbMain())
                .set(field("thumb_extra"), c24P.getThumbExtra())
                .set(field("thumb_extra_filenames"), c24P.getThumbExtraFilenames())
                .set(field("description_detail"), c24P.getDescriptionDetail())
                .set(field("thumb_detail"), c24P.getThumbDetail())
                .set(field("spec_info_table"), c24P.getSpecInfoTable())
                .set(field("delivery_info"), c24P.getDeliveryInfo())
                .set(field("refund_info"), c24P.getRefundInfo())
                .set(field("status"), c24P.getC24Status()).where(
                        field("costco_product_code").eq(c24Group.getProductCode())
                ).execute();
    }

    public void updateStatusByIdxList(List<Integer> idxList, Integer status) {
        context.update(table("c24_product_test"))
                .set(field("status"), status)
                .where(field("idx").in(idxList))
                .execute();

    }

    public void insertC24Product(C24CostcoProduct c24P) {
        context.insertInto(table("c24_product_test"))
                .columns(
                        field("costco_product_code"),
                        field("c24_code"),
                        field("thumb_main"),
                        field("thumb_extra"),
                        field("thumb_extra_filenames"),
                        field("description_detail"),
                        field("thumb_detail"),
                        field("spec_info_table"),
                        field("delivery_info"),
                        field("refund_info"),
                        field("status"))
                .values(
                        c24P.getProductCode(),
                        c24P.getC24Code(),
                        c24P.getThumbMain(),
                        c24P.getThumbExtra(),
                        c24P.getThumbExtraFilenames(),
                        c24P.getDescriptionDetail(),
                        c24P.getThumbDetail(),
                        c24P.getSpecInfoTable(),
                        c24P.getDeliveryInfo(),
                        c24P.getRefundInfo(),
                        c24P.getC24Status())
                .execute();
    }
}
