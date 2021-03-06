package cn.m2c.scm.port.adapter.service.goods;

import cn.m2c.common.JsonUtils;
import cn.m2c.common.RedisUtil;
import cn.m2c.ddd.common.port.adapter.persistence.springJdbc.SupportJdbcTemplate;
import cn.m2c.scm.application.utils.Utils;
import cn.m2c.scm.domain.service.goods.GoodsService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.disconf.client.usertools.DisconfDataGetter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 商品
 */
@Service("goodsRestService")
public class GoodsRestServiceImpl implements GoodsService {
    private static final String M2C_HOST_URL = DisconfDataGetter.getByFileItem("constants.properties", "m2c.host.url").toString().trim();

    private static final Logger LOGGER = LoggerFactory.getLogger(GoodsRestServiceImpl.class);

    @Autowired
    private SupportJdbcTemplate supportJdbcTemplate;

    public SupportJdbcTemplate getSupportJdbcTemplate() {
        return this.supportJdbcTemplate;
    }

    @Autowired
    RestTemplate restTemplate;

    @Override
    public List<Map> getGoodsTags(String dealerId, String goodsId, String classifyId) {
        String key = "scm.goods.market.tags";
        Map cacheMap = new HashMap<>();
        String tags = RedisUtil.getString(key); //从缓存中取数据
        if (StringUtils.isNotEmpty(tags)) { // 缓存不为空
            cacheMap = JsonUtils.toMap(tags);
            if (null != cacheMap && null != cacheMap.get(goodsId)) {
                List<Map> cacheList = (List<Map>) cacheMap.get(goodsId);
                return cacheList;
            }
        }
        List<Map> resultList = new ArrayList<>();
        String url = M2C_HOST_URL + "/m2c.market/market/type/list?dealer_id={0}&goods_id={1}&classify_id={2}";
        try {
            String result = restTemplate.getForObject(url, String.class, dealerId, goodsId, classifyId);
            JSONObject json = JSONObject.parseObject(result);
            if (json.getInteger("status") == 200) {
                JSONObject contentObject = json.getJSONObject("content");
                Iterator<Object> it = contentObject.getJSONArray("typeList").iterator();
                while (it.hasNext()) {
                    Map<String, String> jo = (Map<String, String>) it.next();
                    String labelName = jo.get("typeName");
                    String typeColor = jo.get("typeColor");
                    String typeColorTmd = jo.get("typeColorTmd");
                    String backgroundColor = jo.get("backgroundColor");
                    String backgroundColorTmd = jo.get("backgroundColorTmd");
                    Map mapTags = new HashMap<>();
                    mapTags.put("backcolor", backgroundColor);
                    mapTags.put("backcolorTmd", backgroundColorTmd);
                    mapTags.put("name", labelName);
                    mapTags.put("wordcolor", typeColor);
                    mapTags.put("wordcolorTmd", typeColorTmd);
                    resultList.add(mapTags);
                }
                if (null == cacheMap) {
                    cacheMap = new HashMap<>();
                }
                cacheMap.put(goodsId, resultList);
                RedisUtil.setString(key, 24 * 3600, JsonUtils.toStr(cacheMap));
            }
        } catch (Exception e) {
            LOGGER.error("查询商品营销活动标签失败");
            LOGGER.error("getGoodsTags failed.url=>" + url);
            LOGGER.error("getGoodsTags failed.param=>dealerId=" + dealerId + ",goodsId=" + goodsId + ",classifyId=" + classifyId);
        }
        return resultList;
    }

