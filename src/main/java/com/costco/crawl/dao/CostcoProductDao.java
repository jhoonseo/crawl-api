package com.costco.crawl.dao;

import com.costco.crawl.controller.dto.CostcoProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;


@RequiredArgsConstructor
@Repository
@Slf4j
public class CostcoProductDao {
    private final DSLContext context;

    public Map<Integer, CostcoProduct> getAllCostcoProductMap() {
        Stream<CostcoProduct> costcoProductStream = context
                .select(field("idx"), field("product_code"), field("costco_category_idx"),
                        field("name"), field("name_en"),
                        field("price"), field("sale_amount"), field("sale_period"),
                        field("is_sale"), field("is_option"), field("is_member_only"),
                        field("min_qty"), field("max_qty"),
                        field("status"), field("updated_date_time"))
                .from(table("costco_product"))
                .orderBy(field("product_code").asc())
                .fetchStreamInto(CostcoProduct.class);

        return costcoProductStream
                .collect(Collectors.toMap(CostcoProduct::getProductCode, costcoProduct -> costcoProduct));
    }

    public List<Integer> getAllCostcoProductCodeList() {
        return context
                .select(field("product_code"))
                .from(table("costco_product"))
                .orderBy(field("product_code").asc())
                .fetchInto(Integer.class);
    }

    public List<CostcoProduct> getCostcoProductListAfterDate(LocalDate crawlDate) {
        return context
                .select(field("idx"), field("product_code"), field("costco_category_idx"),
                        field("name"), field("name_en"),
                        field("price"), field("sale_amount"), field("sale_period"),
                        field("is_sale"), field("is_option"), field("is_member_only"),
                        field("min_qty"), field("max_qty"),
                        field("status"), field("updated_date_time"))
                .from(table("costco_product"))
                .where(
                        field("updated_date_time")
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
                        field("status"), field("updated_date_time"))
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
                        field("status"), field("updated_date_time"))
                .values(costcoProduct.getProductCode(), costcoProduct.getCostcoCategoryIdx(),
                        costcoProduct.getName(), costcoProduct.getNameEn(),
                        costcoProduct.getPrice(), costcoProduct.getSaleAmount(), costcoProduct.getSalePeriod(),
                        costcoProduct.getIsSale(), costcoProduct.getIsOption(), costcoProduct.getIsMemberOnly(),
                        costcoProduct.getMinQty(), costcoProduct.getMaxQty(),
                        costcoProduct.getStatus(), costcoProduct.getUpdatedDateTime())
                .execute();
    }

    public void updateCostcoProductByIdx(Integer idx, CostcoProduct costcoProduct) {
        context.update(table("costco_product"))
                .set(field("costco_category_idx"), costcoProduct.getCostcoCategoryIdx())
                .set(field("name"), costcoProduct.getName())
                .set(field("name_en"), costcoProduct.getNameEn())
                .set(field("price"), costcoProduct.getPrice())
                .set(field("sale_amount"), costcoProduct.getSaleAmount())
                .set(field("sale_period"), costcoProduct.getSalePeriod())
                .set(field("is_sale"), costcoProduct.getIsSale())
                .set(field("is_option"), costcoProduct.getIsOption())
                .set(field("is_member_only"), costcoProduct.getIsMemberOnly())
                .set(field("min_qty"), costcoProduct.getMinQty())
                .set(field("max_qty"), costcoProduct.getMaxQty())
                .set(field("status"), costcoProduct.getStatus())
                .set(field("updated_date_time"), costcoProduct.getUpdatedDateTime())
                .where(field("idx").eq(idx))
                .execute();
    }

    public void updateCostcoProduct(CostcoProduct costcoProduct) {
        context.update(table("costco_product"))
                .set(field("costco_category_idx"), costcoProduct.getCostcoCategoryIdx())
                .set(field("name"), costcoProduct.getName())
                .set(field("name_en"), costcoProduct.getNameEn())
                .set(field("price"), costcoProduct.getPrice())
                .set(field("sale_amount"), costcoProduct.getSaleAmount())
                .set(field("sale_period"), costcoProduct.getSalePeriod())
                .set(field("is_sale"), costcoProduct.getIsSale())
                .set(field("is_option"), costcoProduct.getIsOption())
                .set(field("is_member_only"), costcoProduct.getIsMemberOnly())
                .set(field("min_qty"), costcoProduct.getMinQty())
                .set(field("max_qty"), costcoProduct.getMaxQty())
                .set(field("status"), costcoProduct.getStatus())
                .set(field("updated_date_time"), costcoProduct.getUpdatedDateTime())
                .where(field("product_code").eq(costcoProduct.getProductCode()))
                .execute();
    }

    public void updateCostcoProductListStatus(List<Integer> costcoProductCodeSet, Integer status) {
        context.update(table("costco_product"))
                .set(field("status"), status)
                .where(field("product_code").in(costcoProductCodeSet))
                .execute();
    }

    public void updateCostcoProductStatus(Object productCode, Integer status) {
        context.update(table("costco_product"))
                .set(field("status"), status)
                .where(field("product_code").eq(productCode))
                .execute();
    }

    public void updateCrawlDateTimeByIdx(Integer idx, String crawlDateTime) {
        context.update(table("costco_product"))
                .set(field("updated_date_time"), crawlDateTime)
                .where(field("idx").eq(idx))
                .execute();
    }
}
