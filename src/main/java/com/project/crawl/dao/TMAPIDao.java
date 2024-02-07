package com.project.crawl.dao;

import com.project.crawl.service.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.*;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@RequiredArgsConstructor
@Repository
@Slf4j
public class TMAPIDao {
    private final DSLContext context;

    public Stream<Long> getKeywordItemIdStream() {
        return context.select(field("item_id"))
                .from(table("keyword_item_1688"))
                .fetchStreamInto(Long.class);
    }

    public Stream<Long> getDetailItemIdStream() {
        return context.select(field("item_id"))
                .from(table("detail_item_1688"))
                .fetchStreamInto(Long.class);
    }

    public void insertSearchKeywordHistory1688(String keyword, Integer page, String response) {
        context.insertInto(table("tmapi_1688_search_keyword_history"))
                .columns(field("keyword"), field("page"), field("response"))
                .values(keyword, page, response)
                .execute();
    }

    public void insertKeywordItem1688(KeywordItem item, String keyword) {
        context.insertInto(table("keyword_item_1688"))
                .columns(field("item_id"), field("keyword"), field("product_url"),
                        field("title"), field("img"), field("price"),
                        field("quantity_begin"), field("type"), field("unit"),
                        field("item_repurchase_rate"), field("goods_score"), field("image_dsm_score"),
                        field("primary_rank_score"), field("super_new_product"), field("byr_inquiry_uv"))
                .values(item.getItem_id(), keyword, item.getProduct_url(),
                        item.getTitle(), item.getImg(), item.getPrice(),
                        item.getQuantity_begin(), item.getType(), item.getUnit(),
                        item.getItem_repurchase_rate(), item.getGoods_score(), item.getImage_dsm_score(),
                        item.getPrimary_rank_score(), item.isSuper_new_product(), item.getByr_inquiry_uv())
                .execute();
    }

    public void insertKeywordItemCategoryPaths1688(Long itemId, String categoryPath) {
        context.insertInto(table("keyword_item_category_paths_1688"))
                .columns(field("item_id"), field("category_path"))
                .values(itemId, categoryPath)
                .execute();
    }

    public void insertKeywordItemDeliveryInfo1688(Long itemId, SearchDeliveryInfo deliveryInfo) {
        context.insertInto(table("keyword_item_delivery_info_1688"))
                .columns(field("item_id"), field("area_from"), field("weight"), field("suttle_weight"), field("free_postage"))
                .values(itemId, deliveryInfo.getArea_from(), deliveryInfo.getWeight(), deliveryInfo.getSuttle_weight(), deliveryInfo.isFree_postage())
                .execute();
    }

    public void insertKeywordItemPriceInfo1688(Long itemId, PriceInfo priceInfo) {
        context.insertInto(table("keyword_item_price_info_1688"))
                .columns(field("item_id"), field("drop_ship_price"), field("wholesale_price"), field("origin_price"))
                .values(itemId, priceInfo.getDrop_ship_price(), priceInfo.getWholesale_price(), priceInfo.getOrigin_price())
                .execute();
    }

    public void insertKeywordItemQuantityPrice1688(Long itemId, QuantityPrice quantityPrice) {
        context.insertInto(table("keyword_item_quantity_prices_1688"))
                .columns(field("item_id"), field("begin_num"), field("end_num"), field("price"))
                .values(itemId, quantityPrice.getBegin_num(), quantityPrice.getEnd_num(), quantityPrice.getPrice())
                .execute();
    }

    public void insertKeywordItemSaleInfo1688(Long itemId, SaleInfo saleInfo) {
        context.insertInto(table("keyword_item_sale_info_1688"))
                .columns(field("item_id"), field("gmv_30days"), field("gmv_30days_cb"),
                        field("sale_quantity"), field("orders_count"))
                .values(itemId, saleInfo.getGmv_30days(), saleInfo.getGmv_30days_cb(),
                        saleInfo.getSale_quantity(), saleInfo.getOrders_count())
                .execute();
    }

