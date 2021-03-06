package cn.m2c.scm.domain.model.order;

import java.util.Date;

import cn.m2c.ddd.common.domain.model.ConcurrencySafeEntity;

/**
 * 批量发货失败内容
 * @author lqwen
 *
 */
public class OrderWrongMessage extends ConcurrencySafeEntity {
	private String dealerOrderId;
	
	private String expressName;
	
	private String expressNo;
	
	private String importWrongMessage;
	
	
	private String expressFlag;
	private Date createdDate;
	
	public OrderWrongMessage() {
		super();
	}

	
	public OrderWrongMessage(String dealerOrderId, String expressName,
			String expressNo, String importWrongMessage, String expressFlag) {
		super();
		this.dealerOrderId = dealerOrderId;
		this.expressName = expressName;
		this.expressNo = expressNo;
		this.importWrongMessage = importWrongMessage;
		this.expressFlag = expressFlag;
	}


	

}
