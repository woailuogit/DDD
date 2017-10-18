package cn.m2c.scm.application.order.query.dto;

import cn.m2c.scm.domain.model.order.GoodsInfo;

/**
 * 商品数据传输对象
 * @author fanjc
 * created date 2017年10月18日
 * copyrighted@m2c
 */
public class GoodsDto {
	/**分类费率*/
	private float rate = 0;
	/**商品id*/
	private String goodsId;
	/**商品名称*/
	private String goodsName;
	/**商品副标题*/
	private String goodsTitle;
	/**商品分类名称*/
	private String goodsType;
	/**商品分类id*/
	private String goodsTypeId;
	/**计量单位*/
	private String goodsUnit;
	/**规格id*/
	private String skuId;
	/**规格名称*/
	private String skuName;
	/**市场价(单价)*/
	private int price;
	/**重量*/
	private float weight = 0;
	/**供货价*/
	private int supplyPrice;
	/**拍获价(单价)*/
	private int discountPrice;
	/**商品小图url*/
	private String goodsIcon;
	
	private String dealerId;
	
	private int freight;
	
	private String marketingId;
	
	/**购买数量*/
	private float purNum = 0;
	
	public int getFreight() {
		return freight;
	}

	public void setFreight(int freight) {
		this.freight = freight;
	}

	public String getMarketingId() {
		return marketingId;
	}

	public void setMarketingId(String marketingId) {
		this.marketingId = marketingId;
	}

	public float getPurNum() {
		return purNum;
	}
	
	public void setPurNum(float num) {
		purNum = num;
	}
	
	public String getDealerId() {
		return dealerId;
	}
	public void setDealerId(String dealerId) {
		this.dealerId = dealerId;
	}
	public float getRate() {
		return rate;
	}
	public void setRate(float rate) {
		this.rate = rate;
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
	public String getGoodsTitle() {
		return goodsTitle;
	}
	public void setGoodsTitle(String goodsTitle) {
		this.goodsTitle = goodsTitle;
	}
	public String getGoodsType() {
		return goodsType;
	}
	public void setGoodsType(String goodsType) {
		this.goodsType = goodsType;
	}
	public String getGoodsTypeId() {
		return goodsTypeId;
	}
	public void setGoodsTypeId(String goodsTypeId) {
		this.goodsTypeId = goodsTypeId;
	}
	public String getGoodsUnit() {
		return goodsUnit;
	}
	public void setGoodsUnit(String goodsUnit) {
		this.goodsUnit = goodsUnit;
	}
	public String getSkuId() {
		return skuId;
	}
	public void setSkuId(String skuId) {
		this.skuId = skuId;
	}
	public String getSkuName() {
		return skuName;
	}
	public void setSkuName(String skuName) {
		this.skuName = skuName;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	public float getWeight() {
		return weight;
	}
	public void setWeight(float weight) {
		this.weight = weight;
	}
	public int getSupplyPrice() {
		return supplyPrice;
	}
	public void setSupplyPrice(int supplyPrice) {
		this.supplyPrice = supplyPrice;
	}
	public int getDiscountPrice() {
		return discountPrice;
	}
	public void setDiscountPrice(int discountPrice) {
		this.discountPrice = discountPrice;
	}
	public String getGoodsIcon() {
		return goodsIcon;
	}
	public void setGoodsIcon(String goodsIcon) {
		this.goodsIcon = goodsIcon;
	}
	/**
	 * 转换成业务层需要的对象
	 * @return
	 */
	public GoodsInfo toGoodsInfo() {
		return new GoodsInfo(rate, goodsId, goodsName, goodsTitle
				,goodsType, goodsTypeId, goodsUnit, skuId
				,skuName, price, supplyPrice, discountPrice, goodsIcon
				,weight, purNum, freight, marketingId);		
	}
}
