package cn.m2c.scm.application.goods.query.data.representation;

import cn.m2c.common.JsonUtils;
import cn.m2c.scm.application.goods.query.data.bean.GoodsHistoryBean;
import cn.m2c.scm.application.utils.Utils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 商品变更历史
 */
public class GoodsHistoryRepresentation {
    // 变更时间
    private String changeTime;
    // 变更类型，1：修改商品分类，2：修改拍获价，3：修改供货价，4：增加sku
    private Integer changeType;
    // 变更内容
    private String changeContent;
    // 变更前
    private String beforeContent;
    // 变更后
    private String afterContent;
    // 增加sku
    private List<Map> addSku;
    // 变更理由
    private String changeReason;

    public GoodsHistoryRepresentation(GoodsHistoryBean history) {
        this.changeTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(history.getCreateTime());
        this.changeType = history.getChangeType();
        this.changeReason = history.getChangeReason();
        Map beforeMap = JsonUtils.toMap(history.getBeforeContent());
        if (history.getChangeType() == 1) {
            Map afterMap = JsonUtils.toMap(history.getAfterContent());
            this.changeContent = "修改商品分类";

            // 变更前 {"goodsClassifyName":"手机,iOS系统","goodsClassifyId":"SPFL6104939BD41E4E2CB4715F19306C3478","serviceRate":"0","settlementMode":"1"}
            StringBuffer beforeSb = new StringBuffer();
            beforeSb.append(beforeMap.get("goodsClassifyName"));
            Integer settlementMode = null != beforeMap.get("settlementMode") ? Integer.parseInt(beforeMap.get("settlementMode").toString()) : null;
            if (null != settlementMode && settlementMode == 2) {
                beforeSb.append("(费率").append(beforeMap.get("serviceRate")).append("%)");
            }
            this.beforeContent = beforeSb.toString();

            // 变更后 {"goodsClassifyName":"手机,iOS系统","goodsClassifyId":"SPFL6104939BD41E4E2CB4715F19306C3478","serviceRate":"0"}
            StringBuffer afterSb = new StringBuffer();
            afterSb.append(afterMap.get("goodsClassifyName"));
            if (null != settlementMode && settlementMode == 2) {
                afterSb.append("(费率").append(afterMap.get("serviceRate")).append("%)");
            }
            this.afterContent = afterSb.toString();

        } else if (history.getChangeType() == 2) {
            Map afterMap = JsonUtils.toMap(history.getAfterContent());
            this.changeContent = "修改拍获价";
            // 变更前 {"skuName":"5寸,红色","photographPrice":9970000,"skuId":"20171205155603147254"}
            StringBuffer beforeSb = new StringBuffer();
            String beforePrice = Utils.moneyFormatCN(Long.parseLong(String.valueOf(beforeMap.get("photographPrice"))));
            beforeSb.append(beforePrice).append("元");
            if (null != beforeMap.get("skuName") && !"".equals(beforeMap.get("skuName"))) {
                beforeSb.append("（规格值：").append(beforeMap.get("skuName")).append(")");
            }
            this.beforeContent = beforeSb.toString();

            // 变更后 {"photographPrice":9980000}
            StringBuffer afterSb = new StringBuffer();
            String afterPrice = Utils.moneyFormatCN(Long.parseLong(String.valueOf(afterMap.get("photographPrice"))));
            afterSb.append(afterPrice).append("元");
            if (null != beforeMap.get("skuName") && !"".equals(beforeMap.get("skuName"))) {
                afterSb.append("（规格值：").append(beforeMap.get("skuName")).append(")");
            }
            this.afterContent = afterSb.toString();
        } else if (history.getChangeType() == 3) {
            Map afterMap = JsonUtils.toMap(history.getAfterContent());
            this.changeContent = "修改供货价";
            // 变更前 {"skuName":"5寸,白色","supplyPrice":9950000,"skuId":"20171205160651663483"}
            StringBuffer beforeSb = new StringBuffer();
            String beforePrice = Utils.moneyFormatCN(Long.parseLong(String.valueOf(beforeMap.get("supplyPrice"))));
            beforeSb.append(beforePrice).append("元");
            if (null != beforeMap.get("skuName") && !"".equals(beforeMap.get("skuName"))) {
                beforeSb.append("（规格值：").append(beforeMap.get("skuName")).append(")");
            }
            this.beforeContent = beforeSb.toString();

            // 变更后 {"supplyPrice":9960000}
            StringBuffer afterSb = new StringBuffer();
            String afterPrice = Utils.moneyFormatCN(Long.parseLong(String.valueOf(afterMap.get("supplyPrice"))));
            afterSb.append(afterPrice).append("元");
            if (null != beforeMap.get("skuName") && !"".equals(beforeMap.get("skuName"))) {
                afterSb.append("（规格值：").append(beforeMap.get("skuName")).append(")");
            }
            this.afterContent = afterSb.toString();
        } else {
            this.addSku = new ArrayList<>();
            List<Map> afterList = JsonUtils.toList(history.getAfterContent(), Map.class);
            this.changeContent = "增加sku";
            if (null != afterList && afterList.size() > 0) {
                for (Map afterMap : afterList) {
                    // 变更后 {"skuName":"3寸,白色","marketPrice":null,"supplyPrice":9960000.0,"weight":0.2,"photographPrice":9990000.0,"showStatus":2.0,"availableNum":222.0,"goodsCode":"87","skuId":"20180119173349110091"}
                    Long supplyPriceLong = null != afterMap.get("supplyPrice") ? new BigDecimal(String.valueOf(afterMap.get("supplyPrice"))).longValue() : null;
                    String supplyPrice = null != supplyPriceLong ? Utils.moneyFormatCN(supplyPriceLong) : null;

                    Long marketPriceLong = null != afterMap.get("marketPrice") ? new BigDecimal(String.valueOf(afterMap.get("marketPrice"))).longValue() : null;
                    String marketPrice = null != marketPriceLong ? Utils.moneyFormatCN(marketPriceLong) : null;

                    Long photographPriceLong = null != afterMap.get("photographPrice") ? new BigDecimal(String.valueOf(afterMap.get("photographPrice"))).longValue() : null;
                    String photographPrice = null != photographPriceLong ? Utils.moneyFormatCN(photographPriceLong) : null;

                    afterMap.put("photographPrice", photographPrice);
                    afterMap.put("supplyPrice", supplyPrice);
                    afterMap.put("marketPrice", marketPrice);
                    this.addSku.add(afterMap);
                }
            }

        }
    }

    public String getChangeTime() {
        return changeTime;
    }

    public void setChangeTime(String changeTime) {
        this.changeTime = changeTime;
    }

    public Integer getChangeType() {
        return changeType;
    }

    public void setChangeType(Integer changeType) {
        this.changeType = changeType;
    }

    public String getChangeContent() {
        return changeContent;
    }

    public void setChangeContent(String changeContent) {
        this.changeContent = changeContent;
    }

    public String getBeforeContent() {
        return beforeContent;
    }

    public void setBeforeContent(String beforeContent) {
        this.beforeContent = beforeContent;
    }

    public String getAfterContent() {
        return afterContent;
    }

    public void setAfterContent(String afterContent) {
        this.afterContent = afterContent;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }

    public List<Map> getAddSku() {
        return addSku;
    }

    public void setAddSku(List<Map> addSku) {
        this.addSku = addSku;
    }
}
