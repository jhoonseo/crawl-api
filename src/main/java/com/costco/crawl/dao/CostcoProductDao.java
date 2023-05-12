package com.costco.crawl.dao;

import com.costco.crawl.controller.dto.CostcoProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@RequiredArgsConstructor
@Repository
@Slf4j
public class CostcoProductDao {
    private final DSLContext context;

    public @NotNull Map<Object, CostcoProduct> getAllCostcoProductMap() {
        return context
                .select(field("idx"), field("product_code"), field("costco_category_idx"),
                        field("name"), field("name_en"),
                        field("price"), field("sale_amount"), field("sale_period"),
                        field("is_sale"), field("is_option"), field("is_member_only"),
                        field("min_qty"), field("max_qty"),
                        field("status"), field("crawl_date_time"))
                .from(table("costco_product"))
                .orderBy(field("product_code").asc())
                .fetchMap(field("product_code"), CostcoProduct.class);
    }

    public List<CostcoProduct> getCostcoProductListAfterDate(LocalDate crawlDate) {
        return context
                .select(field("idx"), field("product_code"), field("costco_category_idx"),
                        field("name"), field("name_en"),
                        field("price"), field("sale_amount"), field("sale_period"),
                        field("is_sale"), field("is_option"), field("is_member_only"),
                        field("min_qty"), field("max_qty"),
                        field("status"), field("crawl_date_time"))
                .from(table("costco_product"))
                .where(
                        field("crawl_date_time")
                                .greaterOrEqual(Timestamp.valueOf(crawlDate.atStartOfDay()))
                ).and(field("is_option").eq(0))
                .fetchInto(CostcoProduct.class);
    }


    public CostcoProduct getCostcoProductByProductCode(Integer productCode) {
        return context
                .select(field("idx"), field("product_code"), field("costco_category_idx"),
                        field("name"), field("name_en"),
                        field("price"), field("sale_amount"), field("sale_period"),
                        field("is_sale"), field("is_option"), field("is_member_only"),
                        field("min_qty"), field("max_qty"),
                        field("status"), field("crawl_date_time"))
                .from(table("costco_product"))
                .where(
                        field("product_code").eq(productCode)
                ).fetchOneInto(CostcoProduct.class);
    }

    public Integer getCostcoProductIdxByProductCode(Integer productCode) {
        return context
                .select(field("idx"))
                .from(table("costco_product"))
                .where(
                        field("product_code").eq(productCode)
                ).fetchOneInto(Integer.class);
    }

    public void insertCostcoProduct(CostcoProduct costcoProduct) {
        context.insertInto(table("costco_product"))
                .columns(field("product_code"), field("costco_category_idx"),
                        field("name"), field("name_en"),
                        field("price"), field("sale_amount"), field("sale_period"),
                        field("is_sale"), field("is_option"), field("is_member_only"),
                        field("min_qty"), field("max_qty"),
                        field("status"), field("crawl_date_time"))
                .values(costcoProduct.getProductCode(), costcoProduct.getCostcoCategoryIdx(),
                        costcoProduct.getName(), costcoProduct.getNameEn(),
                        costcoProduct.getPrice(), costcoProduct.getSaleAmount(), costcoProduct.getSalePeriod(),
                        costcoProduct.getIsSale(), costcoProduct.getIsOption(), costcoProduct.getIsMemberOnly(),
                        costcoProduct.getMinQty(), costcoProduct.getMaxQty(),
                        costcoProduct.getStatus(), costcoProduct.getCrawlDateTime())
                .execute();
    }

    public void updateInfoByIdx(Integer idx, CostcoProduct costcoProduct) {
        context.update(table("costco_product"))
                .set(field("costco_category_idx"), costcoProduct.getCostcoCategoryIdx())
                .set(field("price"), costcoProduct.getPrice())
                .set(field("sale_amount"), costcoProduct.getSaleAmount())
                .set(field("sale_period"), costcoProduct.getSalePeriod())
                .set(field("is_sale"), costcoProduct.getIsSale())
                .set(field("is_option"), costcoProduct.getIsOption())
                .set(field("status"), costcoProduct.getStatus())
                .where(field("idx").eq(idx))
                .execute();
    }

    public void updateCostcoProductsStatus(Set<Object> costcoProductCodeSet, Integer status) {
        context.update(table("costco_product"))
                .set(field("status"), status)
                .where(field("product_code").in(costcoProductCodeSet))
                .execute();
    }

    public void updateCrawlDateTimeByIdx(Integer idx, String crawlDateTime) {
        context.update(table("costco_product"))
                .set(field("crawl_date_time"), crawlDateTime)
                .where(field("idx").eq(idx))
                .execute();
    }
}
