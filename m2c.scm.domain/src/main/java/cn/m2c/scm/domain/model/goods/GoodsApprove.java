package cn.m2c.scm.domain.model.goods;

import cn.m2c.common.JsonUtils;
import cn.m2c.ddd.common.domain.model.ConcurrencySafeEntity;
import cn.m2c.scm.domain.util.GetMapValueUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 商品审核
 */
public class GoodsApprove extends ConcurrencySafeEntity {

    /**
     * 商品审核id
     */
    private String approveId;

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
     * 商品主图  存储类型是[“url1”,"url2"]
     */
    private String goodsMainImages;

    /**
     * 商品主图视频
     */
    private String goodsMainVideo;

    /**
     * 商品描述
     */
    private String goodsDesc;

    /**
     * 1:手动上架,2:审核通过立即上架
     */
    private Integer goodsShelves;

    /**
     * 审核状态，1：审核中，2：审核不通过
     */
    private Integer approveStatus;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 商品规格
     */
    private List<GoodsSkuApprove> goodsSkuApproves;

    public GoodsApprove() {
        super();
    }

    public GoodsApprove(String approveId, String dealerId, String dealerName, String goodsName, String goodsSubTitle,
                        String goodsClassifyId, String goodsBrandId, String goodsUnitId, Integer goodsMinQuantity,
                        String goodsPostageId, String goodsBarCode, String goodsKeyWord, String goodsGuarantee,
                        String goodsMainImages, String goodsDesc, Integer goodsShelves, String goodsSkuApproves, List<String> skuCodes) {
        this.approveId = approveId;
        this.dealerId = dealerId;
        this.dealerName = dealerName;
        this.goodsName = goodsName;
        this.goodsSubTitle = goodsSubTitle;
        this.goodsClassifyId = goodsClassifyId;
        this.goodsBrandId = goodsBrandId;
        this.goodsUnitId = goodsUnitId;
        this.goodsMinQuantity = goodsMinQuantity;
        this.goodsPostageId = goodsPostageId;
        this.goodsBarCode = goodsBarCode;
        this.goodsKeyWord = goodsKeyWord;
        this.goodsGuarantee = goodsGuarantee;
        this.goodsMainImages = goodsMainImages;
        this.goodsDesc = goodsDesc;
        this.goodsShelves = goodsShelves;
        this.approveStatus = 1;
        //商品规格格式：[{"skuApproveId":"SPSHA5BDED943A1D42CC9111B3723B0987BF","skuName":"L,红","supplyPrice":4000,
        // "weight":20.5,"availableNum":200,"goodsCode":"111111","marketPrice":6000,"photographPrice":5000,"showStatus":2}]
        if (null == this.goodsSkuApproves) {
            this.goodsSkuApproves = new ArrayList<>();
        } else {
            this.goodsSkuApproves.clear();
        }
        List<Map> skuList = JsonUtils.toList(goodsSkuApproves, Map.class);
        if (null != skuList && skuList.size() > 0) {
            for (int i = 0; i < skuList.size(); i++) {
                this.goodsSkuApproves.add(createGoodsSkuApprove(skuList.get(i), skuCodes.get(i)));
            }
        }
    }

    private GoodsSkuApprove createGoodsSkuApprove(Map map, String skuCode) {
        String skuApproveId = GetMapValueUtils.getStringFromMapKey(map, "skuApproveId");
        if (StringUtils.isEmpty(skuApproveId)) {
            skuApproveId = skuCode;
        }
        String skuName = GetMapValueUtils.getStringFromMapKey(map, "skuName");
        Integer availableNum = GetMapValueUtils.getIntFromMapKey(map, "availableNum");
        Float weight = GetMapValueUtils.getFloatFromMapKey(map, "weight");
        Long photographPrice = GetMapValueUtils.getLongFromMapKey(map, "photographPrice");
        Long marketPrice = GetMapValueUtils.getLongFromMapKey(map, "marketPrice");
        Long supplyPrice = GetMapValueUtils.getLongFromMapKey(map, "supplyPrice");
        String goodsCode = GetMapValueUtils.getStringFromMapKey(map, "goodsCode");
        Integer showStatus = GetMapValueUtils.getIntFromMapKey(map, "showStatus");
        GoodsSkuApprove goodsSkuApprove = new GoodsSkuApprove(this, skuApproveId, skuName, availableNum,
                weight, photographPrice, marketPrice, supplyPrice, goodsCode,
                showStatus);
        return goodsSkuApprove;
    }
}