    public void insertKeywordItemScoreInfo1688(String memberId, KeywordShopScoreInfo scoreInfo) {
        context.insertInto(table("keyword_item_score_info_1688"))
                .columns(field("shop_member_id"), field("composite_new_score"), field("composite_score"),
                        field("consultation_score"), field("dispute_score"), field("logistics_score"),
                        field("return_score"))
                .values(memberId, scoreInfo.getComposite_new_score(), scoreInfo.getComposite_score(),
                        scoreInfo.getConsultation_score(), scoreInfo.getDispute_score(), scoreInfo.getLogistics_score(),
                        scoreInfo.getReturn_score())
                .execute();
    }

    public void insertKeywordItemShopInfo1688(Long itemId, KeywordShopInfo shopInfo) {
        // todo 구조 개선이 시급한 부분 shop_info 부분은 member_id 기준으로 unique 여야한다.
        context.insertInto(table("keyword_item_shop_info_1688"))
                .columns(field("item_id"), field("login_id"), field("member_id"),
                        field("biz_type"), field("company_name"), field("tp_member"),
                        field("tp_year"), field("factory_inspection"), field("shop_repurchase_rate"))
                .values(itemId, shopInfo.getLogin_id(), shopInfo.getMember_id(),
                        shopInfo.getBiz_type(), shopInfo.getCompany_name(), shopInfo.isTp_member(),
                        shopInfo.getTp_year(), shopInfo.isFactory_inspection(), shopInfo.getShop_repurchase_rate())
                .execute();
    }

    // ------------------------------------------------
    // todo detail & keyword 테이블 간에 합칠 수 있는 테이블은 합치는게 바람직하다.
    // todo detail 관련 테이블의 delivery_info 테이블은 반정규화를 통해서 메인 테이블에 합치는게 바람직하다.

    public void insertSearchDetailHistory1688(Long itemId, String jsonResponse) {
        context.insertInto(table("tmapi_1688_search_keyword_history"))
                .columns(field("item_id"), field("response"))
                .values(itemId, jsonResponse)
                .execute();
    }

    public void insertDetailItemData1688(DetailData detail) {
        context.insertInto(table("detail_item_1688"))
                .columns(field("item_id"), field("title"), field("category_id"),
                        field("root_category_id"), field("currency"), field("offer_unit"),
                        field("video_url"), field("detail_url"), field("sale_count"),
                        field("unit_weight"))
                .values(detail.getItem_id(), detail.getTitle(), detail.getCategory_id(),
                        detail.getRoot_category_id(), detail.getCurrency(), detail.getOffer_unit(),
                        detail.getVideo_url(), detail.getDetail_url(), detail.getSale_count(),
                        detail.getDelivery_info().getUnit_weight())
                .execute();
    }

    public void insertDetailItemMainImage1688(
            Long itemId,
            String mainImage
    ) {
        context.insertInto(table("detail_item_main_images_1688"))
                .columns(field("item_id"), field("image_url"))
                .values(itemId, mainImage)
                .execute();
    }

    public void insertDetailItemProductProp1688(
            Long itemId,
            String propertyName,
            String propertyValue
    ) {
        context.insertInto(table("detail_item_product_props_1688"))
                .columns(field("item_id"), field("property_name"), field("property_value"))
                .values(itemId, propertyName, propertyValue)
                .execute();
    }

    public void insertDetailItemPropValue1688(
            Integer propId,
            PropValue propValue
    ) {
        context.insertInto(table("detail_item_prop_values_1688"))
                .columns(field("sku_prop_id"), field("name"), field("vid"), field("image_url"))
                .values(propId, propValue.getName(), propValue.getVid(), propValue.getImageUrl())
                .execute();
    }

