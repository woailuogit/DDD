package cn.m2c.scm.application.goods.query.data.representation;

import cn.m2c.scm.application.goods.query.data.bean.GoodsBean;

/**
 * 商品信息
 * 商品ID,商品名，商家ID,商家名,识别图ID,识别图URL
 */
public class GoodsInformationRepresentation {
	/**
	 * 商品ID
	 */
	private String goodsId;
	/**
	 * 商品名
	 */
	private String goodsName;
	/**
	 * 商家Id
	 */
	private String dealerId;
	/**
	 * 商家名
	 */
	private String dealerName;
	/**
	 * 识别图ID
	 */
	private String recognizedId;
	/**
	 * 识别图URL
	 */
	private String recognizedUrl;
	/**
	 * 商品投放状态：0：未投放，1：投放
	 */
	private Integer goodsLaunchStatus;
	
	public GoodsInformationRepresentation(GoodsBean bean) {
        this.goodsId = bean.getGoodsId();
        this.goodsName = bean.getGoodsName();
        this.dealerId = bean.getDealerId();
        this.dealerName = bean.getDealerName();
        this.recognizedId = bean.getRecognizedId();
        this.recognizedUrl = bean.getRecognizedUrl();
        this.goodsLaunchStatus = bean.getGoodsLaunchStatus();
    }

	public String getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(String goodsId) {
		this.goodsId = goodsId;
	}

	public String getGoodsName() {
		return goodsName;
	}

	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}

	public String getDealerId() {
		return dealerId;
	}

	public void setDealerId(String dealerId) {
		this.dealerId = dealerId;
	}

	public String getDealerName() {
		return dealerName;
	}

	public void setDealerName(String dealerName) {
		this.dealerName = dealerName;
	}

	public String getRecognizedId() {
		return recognizedId;
	}

	public void setRecognizedId(String recognizedId) {
		this.recognizedId = recognizedId;
	}

	public String getRecognizedUrl() {
		return recognizedUrl;
	}

	public void setRecognizedUrl(String recognizedUrl) {
		this.recognizedUrl = recognizedUrl;
	}

	public Integer getGoodsLaunchStatus() {
		return goodsLaunchStatus;
	}

	public void setGoodsLaunchStatus(Integer goodsLaunchStatus) {
		this.goodsLaunchStatus = goodsLaunchStatus;
	}
	
}