    @Override
    public List<Map> getGoodsFullCut(String userId, String dealerId, String goodsId, String classifyId) {
        String url = M2C_HOST_URL + "/m2c.market/domain/fullcut/list?dealer_id={0}&goods_id={1}&classify_id={2}&user_id={3}";
        try {
            String result = restTemplate.getForObject(url, String.class, dealerId, goodsId, classifyId, userId);
            JSONObject json = JSONObject.parseObject(result);
            if (json.getInteger("status") == 200) {
                JSONArray contents = json.getJSONArray("content");
                if (null != contents) {
                    List<Map> resultList = new ArrayList<>();
                    Iterator<Object> contentJsons = contents.iterator();
                    while (contentJsons.hasNext()) {
                        Map resultMap = new HashMap<>();
                        List<Map> contentList = new ArrayList<>();
                        Object contentJson = contentJsons.next();
                        JSONObject contentObject = JSONObject.parseObject(JSONObject.toJSONString(contentJson));
                        Integer numPerOne = contentObject.getInteger("numPerOne");
                        Integer numPerDay = contentObject.getInteger("numPerDay");
                        resultMap.put("numPerOne", numPerOne);
                        resultMap.put("numPerDay", numPerDay);
                        resultMap.put("numLimit", new StringBuffer().append("每人优惠").append(numPerOne).append("次，每天仅可优惠").append(numPerDay).append("次").toString());

                        Iterator<Object> it = contentObject.getJSONArray("itemList").iterator();
                        while (it.hasNext()) {
                            Map jo = (Map) it.next();
                            String itemName = (String) jo.get("content");
                            Map map = new HashMap<>();
                            map.put("itemName", itemName);
                            contentList.add(map);
                        }
                        resultMap.put("itemNames", contentList);
                        Integer rangeType = contentObject.getInteger("rangeType");
                        resultMap.put("rangeType", rangeType);

                        resultMap.put("fullCutName", contentObject.getString("fullCutName"));
                        resultMap.put("fullCutType", contentObject.getInteger("fullCutType")); //满减形式，1：减钱，2：打折，3：换购

                        Iterator<Object> rangeIt = contentObject.getJSONArray("suitableRangeList").iterator();
                        List<String> idList = new ArrayList<>();
                        while (rangeIt.hasNext()) {
                            Map jo = (Map) rangeIt.next();
                            String id = (String) jo.get("id");
                            idList.add(id);
                        }
                        resultMap.put("ids", Utils.listToString(idList, ','));
                        resultList.add(resultMap);
                    }
                    return resultList;
                }
            }
        } catch (Exception e) {
            LOGGER.error("查询商品满减信息失败");
            LOGGER.error("getGoodsFullCut failed.url=>" + url);
            LOGGER.error("getGoodsFullCut failed.param=>dealerId=" + dealerId + ",goodsId=" + goodsId + ",classifyId=" + classifyId + ",userId=" + userId);
        }
        return null;
    }

    @Override
    public Map getMediaResourceInfo(String barNo) {
        return null;
    }

    @Override
    public List<String> getGoodsIdByCoordinate(Double longitude, Double latitude) {
        return null;
    }

    @Override
    public String getUserIsFavoriteGoods(String userId, String goodsId, String token) {
        String url = M2C_HOST_URL + "/m2c.users/favorite/app/detail?token={0}&userId={1}&goodsId={2}";
        try {
            String result = restTemplate.getForObject(url, String.class, token, userId, goodsId);
            JSONObject json = JSONObject.parseObject(result);
            if (json.getInteger("status") == 200) {
                JSONObject contents = json.getJSONObject("content");
                if (null != contents) {
                    String favoriteId = contents.getString("favoriteId");
                    return favoriteId;
                }
            }
        } catch (Exception e) {
            LOGGER.error("查询用户是否收藏商品失败");
            LOGGER.error("getUserIsFavoriteGoods failed.url=>" + url);
            LOGGER.error("getUserIsFavoriteGoods failed.param=>goodsId=" + goodsId + ",userId=" + userId);
        }
        return null;
    }

    @Override
    public boolean updateRecognizedImgStatus(String recognizedId, String recognizedUrl, Integer status) {
        return false;
    }

