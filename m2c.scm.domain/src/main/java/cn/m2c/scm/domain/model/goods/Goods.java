package cn.m2c.scm.domain.model.goods;

import cn.m2c.common.JsonUtils;
import cn.m2c.ddd.common.domain.model.ConcurrencySafeEntity;
import cn.m2c.ddd.common.domain.model.DomainEventPublisher;
import cn.m2c.ddd.common.serializer.ObjectSerializer;
import cn.m2c.scm.domain.model.dealer.event.DealerReportStatisticsEvent;
import cn.m2c.scm.domain.model.goods.event.GoodsAddEvent;
import cn.m2c.scm.domain.model.goods.event.GoodsApproveAddEvent;
import cn.m2c.scm.domain.model.goods.event.GoodsChangedEvent;
import cn.m2c.scm.domain.model.goods.event.GoodsDeleteEvent;
import cn.m2c.scm.domain.model.goods.event.GoodsModifyApproveSkuEvent;
import cn.m2c.scm.domain.model.goods.event.GoodsOffShelfEvent;
import cn.m2c.scm.domain.model.goods.event.GoodsUpShelfEvent;
import cn.m2c.scm.domain.util.DealerReportType;
import cn.m2c.scm.domain.util.GetMapValueUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 商品
 */
public class Goods extends ConcurrencySafeEntity {
    /**
     * 商品id
     */
    private String goodsId;

    /**
     * 商家ID
     */
    private String dealerId;

    /**
     * 商家名称
     */
    private String dealerName;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品副标题
     */
    private String goodsSubTitle;

    /**
     * 商品分类id
     */
    private String goodsClassifyId;

    /**
     * 商品品牌id
     */
    private String goodsBrandId;

    /**
     * 商品品牌名称
     */
    private String goodsBrandName;

    /**
     * 商品计量单位id
     */
    private String goodsUnitId;

    /**
     * 最小起订量
     */
    private Integer goodsMinQuantity;

    /**
     * 运费模板id
     */
    private String goodsPostageId;

    /**
     * 商品条形码
     */
    private String goodsBarCode;

    /**
     * 关键词
     */
    private String goodsKeyWord;

    /**
     * 商品保障
     */
    private String goodsGuarantee;

    /**
     * 是否有识别图，0:没有，1：有
     */
    private Integer recognizedFlag;

    /**
     * 识别图列表
     */
    private List<GoodsRecognized> goodsRecognizeds;

    /**
     * 商品主图  存储类型是[“url1”,"url2"]
     */
    private String goodsMainImages;

    /**
     * 商品主图视频
     */
    private String goodsMainVideo;

    /**商品主图视频时长*/
    private Integer goodsMainVideoDuration;
    
    /**商品主图视频大小*/
    private Integer goodsMainVideoSize;
    
    /**
     * 商品描述
     */
    private String goodsDesc;

    /**
     * 1:手动上架,2:审核通过立即上架
     */
    private Integer goodsShelves;

    /**
     * 商品状态，1：仓库中，2：出售中，3：已售罄
     */
    private Integer goodsStatus;

    /**
     * 商品规格,格式：[{"itemName":"尺寸","itemValue":["L","M"]},{"itemName":"颜色","itemValue":["蓝色","白色"]}]
     */
    private String goodsSpecifications;

    /**
     * 商品规格
     */
    private List<GoodsSku> goodsSKUs;

    /**
     * 是否删除，1:正常，2：已删除
     */
    private Integer delStatus;

    /**
     * 是否是多规格：0：单规格，1：多规格
     */
    private Integer skuFlag;

    /**
     * 创建时间
     */
    private Date createdDate;

    /**
     * 商品投放状态：0：未投放，1：投放
     */
    private Integer goodsLaunchStatus;

    /**
     * 最后更新时间
     */
    private Date lastUpdateTime;


    public Goods() {
        super();
    }

