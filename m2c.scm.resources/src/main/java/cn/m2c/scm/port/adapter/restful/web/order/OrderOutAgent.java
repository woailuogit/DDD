package cn.m2c.scm.port.adapter.restful.web.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.m2c.common.MCode;
import cn.m2c.common.MResult;
import cn.m2c.scm.application.order.OrderApplication;
import cn.m2c.scm.application.order.data.representation.OrderMoney;
import cn.m2c.scm.domain.NegativeException;


@RestController
@RequestMapping("/order-out")
public class OrderOutAgent {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(OrderOutAgent.class);
	
	@Autowired
	OrderApplication orderApp;
    
    /**
     * 获取订单需要支付的金额
     */
    @RequestMapping(value="/{orderNo}", method = RequestMethod.GET)
    public ResponseEntity<MResult> getOrderNoMoney(@PathVariable("orderNo") String orderNo
    		,@RequestParam(value="userId", required=false) String userId){
    	MResult result = new MResult(MCode.V_1);
    	try {
    		OrderMoney orderMoney = orderApp.getOrderMoney(orderNo, userId);
    		result.setContent(orderMoney);
    		result.setStatus(MCode.V_200);
		} 
    	catch (NegativeException e) {
    		result.setStatus(e.getStatus());
			result.setContent(e.getMessage());
    	}
    	catch (Exception e) {
			LOGGER.info("获取订单金额失败,e:" + e.getMessage());
			result.setStatus(MCode.V_400);
			result.setContent("获取订单金额失败");
		}
    	return new ResponseEntity<MResult>(result,HttpStatus.OK);
    }
}