    @Override
    public Map getMediaInfo(String mediaResourceId) {
        String url = M2C_HOST_URL + "/m2c.media/mres/detail/" + mediaResourceId + "/client";
        try {
            String result = restTemplate.getForObject(url, String.class);
            JSONObject json = JSONObject.parseObject(result);
            if (json.getInteger("status") == 200) {
                JSONObject contentObject = json.getJSONObject("content");
                if (null != contentObject) {
                    String mediaId = contentObject.getString("mediaId");
                    String mediaName = contentObject.getString("mediaName");
                    String mresName = contentObject.getString("mresName");
                    Map<String, Object> mediaInfo = new HashMap<>();
                    mediaInfo.put("mediaId", mediaId);
                    mediaInfo.put("mediaName", mediaName);
                    mediaInfo.put("mresName", mresName);
                    return mediaInfo;
                }
            }
        } catch (Exception e) {
            LOGGER.error("查询媒体信息失败");
            LOGGER.error("getMediaInfo failed.url=>" + url);
            LOGGER.error("getMediaInfo failed.param=>mediaResourceId=" + mediaResourceId);
        }
        return null;
    }

    @Override
    public Map getUserInfoByUserId(String userId) {
        String url = M2C_HOST_URL + "/m2c.users/user/detail?token={0}&userId={1}";
        try {
            String result = restTemplate.getForObject(url, String.class, "", userId);
            JSONObject json = JSONObject.parseObject(result);
            if (json.getInteger("status") == 200) {
                JSONObject contentObject = json.getJSONObject("content");
                if (null != contentObject) {
                    String areaProvince = contentObject.getString("areaProvince");
                    String areaDistrict = contentObject.getString("areaDistrict");
                    String provinceCode = contentObject.getString("provinceCode");
                    String districtCode = contentObject.getString("districtCode");
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("areaProvince", areaProvince);
                    userInfo.put("areaDistrict", areaDistrict);
                    userInfo.put("provinceCode", provinceCode);
                    userInfo.put("districtCode", districtCode);
                    return userInfo;
                }
            }
        } catch (Exception e) {
            LOGGER.error("查询用户信息失败");
            LOGGER.error("getUserInfoByUserId failed.url=>" + url);
            LOGGER.error("getUserInfoByUserId failed.param=>userId=" + userId);
        }
        return null;
    }

    @Override
    public Integer getCartGoodsTotal(String userId) {
        String url = M2C_HOST_URL + "/m2c.users/cart/getTotalQuantity?userId=" + userId;
        try {
            String result = restTemplate.getForObject(url, String.class);
            JSONObject json = JSONObject.parseObject(result);
            if (json.getInteger("status") == 200) {
                return json.getInteger("content");
            }
        } catch (Exception e) {
            LOGGER.error("查询用户购物车中商品数量失败");
            LOGGER.error("getCartGoodsTotal failed.url=>" + url);
        }
        return 0;
    }