    public Goods(String goodsId, String dealerId, String dealerName, String goodsName, String goodsSubTitle,
                 String goodsClassifyId, String goodsBrandId, String goodsBrandName, String goodsUnitId, Integer goodsMinQuantity,
                 String goodsPostageId, String goodsBarCode, String goodsKeyWord, String goodsGuarantee,
                 String goodsMainImages, String goodsMainVideo, Integer goodsMainVideoDuration, Integer goodsMainVideoSize,
                 String goodsDesc, Integer goodsShelves, String goodsSpecifications, String goodsSKUs, Integer skuFlag) {
        this.goodsId = goodsId;
        this.dealerId = dealerId;
        this.dealerName = dealerName;
        this.goodsName = goodsName;
        this.goodsSubTitle = goodsSubTitle;
        this.goodsClassifyId = goodsClassifyId;
        this.goodsBrandId = goodsBrandId;
        this.goodsBrandName = goodsBrandName;
        this.goodsUnitId = goodsUnitId;
        this.goodsMinQuantity = goodsMinQuantity;
        this.goodsPostageId = goodsPostageId;
        this.goodsBarCode = goodsBarCode;
        this.goodsKeyWord = goodsKeyWord;
        this.goodsGuarantee = goodsGuarantee;
        this.goodsMainImages = goodsMainImages;
        this.goodsMainVideo = goodsMainVideo;
        this.goodsMainVideoDuration = goodsMainVideoDuration;
        this.goodsMainVideoSize = goodsMainVideoSize;
        this.goodsDesc = goodsDesc;
        this.goodsShelves = goodsShelves;
        this.skuFlag = skuFlag;
        if (this.goodsShelves == 1) {//1:手动上架,2:审核通过立即上架
            this.goodsStatus = 1;
        } else {
            this.goodsStatus = 2;
            /*DomainEventPublisher
                    .instance()
                    .publish(new GoodsUpShelfEvent(this.goodsId, this.goodsPostageId, this.goodsRecognizeds(), 1));*/
        }
        this.goodsSpecifications = goodsSpecifications;

        this.createdDate = new Date();
        if (null == this.goodsSKUs) {
            this.goodsSKUs = new ArrayList<>();
        } else {
            this.goodsSKUs.clear();
        }
        List<Map> skuList = ObjectSerializer.instance().deserialize(goodsSKUs, List.class);
        if (null != skuList && skuList.size() > 0) {
            for (Map map : skuList) {
                this.goodsSKUs.add(createGoodsSku(map));
            }
        }

        // 已售罄
        soldOut();

        DomainEventPublisher
                .instance()
                .publish(new GoodsAddEvent(this.goodsId, this.goodsPostageId, this.goodsUnitId, getStandardId(goodsSpecifications), this.goodsShelves));

        Map<String, Map> dealerInfo = new HashMap<>();
        Map infoMap = new HashMap<>();
        infoMap.put("num", 1);
        dealerInfo.put(this.dealerId, infoMap);
        // 数据统计事件
        DomainEventPublisher.instance().publish(new DealerReportStatisticsEvent(dealerInfo, DealerReportType.GOODS_ADD, new Date()));
    }

    /**
     * [{"itemName":"发送","itemValue":[{"spec_name":"16G"},{"spec_name":"32G"}],"state1":"","standardId":"GG1B487E5857C44367AB50C05A5E4B5A5E"},
     * {"itemName":"规格是嘛","itemValue":[{"spec_name":"红"},{"spec_name":"黑"}],"state1":"","standardId":"GGF01849F898A54B5E9FB565388272C2FA"}]
     *
     * @return
     */
    private List<String> getStandardId(String goodsSpecifications) {
        List<String> standardIds = new ArrayList<>();
        if (StringUtils.isNotEmpty(goodsSpecifications)) {
            List<Map> list = JsonUtils.toList(goodsSpecifications, Map.class);
            if (null != list && list.size() > 0) {
                for (Map map : list) {
                    if (map.containsKey("standardId")) {
                        String standardId = null != map.get("standardId") ? (String) map.get("standardId") : "";
                        standardIds.add(standardId);
                    }
                }
            }
        }
        return standardIds;
    }

