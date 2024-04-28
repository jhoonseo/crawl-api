package com.project.crawl.dao;

import com.project.crawl.service.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@RequiredArgsConstructor
@Repository
@Slf4j
public class TMAPIDao {
    private final DSLContext context;
    private final JdbcTemplate jdbcTemplate;

    public Stream<Long> getKeywordItemIdStream1688() {
        return context.select(field("item_id"))
                .from(table("keyword_item_1688"))
                .fetchStreamInto(Long.class);
    }

    public DetailData getDetailItemByItemId(Long itemId) {
        return context.selectFrom(table("detail_item_1688"))
                .where(field("item_id").eq(itemId))
                .fetchOneInto(DetailData.class);
    }


    public DescriptionData getDescriptionItemByByItemId(Long itemId) {
        return context.selectFrom(table("description_item_1688"))
                .where(field("item_id").eq(itemId))
                .fetchOneInto(DescriptionData.class);
    }

    public Stream<Long> getDetailItemIdStream1688() {
        return context.select(field("item_id"))
                .from(table("detail_item_1688"))
                .fetchStreamInto(Long.class);
    }

    public Stream<Long> getDescriptionItemIdStream1688() {
        return context.select(field("item_id"))
                .from(table("description_item_1688"))
                .fetchStreamInto(Long.class);    }

    public List<String> getKeywordItemCategoryPathList1688() {
        return context.select(field("category_path"))
                .from(table("keyword_item_category_paths_1688"))
                .fetchInto(String.class);
    }

    public Stream<KeywordShopInfo> getKeywordItemShopInfoStream1688() {
        return context.select(field("idx"), field("member_id"))
                .from(table("keyword_item_shop_info_1688"))
                .fetchStreamInto(KeywordShopInfo.class);
    }

    public Stream<CategoryPath> getKeywordItemCategoryPathStream1688() {
        return context.select(field("idx"), field("category_path"))
                .from(table("keyword_item_category_paths_1688"))
                .fetchStreamInto(CategoryPath.class);
    }

    public void insertSearchKeywordHistory1688(String keyword, Integer page, String response) {
        context.insertInto(table("tmapi_1688_search_keyword_history"))
                .columns(field("keyword"), field("page"), field("response"))
                .values(keyword, page, response)
                .execute();
    }

    public void insertSearchDetailHistory1688(Long itemId, String jsonResponse) {
        final int execute = context.insertInto(table("tmapi_1688_search_detail_history"))
                .columns(field("item_id"), field("response"))
                .values(itemId, jsonResponse)
                .execute();
    }

    public void insertSearchDescriptionHistory1688(Long itemId, String jsonResponse) {
        final int execute = context.insertInto(table("tmapi_1688_search_description_history"))
                .columns(field("item_id"), field("response"))
                .values(itemId, jsonResponse)
                .execute();
    }


    public void insertKeywordItem1688(KeywordItem item, String keyword) {
        context.insertInto(table("keyword_item_1688"))
                .columns(field("item_id"), field("keyword"), field("product_url"),
                        field("title"), field("img"), field("price"),
                        field("quantity_begin"), field("type"), field("unit"),
                        field("item_repurchase_rate"), field("goods_score"), field("image_dsm_score"),
                        field("primary_rank_score"), field("super_new_product"), field("byr_inquiry_uv"),
                        field("shop_idx"), field("category_path_idxs"), field("sale_info_idx"))
                .values(item.getItem_id(), keyword, item.getProduct_url(),
                        item.getTitle(), item.getImg(), item.getPrice(),
                        item.getQuantity_begin(), item.getType(), item.getUnit(),
                        item.getItem_repurchase_rate(), item.getGoods_score(), item.getImage_dsm_score(),
                        item.getPrimary_rank_score(), item.isSuper_new_product(), item.getByr_inquiry_uv(),
                        item.getShop_idx(), item.getCategory_path_idxs(), item.getSale_info_idx())
                .execute();
    }