    @Override
    public List<Map> getGoodsCoupon(String userId, String dealerId, String goodsId, String classifyId) {
        String url = M2C_HOST_URL + "/m2c.market/domain/coupon/suitable/goods/list?dealer_id={0}&goods_id={1}&category_id={2}&user_id={3}";
        try {
            String result = restTemplate.getForObject(url, String.class, dealerId, goodsId, classifyId, userId);
            JSONObject json = JSONObject.parseObject(result);
            if (json.getInteger("status") == 200) {
                JSONArray contents = json.getJSONArray("content");
                if (null != contents && contents.size() > 0) {
                    List<Map> resultList = new ArrayList<>();
                    Iterator<Object> contentIt = contents.iterator();
                    while (contentIt.hasNext()) {
                        Object contentJson = contentIt.next();
                        JSONObject contentObject = JSONObject.parseObject(JSONObject.toJSONString(contentJson));
                        // 优惠券ID
                        String couponId = contentObject.getString("couponId");
                        // 优惠券优惠形式：1 减钱 2 打折
                        Integer couponForm = contentObject.getInteger("couponForm");
                        // 优惠券类型：1：代金券，2：打折券，3：分享券
                        Integer couponType = contentObject.getInteger("couponType");
                        // 门槛文案
                        String thresholdContent = contentObject.getString("thresholdContent");
                        // 面值
                        String value = contentObject.getString("value");
                        // 优惠券名称
                        String couponName = contentObject.getString("couponName");
                        // 作用范围页面内容
                        String rangeContent = contentObject.getString("rangeContent");
                        // 有效期 ,返回格式:XXXX.XX.XX - XXXX.XX.XX
                        String expirationTime = contentObject.getString("expirationTime");
                        // 有效期开始时间
                        String expirationTimeStart = contentObject.getString("expirationTimeStart");
                        // 有效期截止时间
                        String expirationTimeEnd = contentObject.getString("expirationTimeEnd");
                        // 是否可领取 true：可以，false：不可以
                        Boolean ableToReceive = contentObject.getBoolean("ableToReceive");
                        // 优惠券总数
                        Integer totalCount = contentObject.getInteger("totalCount");
                        // 标签文案
                        String labelContent = contentObject.getString("labelContent");
                        // 生成者类型，1.平台，2.商家
                        Integer creatorType = contentObject.getInteger("creatorType");

                        Map tempMap = new HashMap<>();
                        tempMap.put("couponId", couponId);
                        tempMap.put("couponForm", couponForm);
                        tempMap.put("couponType", couponType);
                        tempMap.put("content", thresholdContent);
                        if (couponForm == 1) { // 减钱
                            tempMap.put("faceValue", Long.parseLong(value) / 10000);
                        } else { // 打折
                            tempMap.put("faceValue", value);
                        }
                        tempMap.put("couponName", couponName);
                        tempMap.put("rangeContent", rangeContent);
                        tempMap.put("expirationTime", expirationTime);
                        tempMap.put("expirationTimeStart", expirationTimeStart);
                        tempMap.put("expirationTimeEnd", expirationTimeEnd);
                        tempMap.put("ableToReceive", ableToReceive);
                        tempMap.put("totalCount", totalCount);
                        tempMap.put("labelContent", labelContent);
                        tempMap.put("creatorType", creatorType);
                        resultList.add(tempMap);
                    }
                    return resultList;
                }
            } else {
                LOGGER.error("查询商品优惠券信息失败");
                LOGGER.error("getGoodsCoupon failed.url=>" + url);
                LOGGER.error("getGoodsCoupon failed.error=>" + json.getString("errorMessage"));
                LOGGER.error("getGoodsCoupon failed.param=>dealerId=" + dealerId + ",goodsId=" + goodsId + ",category_id=" + classifyId + ",userId=" + userId);
            }
        } catch (Exception e) {
            LOGGER.error("查询商品优惠券信息异常");
            LOGGER.error("getGoodsCoupon exception.url=>" + url);
            LOGGER.error("getGoodsCoupon exception.error=>" + e.getMessage());
            LOGGER.error("getGoodsCoupon exception.param=>dealerId=" + dealerId + ",goodsId=" + goodsId + ",category_id=" + classifyId + ",userId=" + userId);
        }
        return null;
    }