    private GoodsSku createGoodsSku(Map map) {
        String skuId = GetMapValueUtils.getStringFromMapKey(map, "skuId");
        String skuName = GetMapValueUtils.getStringFromMapKey(map, "skuName");
        Integer availableNum = GetMapValueUtils.getIntFromMapKey(map, "availableNum");
        Float weight = GetMapValueUtils.getFloatFromMapKey(map, "weight");
        Long photographPrice = GetMapValueUtils.getLongFromMapKey(map, "photographPrice");
        Long marketPrice = GetMapValueUtils.getLongFromMapKey(map, "marketPrice");
        Long supplyPrice = GetMapValueUtils.getLongFromMapKey(map, "supplyPrice");
        String goodsCode = GetMapValueUtils.getStringFromMapKey(map, "goodsCode");
        Integer showStatus = GetMapValueUtils.getIntFromMapKey(map, "showStatus");
        GoodsSku goodsSku = new GoodsSku(this, skuId, skuName, availableNum, availableNum, weight,
                photographPrice, marketPrice, supplyPrice, goodsCode, showStatus);
        return goodsSku;
    }

    /**
     * 修改商品需要审核的供货价和拍获价或增加sku
     *
     * @param goodsSKUs
     */
    public void modifyApproveGoodsSku(String goodsClassifyId, String goodsSpecifications, String goodsSKUs) {
        this.goodsClassifyId = goodsClassifyId;
        this.goodsSpecifications = goodsSpecifications;
        List<Map> skuList = ObjectSerializer.instance().deserialize(goodsSKUs, List.class);
        if (null != skuList && skuList.size() > 0) {
            if (null == this.goodsSKUs) {
                this.goodsSKUs = new ArrayList<>();
            }
            List<Map> addSkuList = new ArrayList<>();
            for (Map map : skuList) {
                String skuId = GetMapValueUtils.getStringFromMapKey(map, "skuId");
                // 判断商品规格sku是否存在,存在就修改供货价和拍获价，不存在就增加商品sku
                GoodsSku goodsSku = getGoodsSKU(skuId);
                if (null == goodsSku) {// 增加规格
                    this.goodsSKUs.add(createGoodsSku(map));
                    Map skuMap = new HashMap<>();
                    skuMap.put("skuId", skuId);
                    String skuName = GetMapValueUtils.getStringFromMapKey(map, "skuName");
                    skuMap.put("skuName", skuName);
                    addSkuList.add(skuMap);
                } else { // 修改供货价和拍获价
                    Long photographPrice = GetMapValueUtils.getLongFromMapKey(map, "photographPrice");
                    Long supplyPrice = GetMapValueUtils.getLongFromMapKey(map, "supplyPrice");
                    goodsSku.modifyApprovePrice(photographPrice, supplyPrice);
                }
            }
            DomainEventPublisher
                    .instance()
                    .publish(new GoodsModifyApproveSkuEvent(this.goodsId, addSkuList));
        }
    }

