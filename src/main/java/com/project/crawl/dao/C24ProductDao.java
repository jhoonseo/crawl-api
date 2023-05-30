package com.project.crawl.dao;

import com.project.crawl.controller.dto.C24CostcoProduct;
import com.project.crawl.controller.dto.C24CostcoProductGroup;
import com.project.crawl.controller.dto.CostcoProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                .leftJoin(table("c24_product_test2").as("c24")).on(field("cp.product_code").eq(field("c24.costco_product_code")))
                .where(field("cp.status").eq(1))
                .orderBy(field("product_code").asc())
                .fetchInto(C24CostcoProduct.class);
    }

    public List<C24CostcoProduct> getAllC24CostcoProductList() {
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
                .leftJoin(table("c24_product_test2").as("c24")).on(field("cp.product_code").eq(field("c24.costco_product_code")))
                .orderBy(field("product_code").asc())
                .fetchInto(C24CostcoProduct.class);
    }


    public List<C24CostcoProduct> getC24CostcoProductListForExcel() {
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
                .leftJoin(table("c24_product_test2").as("c24")).on(field("cp.product_code").eq(field("c24.costco_product_code")))
                .where(field("cp.status").eq(1).and(field("c24.status").eq(1)))
                .orderBy(field("product_code").asc())
                .fetchInto(C24CostcoProduct.class);
    }

    public String getLastC24Code() {
        return context.select(field("c24_code"))
                .from(table("c24_product_test2"))
                .orderBy(field("c24_code").desc())
                .limit(1)
                .fetchOneInto(String.class);
    }
    public List<Integer> getDisablingIdxList() {
        return context.select(field("c24.idx"))
                .from(table("c24_product_test2").as("c24"))
                .leftJoin(table("costco_product").as("cp"))
                .on(field("c24.costco_product_code").eq("cp.product_code"))
                .where(
                        field("c24.status").eq(1)
                                .and(field("cp.status").eq(0)))
                .orderBy(field("product_code").asc())
                .fetchInto(Integer.class);
    }

    public Map<Integer, C24CostcoProduct> getAllC24Product() {
        Stream<C24CostcoProduct> a = context.select(
                    field("idx").as("c24_idx"),
                    field("thumb_detail"),
                    field("thumb_main"),
                    field("thumb_extra"),
                    field("thumb_extra_filenames")
                ).from(table("c24_product_test2"))
                .fetchStreamInto(C24CostcoProduct.class);
        return a.collect(Collectors.toMap(C24CostcoProduct::getC24Idx, c24 -> c24));
    }

    public List<C24CostcoProduct> getC24ProductListByC24CodeCollection(Collection collection) {
        return context.select(
                field("idx").as("c24_idx"),
                field("thumb_detail"),
                field("thumb_main"),
                field("thumb_extra")
        ).from(table("c24_product_test2"))
                .fetchInto(C24CostcoProduct.class);
    }

    public void updateThumbDetailByIdx(Integer idx, String thumbDetail) {
        context.update(table("c24_product_test2"))
                .set(field("thumb_detail"), thumbDetail)
                .where(field("idx").eq(idx))
                .execute();
    }

    public void updateThumbsInfoByIdx(Integer idx, String thumbMain, String thumbExtra, String thumbExtraFilename, String thumbDetail) {
        context.update(table("c24_product_test2"))
                .set(field("thumb_main"), thumbMain)
                .set(field("thumb_extra"), thumbExtra)
                .set(field("thumb_extra_filenames"), thumbExtraFilename)
                .set(field("thumb_detail"), thumbDetail)
                .where(field("idx").eq(idx))
                .execute();
    }

    public void updateThumbsInfoByProductCode(Integer productCode, String thumbMain, String thumbExtra, String thumbExtraFilename, String thumbDetail) {
        context.update(table("c24_product_test2"))
                .set(field("thumb_main"), thumbMain)
                .set(field("thumb_extra"), thumbExtra)
                .set(field("thumb_extra_filenames"), thumbExtraFilename)
                .set(field("thumb_detail"), thumbDetail)
                .where(field("costco_product_code").eq(productCode))
                .execute();
    }

    public void updateC24Group(C24CostcoProductGroup c24Group) {
        C24CostcoProduct c24P = c24Group.getCommonC24CostcoProduct();
        context.update(table("c24_product_test2"))
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

    public void updateStatusByProductCode(Integer productCode, Integer status) {
        context.update(table("c24_product_test2"))
                .set(field("status"), status)
                .where(field("costco_product_code").eq(productCode))
                .execute();
    }

    public void updateStatusByIdxList(List<Integer> idxList, Integer status) {
        context.update(table("c24_product_test2"))
                .set(field("status"), status)
                .where(field("idx").in(idxList))
                .execute();
    }

    public void insertC24Product(C24CostcoProduct c24P) {
        context.insertInto(table("c24_product_test2"))
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

    public void deleteC24ProductByC24CodeCollection(Collection collection) {
        context.deleteFrom(table("c24_product_test2"))
                .where(field("c24_code").in(collection))
                .execute();
    }
}