    @Override
    public Map getCouponRange(String couponId) {
        String url = M2C_HOST_URL + "/m2c.market/domain/coupon/suitable/range/goods?coupon_id={0}";
        try {
            String result = restTemplate.getForObject(url, String.class, couponId);
            JSONObject json = JSONObject.parseObject(result);
            if (json.getInteger("status") == 200) {
                JSONObject content = json.getJSONObject("content");
                if (null != content) {
                    /**
                     * 优惠券作用范围，0：全场，1：商家，2：商品，3：品类
                     * 备注：
                     * 当作用范围为全场时，dealerId、goodsId、categoryId为排除的对象；当作用范围不是全场时，三个id为优惠券实际作用的对象
                     */
                    Integer rangeType = content.getInteger("rangeType");
                    List dealerIdList = null;
                    List goodsIdsList = null;
                    List categoryList = null;
                    JSONArray dealerIds = content.getJSONArray("dealerList");
                    if (null != dealerIds && dealerIds.size() > 0) {
                        dealerIdList = dealerIds.toJavaList(String.class);
                    }
                    JSONArray goodsIds = content.getJSONArray("goodsList");
                    if (null != goodsIds && goodsIds.size() > 0) {
                        goodsIdsList = goodsIds.toJavaList(String.class);
                    }
                    JSONArray categoryIds = content.getJSONArray("categoryList");
                    if (null != categoryIds && categoryIds.size() > 0) {
                        categoryList = categoryIds.toJavaList(String.class);
                    }

                    // 生成者类型，1.平台，2.商家
                    Integer creatorType = content.getInteger("creatorType");
                    // 生成者
                    String creator = content.getString("creator");

                    if (null != creatorType && creatorType == 2 && rangeType == 0) {
                        rangeType = 4;// 商家全店
                        if (null != dealerIdList && dealerIdList.size() > 0) {
                            dealerIdList.add(creator);
                        } else {
                            dealerIdList = new ArrayList<>();
                            dealerIdList.add(creator);
                        }
                    }


                    Map resultMap = new HashMap<>();
                    resultMap.put("rangeType", rangeType);
                    resultMap.put("dealerIdList", dealerIdList);
                    resultMap.put("goodsIdsList", goodsIdsList);
                    resultMap.put("categoryList", categoryList);
                    return resultMap;
                }
            } else {
                LOGGER.error("查询商品优惠券适用范围信息失败");
                LOGGER.error("getCouponRange failed.url=>" + url);
                LOGGER.error("getCouponRange failed.error=>" + json.getString("errorMessage"));
                LOGGER.error("getCouponRange failed.param=>couponId=" + couponId);
            }
        } catch (Exception e) {
            LOGGER.error("查询商品优惠券适用范围信息异常");
            LOGGER.error("getCouponRange exception.url=>" + url);
            LOGGER.error("getCouponRange exception.error=>" + e.getMessage());
            LOGGER.error("getCouponRange exception.param=>couponId=" + couponId);
        }
        return null;
    }

    @Override
    public Map packetZoneGoods(String userId) {
        String url = M2C_HOST_URL + "/m2c.market/domain/packet/zone/goods?userId=" + userId;
        Map resultMap = new HashMap<>();
        try {
            String result = restTemplate.getForObject(url, String.class);
            JSONObject json = JSONObject.parseObject(result);
            resultMap.put("statusCode", json.getInteger("status"));
            if (json.getInteger("status") == 200) {
                JSONObject content = json.getJSONObject("content");
                if (null != content) {
                    String packetName = content.getString("packetName");
                    String packetId = content.getString("packetId");
                    String tip = content.getString("tip");
                    String bannerUrl = content.getString("bannerUrl");
                    String bgPicUrl = content.getString("bgPicUrl");
                    resultMap.put("packetName", packetName);
                    resultMap.put("packetId", packetId);
                    resultMap.put("tip", tip);
                    resultMap.put("bannerUrl", bannerUrl);
                    resultMap.put("bgPicUrl", bgPicUrl);

                    List<Map> zoneMapList = new ArrayList<>();
                    JSONArray zoneList = content.getJSONArray("zonelist");
                    Iterator<Object> zoneIt = zoneList.iterator();
                    while (zoneIt.hasNext()) {
                        Object zoneJson = zoneIt.next();
                        JSONObject zoneObject = JSONObject.parseObject(JSONObject.toJSONString(zoneJson));
                        JSONObject couponRange = zoneObject.getJSONObject("couponRange");
                        String zoneId = zoneObject.getString("zoneId");
                        String zoneName = zoneObject.getString("zoneName");
                        /**
                         * 优惠券作用范围，0：全场，1：商家，2：商品，3：品类
                         * 备注：
                         * 当作用范围为全场时，dealerId、goodsId、categoryId为排除的对象；当作用范围不是全场时，三个id为优惠券实际作用的对象
                         */
                        Integer rangeType = couponRange.getInteger("rangeType");
                        List dealerIdList = null;
                        List goodsIdsList = null;
                        List categoryList = null;
                        JSONArray dealerIds = couponRange.getJSONArray("dealerList");
                        if (null != dealerIds && dealerIds.size() > 0) {
                            dealerIdList = dealerIds.toJavaList(String.class);
                        }
                        JSONArray goodsIds = couponRange.getJSONArray("goodsList");
                        if (null != goodsIds && goodsIds.size() > 0) {
                            goodsIdsList = goodsIds.toJavaList(String.class);
                        }
                        JSONArray categoryIds = couponRange.getJSONArray("categoryList");
                        if (null != categoryIds && categoryIds.size() > 0) {
                            categoryList = categoryIds.toJavaList(String.class);
                        }

                        Map couponMap = new HashMap<>();
                        couponMap.put("rangeType", rangeType);
                        couponMap.put("dealerIdList", dealerIdList);
                        couponMap.put("goodsIdsList", goodsIdsList);
                        couponMap.put("categoryList", categoryList);

                        Map zoneMap = new HashMap<>();
                        zoneMap.put("zoneId", zoneId);
                        zoneMap.put("zoneName", zoneName);
                        zoneMap.put("couponRange", couponMap);

                        zoneMapList.add(zoneMap);
                    }
                    resultMap.put("zoneList", zoneMapList);
                    return resultMap;
                }
            } else {
                LOGGER.error("查询新人礼包专区信息失败");
                LOGGER.error("packetZoneGoods failed.url=>" + url);
                LOGGER.error("packetZoneGoods failed.error=>" + json.getString("errorMessage"));
            }
        } catch (Exception e) {
            resultMap.put("statusCode", 400);
            LOGGER.error("查询新人礼包专区信息异常");
            LOGGER.error("packetZoneGoods exception.url=>" + url);
            LOGGER.error("packetZoneGoods exception.error=>" + e.getMessage());
        }
        return resultMap;
    }