    public List<GoodsHistory> getGoodsHistory(String goodsClassifyId, String goodsSKUs, String changeReason, String changeInfo) {
        List<GoodsHistory> histories = new ArrayList<>();
        String historyNo = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
        Date nowDate = new Date();

        Map changeMap = JsonUtils.toMap(changeInfo);
        String newServiceRate = "";
        String settlementMode = "";
        if (null != changeMap && !"".equals(changeMap)) {
            newServiceRate = null != changeMap && null != changeMap.get("newServiceRate") ? changeMap.get("newServiceRate").toString() : null;
            settlementMode = null != changeMap && null != changeMap.get("settlementMode") ? changeMap.get("settlementMode").toString() : null;
        }
        if (!this.goodsClassifyId.equals(goodsClassifyId)) {
            // {"newClassifyName":"手机,iOS系统","oldServiceRate":"0","oldClassifyName":"手机,墨迹","newServiceRate":"0","settlementMode":1}
            String oldServiceRate = null;
            String oldClassifyName = null;
            String newClassifyName = null;
            if (null != changeMap && !"".equals(changeMap)) {
                oldServiceRate = null != changeMap.get("oldServiceRate") ? changeMap.get("oldServiceRate").toString() : null;
                oldClassifyName = null != changeMap.get("oldClassifyName") ? changeMap.get("oldClassifyName").toString() : null;
                newClassifyName = null != changeMap.get("newClassifyName") ? changeMap.get("newClassifyName").toString() : null;
            }
            Map before = new HashMap<>();
            before.put("goodsClassifyId", this.goodsClassifyId);
            before.put("goodsClassifyName", oldClassifyName);
            before.put("serviceRate", oldServiceRate);
            before.put("settlementMode", settlementMode);
            Map after = new HashMap<>();
            after.put("goodsClassifyId", goodsClassifyId);
            after.put("goodsClassifyName", newClassifyName);
            after.put("serviceRate", newServiceRate);

            String historyId = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
            GoodsHistory history = new GoodsHistory(historyId, historyNo, this.goodsId,
                    1, JsonUtils.toStr(before),
                    JsonUtils.toStr(after), changeReason, nowDate);
            histories.add(history);
        }

        List<Map> skuList = ObjectSerializer.instance().deserialize(goodsSKUs, List.class);
        if (null != skuList && skuList.size() > 0) {
            List<Map> addSkuList = new ArrayList<>();
            for (Map map : skuList) {
                String skuId = GetMapValueUtils.getStringFromMapKey(map, "skuId");
                // 判断商品规格sku是否存在,存在就修改供货价和拍获价，不存在就增加商品sku
                GoodsSku goodsSku = getGoodsSKU(skuId);
                if (null == goodsSku) {// 增加规格
                    map.put("serviceRate", newServiceRate);
                    map.put("settlementMode", settlementMode);
                    addSkuList.add(map);
                } else { // 修改供货价和拍获价
                    Long photographPrice = GetMapValueUtils.getLongFromMapKey(map, "photographPrice");
                    Long supplyPrice = GetMapValueUtils.getLongFromMapKey(map, "supplyPrice");
                    if (goodsSku.isModifyPhotographPrice(photographPrice)) {
                        String historyId = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
                        Map before = new HashMap<>();
                        // 取商品库价格
                        Map photographPriceMap = goodsSku.getChangePhotographPrice(photographPrice);
                        before.put("photographPrice", photographPriceMap.get("oldPhotographPrice"));
                        before.put("skuId", photographPriceMap.get("skuId"));
                        before.put("skuName", photographPriceMap.get("skuName"));

                        Map after = new HashMap<>();
                        after.put("photographPrice", photographPriceMap.get("newPhotographPrice"));
                        GoodsHistory history = new GoodsHistory(historyId, historyNo, this.goodsId,
                                2, JsonUtils.toStr(before),
                                JsonUtils.toStr(after), changeReason, nowDate);
                        histories.add(history);
                    }
                    if (goodsSku.isModifySupplyPrice(supplyPrice)) {
                        String historyId = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
                        Map before = new HashMap<>();
                        // 取商品库价格
                        Map supplyPriceMap = goodsSku.getChangeSupplyPrice(photographPrice);
                        before.put("supplyPrice", supplyPriceMap.get("oldSupplyPrice"));
                        before.put("skuId", supplyPriceMap.get("skuId"));
                        before.put("skuName", supplyPriceMap.get("skuName"));

                        Map after = new HashMap<>();
                        after.put("supplyPrice", supplyPriceMap.get("newSupplyPrice"));
                        GoodsHistory history = new GoodsHistory(historyId, historyNo, this.goodsId,
                                3, JsonUtils.toStr(before),
                                JsonUtils.toStr(after), changeReason, nowDate);
                        histories.add(history);
                    }
                }
            }
            if (null != addSkuList && addSkuList.size() > 0) {
                String historyId = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
                GoodsHistory history = new GoodsHistory(historyId, historyNo, this.goodsId,
                        4, "",
                        JsonUtils.toStr(addSkuList), changeReason, nowDate);
                histories.add(history);
            }
        }
        return histories;
    }

