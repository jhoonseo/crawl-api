package com.project.crawl.dao;

import com.project.crawl.controller.dto.CostcoProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.InsertValuesStep14;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;


@RequiredArgsConstructor
@Repository
@Slf4j
public class ProductDao {
    private final DSLContext context;

    public Map<Integer, CostcoProduct> getAllCostcoProductMap() {
        Stream<CostcoProduct> costcoProductStream = context
                .select(field("idx"), field("product_code"), field("costco_category_idx"),
                        field("name"), field("name_en"),
                        field("price"), field("sale_amount"), field("sale_period"),
                        field("is_sale"), field("is_option"), field("is_member_only"),
                        field("min_qty"), field("max_qty"),
                        field("status"), field("updated_date_time"))
                .from(table("product_costco"))
                .where(field("status").notEqual(-1))
                .orderBy(field("product_code").asc())
                .fetchStreamInto(CostcoProduct.class);

        return costcoProductStream
                .collect(Collectors.toMap(CostcoProduct::getProductCode, costcoProduct -> costcoProduct));
    }

    public List<Integer> getAllCostcoProductCodeList() {
        return context
                .select(field("product_code"))
                .from(table("product_costco"))
                .where(field("status").notEqual(-1))
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
                .from(table("product_costco"))
                .where(
                        field("updated_date_time")
                                .greaterOrEqual(Timestamp.valueOf(crawlDate.atStartOfDay()))
                ).and(field("is_option").eq(0)
                ).and(field("status").notEqual(-1))
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
                .from(table("product_costco"))
                .where(
                        field("product_code").eq(productCode)
                ).and(field("status").notEqual(-1))
                .fetchOneInto(CostcoProduct.class);
    }

    public Integer getCostcoProductIdxByProductCode(Integer productCode) {
        return context
                .select(field("idx"))
                .from(table("product_costco"))
                .where(
                        field("product_code").eq(productCode)
                ).and(field("status").notEqual(-1))
                .fetchOneInto(Integer.class);
    }

    public List<String> getDailyDetailItemMainImages1688(String startDate) {
        // detail_item_main_images_1688
        return context.select(field("image_url"))
                .from(table("detail_item_main_images_1688"))
                .where(
                        field("updated_datetime").greaterOrEqual(startDate)
                ).fetchInto(String.class);
    }

    public List<String> getDailyDetailItemMainImages1688() {
        // detail_item_main_images_1688
        return context.select(field("image_url"))
                .from(table("detail_item_main_images_1688"))
                .fetchInto(String.class);
    }

    public List<String> getDailyDescriptionItemImageUrlTexts1688() {
        return context.select(field("img_url_text"))
                .from(table("description_item_1688"))
                .fetchInto(String.class);
    }

    public void insertCostcoProduct(CostcoProduct costcoProduct) {
        context.insertInto(table("product_costco"))
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

    public void insertCostcoProductCollection(Collection<CostcoProduct> costcoProductCollection) {
        // Prepare the insert query
        InsertValuesStep14<Record, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object> insertQuery = context
                .insertInto(table("product_costco"))
                .columns(
                        field("product_code"),
                        field("costco_category_idx"),
                        field("name"),
                        field("name_en"),
                        field("price"),
                        field("sale_amount"),
                        field("sale_period"),
                        field("is_sale"),
                        field("is_option"),
                        field("is_member_only"),
                        field("min_qty"),
                        field("max_qty"),
                        field("status"),
                        field("updated_date_time")
                );

        // Create a list of propValues to be inserted
        List<InsertValuesStep14<Record,
            Object, Object, Object, Object, Object,
            Object, Object, Object, Object, Object,
            Object, Object, Object, Object>> valuesList = new ArrayList<>();

        // Add propValues for each CostcoProduct in the set
        costcoProductCollection.forEach(product -> {
            InsertValuesStep14<Record,
                Object, Object, Object, Object, Object,
                Object, Object, Object, Object, Object,
                Object, Object, Object, Object> values = (InsertValuesStep14<Record, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object>) insertQuery.values(
                    product.getProductCode(),
                    product.getCostcoCategoryIdx(),
                    product.getName(),
                    product.getNameEn(),
                    product.getPrice(),
                    product.getSaleAmount(),
                    product.getSalePeriod(),
                    product.getIsSale(),
                    product.getIsOption(),
                    product.getIsMemberOnly(),
                    product.getMinQty(),
                    product.getMaxQty(),
                    product.getStatus(),
                    product.getUpdatedDateTime()
            ).onDuplicateKeyIgnore();
            valuesList.add(values);
        });

        // Execute the insert query
        context.batch(valuesList).execute();
    }

    public void updateCostcoProductByIdx(Integer idx, CostcoProduct costcoProduct) {
        context.update(table("product_costco"))
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
        context.update(table("product_costco"))
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
                .and(field("status").notEqual(-1))
                .execute();
    }

    public void updateCostcoProductListStatus(List<Integer> costcoProductCodeSet, Integer status) {
        context.update(table("product_costco"))
                .set(field("status"), status)
                .where(field("product_code").in(costcoProductCodeSet))
                .and(field("status").notEqual(-1))
                .execute();
    }

    public void updateCostcoProductStatus(Object productCode, Integer status) {
        context.update(table("product_costco"))
                .set(field("status"), status)
                .where(field("product_code").eq(productCode))
                .and(field("status").notEqual(-1))
                .execute();
    }

    public void updateCrawlDateTimeByIdx(Integer idx, String crawlDateTime) {
        context.update(table("product_costco"))
                .set(field("updated_date_time"), crawlDateTime)
                .where(field("idx").eq(idx))
                .execute();
    }
}