    public void insertKeywordItemCategoryPaths1688(String categoryPath) {
        context.insertInto(table("keyword_item_category_paths_1688"))
                .columns(field("category_path"))
                .values(categoryPath)
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

    public Integer insertKeywordItemSaleInfo1688(SaleInfo saleInfo) {
        // 삽입할 데이터 설정
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("gmv_30days", saleInfo.getGmv_30days());
        parameters.put("gmv_30days_cb", saleInfo.getGmv_30days_cb());
        parameters.put("sale_quantity", saleInfo.getSale_quantity());
        parameters.put("orders_count", saleInfo.getOrders_count());
        parameters.put("status", 1); // status 컬럼에 대한 Default 값을 명시적으로 설정
        parameters.put("created_datetime", LocalDateTime.now());
        parameters.put("updated_datetime", LocalDateTime.now());


        // SimpleJdbcInsert를 사용하여 insert 수행
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("keyword_item_sale_info_1688")
                .usingGeneratedKeyColumns("idx");

        Number idx = simpleJdbcInsert.executeAndReturnKey(parameters);

        return idx.intValue();

    }

    public void insertKeywordItemScoreInfo1688(Integer shopIdx, KeywordShopScoreInfo scoreInfo) {
        context.insertInto(table("keyword_item_score_info_1688"))
                .columns(field("shop_idx"), field("composite_new_score"), field("composite_score"),
                        field("consultation_score"), field("dispute_score"), field("logistics_score"),
                        field("return_score"))
                .values(shopIdx, scoreInfo.getComposite_new_score(), scoreInfo.getComposite_score(),
                        scoreInfo.getConsultation_score(), scoreInfo.getDispute_score(), scoreInfo.getLogistics_score(),
                        scoreInfo.getReturn_score())
                .execute();
    }

    public Integer insertKeywordItemShopInfo1688(Long itemId, KeywordShopInfo shopInfo) {
        // todo 구조 개선이 시급한 부분 shop_info 부분은 member_id 기준으로 unique 여야한다. return inserted row's idx

        // 삽입할 데이터 설정
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("item_id", itemId);
        parameters.put("login_id", shopInfo.getLogin_id());
        parameters.put("member_id", shopInfo.getMember_id());
        parameters.put("biz_type", shopInfo.getBiz_type());
        parameters.put("company_name", shopInfo.getCompany_name());
        parameters.put("tp_member", shopInfo.isTp_member());
        parameters.put("tp_year", shopInfo.getTp_year());
        parameters.put("factory_inspection", shopInfo.isFactory_inspection());
        parameters.put("shop_repurchase_rate", shopInfo.getShop_repurchase_rate());
        parameters.put("status", 1);
        parameters.put("created_datetime", LocalDateTime.now());
        parameters.put("updated_datetime", LocalDateTime.now());

        // SimpleJdbcInsert를 사용하여 insert 수행
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("keyword_item_shop_info_1688")
                .usingGeneratedKeyColumns("idx");

        Number idx = simpleJdbcInsert.executeAndReturnKey(parameters);

        return idx.intValue();
    }

    // ------------------------------------------------
    // todo detail & keyword 테이블 간에 합칠 수 있는 테이블은 합치는게 바람직하다.
    // todo detail 관련 테이블의 delivery_info 테이블은 반정규화를 통해서 메인 테이블에 합치는게 바람직하다.

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
        /*
        context.insertInto(table("detail_item_sku_price_range_1688"))
                .columns(field("item_id"), field("begin_num"), field("stock"),
                        field("sell_unit"))
                .values(itemId, skuPriceRange.getBegin_num(), skuPriceRange.getStock(),
                        skuPriceRange.getSell_unit())
                .returning().fetchOne()
                .get(field("idx"));
         */

        // 삽입할 데이터 설정
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("item_id", itemId);
        parameters.put("begin_num", skuPriceRange.getBegin_num());
        parameters.put("stock", skuPriceRange.getStock());
        parameters.put("sell_unit", skuPriceRange.getSell_unit());
        parameters.put("status", 1);
        parameters.put("created_datetime", LocalDateTime.now());
        parameters.put("updated_datetime", LocalDateTime.now());

        // SimpleJdbcInsert를 사용하여 insert 수행
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("detail_item_sku_price_range_1688")
                .usingGeneratedKeyColumns("idx");

        Number idx = simpleJdbcInsert.executeAndReturnKey(parameters);

        return idx.intValue();

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
        // 삽입할 데이터 설정
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("item_id", itemId);
        parameters.put("prop_name", skuProp.getProp_name());
        parameters.put("pid", skuProp.getPid());
        parameters.put("status", 1); // status 컬럼에 대한 Default 값을 명시적으로 설정
        parameters.put("created_datetime", LocalDateTime.now());
        parameters.put("updated_datetime", LocalDateTime.now());


        // SimpleJdbcInsert를 사용하여 insert 수행
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("detail_item_sku_props_1688")
                .usingGeneratedKeyColumns("idx");

        Number idx = simpleJdbcInsert.executeAndReturnKey(parameters);

        return idx.intValue();
        /*
        return (Integer) context.insertInto(table("detail_item_sku_props_1688"))
                .columns(field("item_id"), field("prop_name"), field("pid"))
                .values(itemId, skuProp.getProp_name(), skuProp.getPid())
                .returning()
                .fetchOne()
                .get(field("idx"));
         */
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


    public void insertDescriptionItemData1688(DescriptionData description) {
        context.insertInto(table("description_item_1688"))
                .columns(field("item_id"), field("img_url_text"), field("img_filename_text"),
                        field("img_description_detail"))
                .values(description.getItem_id(), description.getImg_url_text(), description.getImg_filename_text(),
                        description.getImg_description_detail())
                .execute();
    }


    public Integer insertTest() {
        String content = "content";

        // 삽입할 데이터 설정
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("content", content);

        // SimpleJdbcInsert를 사용하여 insert 수행
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("insert_test")
                .usingGeneratedKeyColumns("idx");

        Number idx = simpleJdbcInsert.executeAndReturnKey(parameters);

        // 반환된 idx 값이 null이 아닌지 확인하여 Long 형으로 변환하여 반환
        return idx.intValue();


    }
}