    /**
     * 根据skuId获取商品规格
     *
     * @param skuId
     * @return
     */
    private GoodsSku getGoodsSKU(String skuId) {
        GoodsSku goodsSku = null;
        List<GoodsSku> goodsSKUs = this.goodsSKUs;
        for (GoodsSku sku : goodsSKUs) {
            goodsSku = sku.getGoodsSKU(skuId);
            if (null != goodsSku) {
                return goodsSku;
            }
        }
        return goodsSku;
    }

    public boolean isNeedApprove(String goodsClassifyId, String goodsSKUs) {
        //修改分类，供货价、拍获价、规格需要审批
        boolean isNeedApprove = false;
        if (!goodsClassifyId.equals(this.goodsClassifyId)) { // 修改分类需要审核
            isNeedApprove = true;
        }
        List<Map> skuList = ObjectSerializer.instance().deserialize(goodsSKUs, List.class);
        if (null != skuList && skuList.size() > 0) {
            for (Map map : skuList) {
                String skuId = GetMapValueUtils.getStringFromMapKey(map, "skuId");
                // 判断商品规格sku是否存在,存在就修改供货价和拍获价，不存在就增加商品sku
                GoodsSku goodsSku = getGoodsSKU(skuId);
                if (null == goodsSku) {// 增加了规格
                    isNeedApprove = true;
                } else {
                    // 判断供货价和拍获价是否修改
                    Long photographPrice = GetMapValueUtils.getLongFromMapKey(map, "photographPrice");
                    Long supplyPrice = GetMapValueUtils.getLongFromMapKey(map, "supplyPrice");
                    if (goodsSku.isModifyNeedApprovePrice(photographPrice, supplyPrice)) { //修改了供货价和拍获价
                        isNeedApprove = true;
                    }
                }
            }
        }
        return isNeedApprove;
    }


