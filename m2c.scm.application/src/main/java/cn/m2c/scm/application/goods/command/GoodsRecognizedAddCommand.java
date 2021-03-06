package cn.m2c.scm.application.goods.command;

import cn.m2c.ddd.common.AssertionConcern;

import java.io.Serializable;

/**
 * 增加商品识别图
 */
public class GoodsRecognizedAddCommand extends AssertionConcern implements Serializable {
    /**
     * 商品id
     */
    private String goodsId;

    /**
     * 识别图片id
     */
    private String recognizedId;

    /**
     * 识别图片url
     */
    private String recognizedUrl;

    public GoodsRecognizedAddCommand(String goodsId, String recognizedId, String recognizedUrl) {
        this.goodsId = goodsId;
        this.recognizedId = recognizedId;
        this.recognizedUrl = recognizedUrl;
    }

    public String getGoodsId() {
        return goodsId;
    }

    public String getRecognizedId() {
        return recognizedId;
    }

    public String getRecognizedUrl() {
        return recognizedUrl;
    }
}