    @Override
    public Map packetZoneGoodsByZoneId(String zoneId) {
        String url = M2C_HOST_URL + "/m2c.market/domain/packet/zone/goods/detail?zoneId=" + zoneId;
        Map resultMap = new HashMap<>();
        try {
            String result = restTemplate.getForObject(url, String.class);
            JSONObject json = JSONObject.parseObject(result);
            resultMap.put("statusCode", json.getInteger("status"));
            if (json.getInteger("status") == 200) {
                JSONObject content = json.getJSONObject("content");
                if (null != content) {
                    String zoneName = content.getString("zoneName");
                    JSONObject couponRange = content.getJSONObject("couponRange");
                    /**
                     * 优惠券作用范围，0：全场，1：商家，2：商品，3：品类
                     * 备注：
                     * 当作用范围为全场时，dealerId、goodsId、categoryId为排除的对象；当作用范围不是全场时，三个id为优惠券实际作用的对象
                     */
                    Integer rangeType = couponRange.getInteger("rangeType");
                    List dealerIdList = null;
                    List goodsIdsList = null;
                    List categoryList = null;
                    JSONArray dealerIds = couponRange.getJSONArray("dealerList");
                    if (null != dealerIds && dealerIds.size() > 0) {
                        dealerIdList = dealerIds.toJavaList(String.class);
                    }
                    JSONArray goodsIds = couponRange.getJSONArray("goodsList");
                    if (null != goodsIds && goodsIds.size() > 0) {
                        goodsIdsList = goodsIds.toJavaList(String.class);
                    }
                    JSONArray categoryIds = couponRange.getJSONArray("categoryList");
                    if (null != categoryIds && categoryIds.size() > 0) {
                        categoryList = categoryIds.toJavaList(String.class);
                    }

                    Map couponMap = new HashMap<>();
                    couponMap.put("rangeType", rangeType);
                    couponMap.put("dealerIdList", dealerIdList);
                    couponMap.put("goodsIdsList", goodsIdsList);
                    couponMap.put("categoryList", categoryList);
                    couponMap.put("zoneName", zoneName);
                    return couponMap;
                }
            } else {
                LOGGER.error("查询新人礼包专区更多信息失败");
                LOGGER.error("packetZoneGoods failed.url=>" + url);
                LOGGER.error("packetZoneGoods failed.error=>" + json.getString("errorMessage"));
            }
        } catch (Exception e) {
            resultMap.put("statusCode", 400);
            LOGGER.error("查询新人礼包专区更多信息异常");
            LOGGER.error("packetZoneGoodsByZoneId exception.url=>" + url);
            LOGGER.error("packetZoneGoodsByZoneId exception.error=>" + e.getMessage());
        }
        return resultMap;
    }