    /**
     * 修改商品
     */
    public void modifyGoods(String goodsName, String goodsSubTitle,
                            String goodsClassifyId, String goodsBrandId, String goodsBrandName, String goodsUnitId, Integer goodsMinQuantity,
                            String goodsPostageId, String goodsBarCode, String goodsKeyWord, String goodsGuarantee,
                            String goodsMainImages, String goodsMainVideo, Integer goodsMainVideoDuration, Integer goodsMainVideoSize,
                            String goodsDesc, String goodsSpecifications, String goodsSKUs, String changeReason,
                            Float oldServiceRate, Float newServiceRate, String oldClassifyName, String newClassifyName, Integer settlementMode) {
        this.lastUpdateTime = new Date();
        String oldGoodsUnitId = this.goodsUnitId;
        String newGoodsUnitId = goodsUnitId;
        this.goodsName = goodsName;
        this.goodsSubTitle = goodsSubTitle;
        this.goodsBrandId = goodsBrandId;
        this.goodsBrandName = goodsBrandName;
        this.goodsUnitId = goodsUnitId;
        this.goodsMinQuantity = goodsMinQuantity;
        String oldGoodsPostageId = this.goodsPostageId;
        this.goodsPostageId = goodsPostageId;
        this.goodsBarCode = goodsBarCode;
        this.goodsKeyWord = goodsKeyWord;
        this.goodsGuarantee = goodsGuarantee;
        this.goodsMainImages = goodsMainImages;
        this.goodsMainVideo = goodsMainVideo;
        this.goodsMainVideoDuration = goodsMainVideoDuration;
        this.goodsMainVideoSize = goodsMainVideoSize;
        this.goodsDesc = goodsDesc;

        //修改分类，供货价、拍获价、规格需要审批
        boolean isNeedApprove = false;
        Map changeGoodsInfo = new HashMap<>(); // 审核字段变更记录历史
        if (!goodsClassifyId.equals(this.goodsClassifyId)) { // 修改分类需要审核
            isNeedApprove = true;
            changeGoodsInfo.put("oldGoodsClassifyId", this.goodsClassifyId);
            changeGoodsInfo.put("newGoodsClassifyId", goodsClassifyId);
            changeGoodsInfo.put("oldServiceRate", oldServiceRate);
            changeGoodsInfo.put("newServiceRate", newServiceRate);
            changeGoodsInfo.put("oldClassifyName", oldClassifyName);
            changeGoodsInfo.put("newClassifyName", newClassifyName);
            changeGoodsInfo.put("settlementMode", settlementMode);
        }

        List<Map> skuList = ObjectSerializer.instance().deserialize(goodsSKUs, List.class);
        if (null != skuList && skuList.size() > 0) {
            boolean goodsNumThanZero = false;
            // 增加的sku
            List<Map> addGoodsSkuList = new ArrayList<>();
            // 变更的拍获价
            List<Map> photographPriceChangeList = new ArrayList<>();
            // 变更的供货价
            List<Map> supplyPriceChangeList = new ArrayList<>();
            for (Map map : skuList) {
                String skuId = GetMapValueUtils.getStringFromMapKey(map, "skuId");
                // 判断商品规格sku是否存在,存在就修改供货价和拍获价，不存在就增加商品sku
                GoodsSku goodsSku = getGoodsSKU(skuId);

                Integer showStatus = 2; //是否对外展示，1：不展示，2：展示
                if (null != this.skuFlag && this.skuFlag == 1) {
                    Boolean isShow = GetMapValueUtils.getBooleanFromMapKey(map, "showStatus");
                    if (!isShow) {
                        showStatus = 1;
                    }
                }
                if (null == goodsSku) {// 增加了规格
                    isNeedApprove = true;
                    map.put("showStatus", showStatus);
                    map.put("serviceRate", newServiceRate);
                    map.put("settlementMode", settlementMode);
                    addGoodsSkuList.add(map);
                } else {
                    Integer availableNum = GetMapValueUtils.getIntFromMapKey(map, "availableNum");
                    if (availableNum > 0) {
                        goodsNumThanZero = true;
                    }
                    Float weight = GetMapValueUtils.getFloatFromMapKey(map, "weight");
                    Long marketPrice = GetMapValueUtils.getLongFromMapKey(map, "marketPrice");
                    String goodsCode = GetMapValueUtils.getStringFromMapKey(map, "goodsCode");

                    // 修改商品规格不需要审批的信息
                    goodsSku.modifyNotApproveGoodsSku(availableNum, weight, marketPrice, goodsCode, showStatus);

                    // 判断供货价和拍获价是否修改
                    Long photographPrice = GetMapValueUtils.getLongFromMapKey(map, "photographPrice");
                    Long supplyPrice = GetMapValueUtils.getLongFromMapKey(map, "supplyPrice");
                    if (goodsSku.isModifyNeedApprovePrice(photographPrice, supplyPrice)) { //修改了供货价和拍获价
                        isNeedApprove = true;

                        // 获取变更的拍获价和供货价信息
                        Map photographPriceMap = goodsSku.getChangePhotographPrice(photographPrice);
                        if (null != photographPriceMap) {
                            photographPriceChangeList.add(photographPriceMap);
                        }
                        Map supplyPriceMap = goodsSku.getChangeSupplyPrice(supplyPrice);
                        if (null != supplyPriceMap) {
                            supplyPriceChangeList.add(supplyPriceMap);
                        }
                    }
                }
            }

            // 增加的sku
            if (null != addGoodsSkuList && addGoodsSkuList.size() > 0) {
                changeGoodsInfo.put("addGoodsSkuList", addGoodsSkuList);
            }
            // 变更的拍获价
            if (null != photographPriceChangeList && photographPriceChangeList.size() > 0) {
                changeGoodsInfo.put("photographPriceChangeList", photographPriceChangeList);
            }
            // 变更的供货价
            if (null != supplyPriceChangeList && supplyPriceChangeList.size() > 0) {
                changeGoodsInfo.put("supplyPriceChangeList", supplyPriceChangeList);
            }

            if (goodsNumThanZero) {  // 库存不为0
                if (this.goodsStatus == 3) { // 若商品为已售罄则改为在售中
                    this.goodsStatus = 2;
                }
            } else { // 库存为0
                if (this.goodsStatus == 2) { // 若商品为在售中改为已售罄
                    this.goodsStatus = 3;
                }
            }
        }

        if (isNeedApprove) {//发布事件，增加一条待审核商品记录
            String spec = this.goodsSpecifications;
            if (null != this.skuFlag && this.skuFlag == 1) {//是否是多规格：0：单规格，1：多规格
                spec = goodsSpecifications;
            }
            changeGoodsInfo.put("changeReason", changeReason);
            DomainEventPublisher
                    .instance()
                    .publish(new GoodsApproveAddEvent(this.goodsId, this.dealerId, this.dealerName, this.goodsName,
                            this.goodsSubTitle, goodsClassifyId, this.goodsBrandId, this.goodsBrandName, this.goodsUnitId,
                            this.goodsMinQuantity, this.goodsPostageId, this.goodsBarCode,
                            this.goodsKeyWord, this.goodsGuarantee, this.goodsMainImages, this.goodsMainVideo, this.goodsMainVideoDuration,
                            this.goodsMainVideoSize, this.goodsDesc, spec, goodsSKUs, this.skuFlag, changeGoodsInfo));
        }

        DomainEventPublisher.instance().publish(new GoodsChangedEvent(this.goodsId, this.goodsName, this.dealerId, this.dealerName,
                oldGoodsUnitId, newGoodsUnitId, oldGoodsPostageId, this.goodsPostageId, this.goodsStatus));
    }