    public void insertDetailItemShopInfo1688(
            Long itemId,
            DetailShopInfo shopInfo
    ) {
        context.insertInto(table("detail_item_shop_info_1688"))
                .columns(field("item_id"), field("shop_name"), field("shop_url"),
                        field("seller_login_id"), field("seller_user_id"), field("seller_member_id"))
                .values(itemId, shopInfo.getShop_name(), shopInfo.getShop_url(),
                        shopInfo.getSeller_login_id(), shopInfo.getSeller_user_id(), shopInfo.getSeller_member_id())
                .execute();
    }

    public Integer insertDetailItemSkuPriceRange1688(
            Long itemId,
            SkuPriceRange skuPriceRange
    ) {
        return (Integer) context.insertInto(table("detail_item_sku_price_range_1688"))
                .columns(field("item_id"), field("begin_num"), field("stock"),
                        field("sell_unit"))
                .values(itemId, skuPriceRange.getBegin_num(), skuPriceRange.getStock(),
                        skuPriceRange.getSell_unit())
                .returning().fetchOne()
                .get(field("idx"));

        //context.insertInto(mt).set(record).returning().fetchOne().getIdx()
    }

    public void insertDetailItemSkuParam1688(
            Integer skuPriceRangeIdx,
            SkuParam skuParam
    ) {
        context.insertInto(table("detail_item_sku_params_1688"))
                .columns(field("price_range_id"), field("beginAmount"), field("price"))
                .values(skuPriceRangeIdx, skuParam.getBeginAmount(), skuParam.getPrice())
                .execute();
    }

    public Integer insertDetailItemSkuProp1688(
            Long itemId,
            SkuProp skuProp
    ) {
        return (Integer) context.insertInto(table("detail_item_sku_props_1688"))
                .columns(field("item_id"), field("prop_name"), field("pid"))
                .values(itemId, skuProp.getProp_name(), skuProp.getPid())
                .returning()
                .fetchOne()
                .get(field("idx"));
    }

    public void insertDetailItemSku1688(
            Long itemId,
            Sku sku
    ) {
        context.insertInto(table("detail_item_skus_1688"))
                .columns(field("skuid"), field("item_id"), field("specid"),
                        field("sale_price"), field("origin_price"), field("stock"),
                        field("sale_count"), field("props_ids"), field("props_names"))
                .values(sku.getSkuid(), itemId, sku.getSpecid(),
                        sku.getSale_price(), sku.getOrigin_price(), sku.getStock(),
                        sku.getSale_count(), sku.getProps_ids(), sku.getProps_names())
                .execute();
    }

    public Integer insertTest() {
        /*
        Integer a = (Integer) context.insertInto(table("insert_test"))
                .columns(field("content"))
                .values("test")
                .returning(table("insert_test").field("idx"))
                .fetchOne()
                .get(field("idx"));

        System.out.println(a);


         */
        /*
        '        return context.insertInto(cg, cg.ORDER_IDX, cg.GROUP_PRICE, cg.GROUP_DELIVERY_PRICE)
                .values(orderIdx, groupPrice, groupDeliveryPrice)
                .returning(co.IDX)
                .fetchOne()
                .getIdx();
        Record b = context.insertInto(table("insert_test"))
                .columns(field("content"))
                .values("test")
                .returning(field("idx"))
                .fetchOne();
        System.out.println(b);


        return b.size();
         */

        // Define the insert statement without using returningResult()
        InsertValuesStep1<Record, Object> insertStatement = context
                .insertInto(table("insert_test"))
                .columns(field("content"))
                .values("test");

// Execute the insert statement and fetch the generated keys
        Record result = insertStatement.returning(field("idx")).fetchOne();

// Check if the result is not null and print the generated key
        if (result != null) {
            Integer generatedKey = result.get(field("idx", Integer.class));
            System.out.println("Generated key: " + generatedKey);
            return generatedKey; // Or handle as needed
        } else {
            // Handle the case where no key is generated or result is null
            System.out.println("No key was generated or result is null.");
            return 0; // Or appropriate handling
        }

    }
}
