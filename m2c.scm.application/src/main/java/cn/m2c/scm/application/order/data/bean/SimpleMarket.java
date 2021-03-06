package cn.m2c.scm.application.order.data.bean;

import cn.m2c.ddd.common.persistence.orm.ColumnAlias;
/***
 * 简单营销实体
 * @author fanjc
 * created date 2017年11月6日
 * copyrighted@m2c
 */
public class SimpleMarket {
	
	@ColumnAlias(value = "marketing_id")
	private String marketingId;
	
	@ColumnAlias(value = "market_level")
	private int marketLevel;
	
	@ColumnAlias(value = "market_type")
	private Integer marketType;//营销形式，1：减钱，2：打折，3：换购
	
	@ColumnAlias(value = "threshold")
	private long threshold;
	
	@ColumnAlias(value = "threshold_type")
	private Integer thresholdType;//1：金额，2：件数
	
	@ColumnAlias(value = "discount")
	private Integer discount;
	
	@ColumnAlias(value = "share_percent")
	private String sharePercent;
	
	@ColumnAlias(value = "market_name")
	private String marketName;
	
	
	@ColumnAlias(value = "_status")
	private Integer useStatus;
	
	
	/**用于退货后是否还处于满足状态*/
	private boolean isFull = true;
	
	public String getMarketName() {
		return marketName;
	}

	public void setMarketName(String marketName) {
		this.marketName = marketName;
	}

	public String getSharePercent() {
		return sharePercent;
	}

	public void setSharePercent(String sharePercent) {
		this.sharePercent = sharePercent;
	}

	public void setIsFull(boolean full) {
		isFull = full;
	}
	
	public boolean isFull() {
		return isFull;
	}
	
	public Integer getDiscount() {
		return discount;
	}
	public void setDiscount(Integer discount) {
		this.discount = discount;
	}
	public String getMarketingId() {
		return marketingId;
	}
	public void setMarketingId(String marketingId) {
		this.marketingId = marketingId;
	}
	public int getMarketLevel() {
		return marketLevel;
	}
	public void setMarketLevel(int marketLevel) {
		this.marketLevel = marketLevel;
	}
	public Integer getMarketType() {
		return marketType;
	}
	public void setMarketType(Integer marketType) {
		this.marketType = marketType;
	}
	public long getThreshold() {
		return threshold;
	}
	public void setThreshold(long threshold) {
		this.threshold = threshold;
	}
	public Integer getThresholdType() {
		return thresholdType;
	}
	public void setThresholdType(Integer thresholdType) {
		this.thresholdType = thresholdType;
	}

	public Integer getUseStatus() {
		return useStatus;
	}

	public void setUseStatus(Integer useStatus) {
		this.useStatus = useStatus;
	}
}