    /**
     * 删除商品
     */
    public void remove() {
        this.lastUpdateTime = new Date();
        this.delStatus = 2;
        DomainEventPublisher
                .instance()
                .publish(new GoodsDeleteEvent(this.goodsId, this.goodsStatus, this.goodsPostageId, this.goodsUnitId, getStandardId(goodsSpecifications)));
    }

    /**
     * 上架,商品状态，1：仓库中，2：出售中，3：已售罄
     */
    public void upShelf() {
        this.lastUpdateTime = new Date();
        this.goodsStatus = 2;
        Integer total = 0;
        for (GoodsSku goodsSku : this.goodsSKUs) {
            total = total + goodsSku.availableNum();
        }
        if (total <= 0) {
            this.goodsStatus = 3;
        }
        DomainEventPublisher
                .instance()
                .publish(new GoodsUpShelfEvent(this.goodsId, this.goodsPostageId, this.goodsRecognizeds(), 1));
    }

    /**
     * 下架,商品状态，1：仓库中，2：出售中，3：已售罄
     */
    public void offShelf() {
        this.lastUpdateTime = new Date();
        this.goodsStatus = 1;
        DomainEventPublisher
                .instance()
                .publish(new GoodsOffShelfEvent(this.goodsId, this.goodsPostageId, this.goodsRecognizeds(), 0));
    }

    /**
     * 修改商品识别图
     */
    public void modifyRecognized(String recognizedNo, String recognizedId, String recognizedUrl) {
        for (GoodsRecognized goodsRecognized : this.goodsRecognizeds) {
            if (goodsRecognized.isEqualsRecognizedNo(recognizedNo)) {
                goodsRecognized.modifyRecognized(recognizedId, recognizedUrl);
            }
        }
    }