    @Override
    public Map photographGetCoupon(String userId, String mediaResourceId) {
        String url = M2C_HOST_URL + "/m2c.market/domain/seek/coupon/scan/query?userId=" + userId + "&adsId=" + mediaResourceId;

        try {
            String result = restTemplate.getForObject(url, String.class);
            JSONObject json = JSONObject.parseObject(result);
            if (json.getInteger("status") == 200) {
                JSONObject contentObject = json.getJSONObject("content");
                if (null != contentObject) {
                    // 优惠券ID
                    String couponId = contentObject.getString("couponId");
                    // 优惠券优惠形式：1 减钱 2 打折
                    Integer couponForm = contentObject.getInteger("couponForm");
                    // 优惠券类型：1：代金券，2：打折券，3：分享券
                    Integer couponType = contentObject.getInteger("couponType");
                    // 门槛文案
                    String thresholdContent = contentObject.getString("thresholdContent");
                    // 面值
                    String value = contentObject.getString("value");
                    // 优惠券名称
                    String couponName = contentObject.getString("couponName");
                    // 作用范围页面内容
                    String rangeContent = contentObject.getString("rangeContent");
                    // 有效期 ,返回格式:XXXX.XX.XX - XXXX.XX.XX
                    String expirationTime = contentObject.getString("expirationTime");
                    // 有效期开始时间
                    String expirationTimeStart = contentObject.getString("expirationTimeStart");
                    // 有效期截止时间
                    String expirationTimeEnd = contentObject.getString("expirationTimeEnd");
                    // 生成者类型，1.平台，2.商家
                    Integer creatorType = contentObject.getInteger("creatorType");
                    // 活动id
                    String activityId = contentObject.getString("activityId");
                    // 活动标题
                    String activityName = contentObject.getString("activityName");
                    // 广告位id
                    String adsId = contentObject.getString("adsId");

                    Map resultMap = new HashMap<>();
                    resultMap.put("couponId", couponId);
                    resultMap.put("couponForm", couponForm);
                    resultMap.put("couponType", couponType);
                    resultMap.put("content", thresholdContent);
                    if (couponForm == 1) { // 减钱
                        resultMap.put("faceValue", Long.parseLong(value) / 10000);
                    } else { // 打折
                        resultMap.put("faceValue", value);
                    }
                    resultMap.put("couponName", couponName);
                    resultMap.put("rangeContent", rangeContent);
                    resultMap.put("expirationTime", expirationTime);
                    resultMap.put("expirationTimeStart", expirationTimeStart);
                    resultMap.put("expirationTimeEnd", expirationTimeEnd);
                    resultMap.put("activityId", activityId);
                    resultMap.put("activityName", activityName);
                    resultMap.put("creatorType", creatorType);
                    resultMap.put("adsId", adsId);
                    return resultMap;
                }
            } else {
                LOGGER.error("拍照领券查询优惠券信息失败");
                LOGGER.error("photographGetCoupon failed.url=>" + url);
                LOGGER.error("photographGetCoupon failed.error=>" + json.getString("errorMessage"));
            }
        } catch (Exception e) {
            LOGGER.error("拍照领券查询优惠券信息异常");
            LOGGER.error("photographGetCoupon exception.url=>" + url);
            LOGGER.error("photographGetCoupon exception.error=>" + e.getMessage());
        }
        return null;
    }
}
