package cn.m2c.scm.domain.model.order;

import cn.m2c.ddd.common.domain.model.ValueObject;
/***
 * 商品信息值对象
 * @author fanjc
 * created date 2017年10月18日
 * copyrighted@m2c
 */
public class GoodsInfo extends ValueObject {
	/**分类费率*/
	private Float rate;
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
	private Long price;
	/**重量*/
	private Float weight = 0f;
	/**供货价*/
	private Long supplyPrice;
	/**拍获价(单价)*/
	private Long discountPrice;
	/**商品小图url*/
	private String goodsIcon;
	
	private Integer sellNum;
	
	private Long freight;
	/**平台优惠*/
	private Long plateformDiscount;
	
	/**是否为换货商品 1是， 0否*/
	private int isChange = 0;
	/**换货价*/
	private long changePrice;
	
	/**是否需要执行特惠价 1是， 0否*/
	private int isSpecial = 0;
	/**特惠价*/
	private long specialPrice;
	
	/**
     * 优惠券优惠金额
     */
    private Long couponDiscount = 0l;
    /**
     * 优惠券ID
     */
    private String couponId;
	
	public Integer getSellNum() {
		if (sellNum == null)
			sellNum = 0;
		return sellNum;
	}

	public Long getFreight() {
		return freight;
	}
	
	void setFreight(long f) {
		freight = f;
	}

	public GoodsInfo() {
		super();
	}
	
	public GoodsInfo(float rate, String goodsId, String goodsName, String goodsTitle
			,String goodsType, String goodsTypeId, String goodsUnit, String skuId
			,String skuName, long price, long supplyPrice, long discountPrice, String goodsIcon
			,float weight, int sellNum, Long freight, long plateformDiscount
			,int isChange, long changePrice, int isSpecial, long specialPrice
			,String couponId, long couponDiscount) {
		this.rate = rate;
		this.goodsId = goodsId;
		this.goodsName = goodsName;
		this.goodsTitle = goodsTitle;
		this.goodsType = goodsType;
		this.goodsTypeId = goodsTypeId;
		this.goodsUnit = goodsUnit;
		this.skuId = skuId;
		this.skuName = skuName;
		this.price = price;
		this.supplyPrice = supplyPrice;
		this.discountPrice = discountPrice;
		this.goodsIcon = goodsIcon;
		this.weight = weight;
		this.sellNum = sellNum;
		this.freight = freight;
		this.plateformDiscount = plateformDiscount;
		this.isChange = isChange;
		this.changePrice = changePrice;
		this.isSpecial = isSpecial;
		this.specialPrice = specialPrice;
		this.couponDiscount = couponDiscount;
		this.couponId = couponId;
	}
	
	public long getDiscountPrice() {
		return discountPrice;
	}
	
	String getSkuId() {
		return skuId;
	}
	
	long getPlateformDiscount() {
		return plateformDiscount;
	}
	
	void setPlateformDiscount(long discount) {
		plateformDiscount = discount;
	}
	
	long calGoodsAmount() {
		if (isSpecial == 1)
			return specialPrice * sellNum;
		return discountPrice * sellNum;
	}
	
	boolean isChange() {
		return isChange == 1;
	}
	/***
	 * 换购金额
	 * @return
	 */
	long changePrice() {
		return changePrice * sellNum;
	}
	/***
	 * 获取优惠券id
	 * @return
	 */
	String getCouponId() {
		return couponId;
	}
	
	Long getCouponDiscount() {
		if (couponDiscount == null)
			couponDiscount = 0l;
		return couponDiscount;
	}
}