    /**
     * 添加商品识别图
     */
    public void addRecognized(String recognizedId, String recognizedUrl) {
        if (null == this.goodsRecognizeds) {
            this.goodsRecognizeds = new ArrayList<>();
        }
        this.goodsRecognizeds.add(new GoodsRecognized(this, recognizedId, recognizedUrl));
        this.recognizedFlag = 1;
    }

    public boolean overRecognizedMaxLimit(Integer maxLimit) {
        if (null == this.goodsRecognizeds || this.goodsRecognizeds.size() == 0) {
            return false;
        } else {
            return this.goodsRecognizeds.size() >= maxLimit;
        }
    }

    /**
     * 删除商品识别图
     */
    public void delRecognized(String recognizedNo) {
        Iterator<GoodsRecognized> it = this.goodsRecognizeds.iterator();
        while (it.hasNext()) {
            if (it.next().isEqualsRecognizedNo(recognizedNo)) {
                it.remove();
            }
        }
        if (null == this.goodsRecognizeds || this.goodsRecognizeds.size() <= 0) {
            this.recognizedFlag = 0;
        }
    }


    public Integer getId() {
        return Integer.parseInt(String.valueOf(this.id()));
    }

    /**
     * 上架,商品状态，1：仓库中，2：出售中，3：已售罄
     */
    public void soldOut() {
        if (this.goodsStatus == 2) {
            Integer total = 0;
            for (GoodsSku goodsSku : this.goodsSKUs) {
                total = total + goodsSku.availableNum();
            }
            if (total <= 0) {
                this.goodsStatus = 3;
            }
        }
    }

    public void orderCancel() {
        if (this.goodsStatus == 3) {
            this.goodsStatus = 2;
        }
    }

    /**
     * 修改商品品牌名称
     */
    public void modifyBrandName(String brandName) {
        this.goodsBrandName = brandName;
    }

    /**
     * 修改商品供应商名称
     */
    public void modifyDealerName(String dealerName) {
        this.dealerName = dealerName;
    }

    /**
     * 修改商品投放状态
     */
    public void launchGoods() {
        this.goodsLaunchStatus = 1;
    }

    public List<GoodsRecognized> goodsRecognizeds() {
        return goodsRecognizeds;
    }

    public Integer goodsStatus() {
        return goodsStatus;
    }

    public void modifyGoodsMainImages(List<String> images) {
        this.goodsMainImages = JsonUtils.toStr(images);
    }

    /**
     * 删除商品保障
     *
     * @param guaranteeId
     */
    public void delGoodsGuarantee(String guaranteeId) {
        List list = ObjectSerializer.instance().deserialize(this.goodsGuarantee, List.class);
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String goodsGuaranteeId = it.next();
            if (goodsGuaranteeId.equals(guaranteeId)) {
                it.remove();
            }
        }
        this.goodsGuarantee = JsonUtils.toStr(list);
    }

    public String goodsId() {
        return goodsId;
    }

    public String dealerId() {
        return dealerId;
    }

    public String goodsName() {
        return goodsName;
    }

    public List<String> goodsMainImages() {
        if (StringUtils.isNotEmpty(this.goodsMainImages)) {
            List<String> list = JsonUtils.toList(this.goodsMainImages, String.class);
            return list;
        }
        return null;
    }

    public Map goodsNeedApproveInfo() {
        Map map = new HashMap<>();
        map.put("goodsClassifyId", this.goodsClassifyId);
        List<Map> skuMaps = new ArrayList<>();
        for (GoodsSku goodsSku : this.goodsSKUs) {
            skuMaps.add(goodsSku.convertToMap());
        }
        map.put("goodsSKUs", skuMaps);
        return map;
    }

    public String goodsClassifyId() {
        return goodsClassifyId;
    }

    public boolean isSelling() {
        return this.delStatus == 1 && this.goodsStatus == 2;
    }

    public void actReturnUpdateStatus() {
        if (this.delStatus == 1 && this.goodsStatus == 3) {
            this.goodsStatus = 2;
        }
    }
}