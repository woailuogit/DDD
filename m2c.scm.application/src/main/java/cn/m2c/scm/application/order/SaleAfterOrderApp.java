package cn.m2c.scm.application.order;

import cn.m2c.common.JsonUtils;
import cn.m2c.common.MCode;
import cn.m2c.ddd.common.event.annotation.EventListener;
import cn.m2c.ddd.common.logger.OperationLogManager;
import cn.m2c.scm.application.order.command.AddSaleAfterCmd;
import cn.m2c.scm.application.order.command.AproveSaleAfterCmd;
import cn.m2c.scm.application.order.command.SaleAfterCmd;
import cn.m2c.scm.application.order.command.SaleAfterShipCmd;
import cn.m2c.scm.application.order.data.bean.DealerOrderMoneyBean;
import cn.m2c.scm.application.order.data.bean.RefundEvtBean;
import cn.m2c.scm.application.order.data.bean.SimpleCoupon;
import cn.m2c.scm.application.order.data.bean.SimpleMarket;
import cn.m2c.scm.application.order.data.bean.SkuNumBean;
import cn.m2c.scm.application.order.query.AfterSellOrderQuery;
import cn.m2c.scm.application.utils.Utils;
import cn.m2c.scm.domain.NegativeException;
import cn.m2c.scm.domain.model.order.AfterSellFlow;
import cn.m2c.scm.domain.model.order.AfterSellFlowRepository;
import cn.m2c.scm.domain.model.order.DealerOrderDtl;
import cn.m2c.scm.domain.model.order.MainOrder;
import cn.m2c.scm.domain.model.order.OrderRepository;
import cn.m2c.scm.domain.model.order.SaleAfterOrder;
import cn.m2c.scm.domain.model.order.SaleAfterOrderRepository;
import cn.m2c.scm.domain.service.order.OrderService;
import cn.m2c.scm.domain.util.GetDisconfDataGetter;
import com.baidu.disconf.client.usertools.DisconfDataGetter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * 售后应用层服务
 *
 * @author 89776
 *         created date 2017年10月21日
 *         copyrighted@m2c
 */
@Service
public class SaleAfterOrderApp {
    private final static Logger LOGGER = LoggerFactory.getLogger(SaleAfterOrderApp.class);
    @Autowired
    SaleAfterOrderRepository saleAfterRepository;

    @Autowired
    AfterSellOrderQuery saleOrderQuery;

    @Autowired
    AfterSellFlowRepository afterSellFlowRepository;

    @Resource
    private OperationLogManager operationLogManager;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    private static final String SCM_JOB_USER = DisconfDataGetter.getByFileItem("constants.properties", "scm.job.user").toString().trim();


    /***
     * 创建售后单
     *
     * @param cmd
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, NegativeException.class})
    @EventListener(isListening = true)
    public void createSaleAfterOrder(AddSaleAfterCmd cmd) throws NegativeException {
        // 获取订单SKU详情看是否满足售后申请
        DealerOrderDtl itemDtl = saleAfterRepository.getDealerOrderDtlBySku(cmd.getDealerOrderId(),
                cmd.getSkuId(), cmd.getSortNo());

        if (itemDtl == null) {
            throw new NegativeException(MCode.V_1, "申请售后的商品不存在！");
        }

        int _sortNo = itemDtl.getSortNo();
        int ij = saleAfterRepository.getSaleAfterOrderBySkuId(cmd.getDealerOrderId(),
                cmd.getSkuId(), _sortNo);
        if (ij > 0) {
            throw new NegativeException(MCode.V_100, "此商品已有售后还在处理中！");
        }

        if (!itemDtl.canApplySaleAfter()) {
            throw new NegativeException(MCode.V_100, "商品处于不可申请售后状态！");
        }

        int orderType = cmd.getType() == 3 ? 0 : cmd.getType(); //0换货， 1退货，2仅退款                  app传 1退货，2退款，3换货

        // 增加售后限制, 换货除外
        if (orderType != 0) {
            int count = saleAfterRepository.checkCanApply(itemDtl.getOrderId(), itemDtl.getMarketId(), itemDtl.getCouponId());
            if (count > 0) {
                throw new NegativeException(MCode.V_101, "商品处于不可申请售后状态，因参与活动的其他商品正在申请中！");
            }
        }

        int status = 2; //0申请退货,1申请换货,2申请退款          订单类型，0换货， 1退货，2仅退款
        switch (orderType) {
            case 0:
                status = 1;
                break;
            case 1:
                status = 0;
                break;
            case 2:
                status = 2;
                break;
        }
        long money = itemDtl.sumGoodsMoney();
        if (orderType != 0) {
            // 生成售后单保存, 计算售后需要退的钱
            String mkId = itemDtl.getMarketId();
            String couponId = itemDtl.getCouponId();//优惠券id
            //List<SkuNumBean> totalSku = saleOrderQuery.getTotalSku(cmd.getOrderId());//获取满足条件的所有的商品详情
            /*List<SkuNumBean> totalSku = saleOrderQuery.getTotalSkuByMarket(cmd.getOrderId(), mkId, couponId);
            
            long discountMoney = 0;//售后优惠的钱
            long couponDiscountMoney = 0;//优惠券优惠的钱
            if (!StringUtils.isEmpty(mkId)) {//计算售后需要退的钱
                SimpleMarket marketInfo = saleOrderQuery.getMarketById(mkId, cmd.getOrderId());
                List<SkuNumBean> skuBeanLs = saleOrderQuery.getOrderDtlByMarketId(mkId, cmd.getOrderId());
                discountMoney = OrderMarketCalc.calcReturnMoney(marketInfo, skuBeanLs, cmd.getSkuId(), _sortNo);
                //-----将处理好的优惠平摊金额复制给所有订单的列表里面，用于计算优惠券使用
                if (skuBeanLs.size() > 0) {
                    copySku(skuBeanLs, totalSku);
                }
            }
            if (!StringUtils.isEmpty(couponId)) {//计算优惠券的金额
                SimpleCoupon couponInfo = saleOrderQuery.getCouponById(couponId, cmd.getOrderId());
                couponDiscountMoney = OrderCouponCalc.calcReturnMoney(couponInfo, totalSku, cmd.getSkuId(), _sortNo);//计算退款金额
            }
            money = money - discountMoney - couponDiscountMoney;*/
            
            List<SkuNumBean> totalSku1 = saleOrderQuery.getTotalSkuForAfter(cmd.getOrderId());//获取满足条件的所有的商品详情
            StringBuffer couponIdBuffer = new StringBuffer(50);
            List<SkuNumBean> totalSku2 = copyList(totalSku1, couponIdBuffer);
            if (StringUtils.isEmpty(couponId))
            	couponId = couponIdBuffer.toString();
            couponIdBuffer = null;
            
            long beforeMoney = 0;//退前的钱
            long afterMoney = 0;//退后的钱
            //SimpleMarket marketInfo = null;
            SimpleCoupon couponInfo = null;
            List<SimpleMarket> markets = saleOrderQuery.getMarketsByOrderId(cmd.getOrderId());
            /*if (!StringUtils.isEmpty(mkId)) {//计算售后需要退的钱
                marketInfo = saleOrderQuery.getMarketById(mkId, cmd.getOrderId());
                List<SkuNumBean> skuBeanLs = splitByMarketId(mkId, totalSku1);
                AfterOrderMarketCalc.calcReturnMoney1(marketInfo, skuBeanLs);
                //-----将处理好的优惠平摊金额复制给所有订单的列表里面，用于计算优惠券使用
                if (skuBeanLs.size() > 0) {
                    copySku(skuBeanLs, totalSku1);
                }
            }*/
            if (markets != null && markets.size() > 0) {//计算售后需要退的钱
                for(SimpleMarket marketInfo : markets) {
	                List<SkuNumBean> skuBeanLs = splitByMarketId(marketInfo.getMarketingId(), totalSku1);
	                AfterOrderMarketCalc.calcReturnMoney1(marketInfo, skuBeanLs);
	                //-----将处理好的优惠平摊金额复制给所有订单的列表里面，用于计算优惠券使用
	                if (skuBeanLs.size() > 0) {
	                    copySku(skuBeanLs, totalSku1);
	                }
                }
            }
            if (!StringUtils.isEmpty(couponId)) {//计算优惠券的金额
                couponInfo = saleOrderQuery.getCouponById(couponId, cmd.getOrderId());
                AfterOrderMarketCalc.calcCouponReturnMoney1(couponInfo, totalSku1);
            }
            //得到钱的总额
            beforeMoney = getTotalMoney(totalSku1);
            
            /*if (!StringUtils.isEmpty(mkId)) {//计算售后需要退的钱
                List<SkuNumBean> skuBeanLs = splitByMarketId(mkId, totalSku2);
                AfterOrderMarketCalc.calcReturnMoney2(marketInfo, skuBeanLs, cmd.getSkuId(), _sortNo);
                //-----将处理好的优惠平摊金额复制给所有订单的列表里面，用于计算优惠券使用
                if (skuBeanLs.size() > 0) {
                    copySku(skuBeanLs, totalSku2);
                }
            }*/
            
            if (markets != null && markets.size() > 0) {//计算售后需要退的钱
            	for(SimpleMarket marketInfo : markets) {
	                List<SkuNumBean> skuBeanLs = splitByMarketId(marketInfo.getMarketingId(), totalSku2);
	                AfterOrderMarketCalc.calcReturnMoney2(marketInfo, skuBeanLs, cmd.getSkuId(), _sortNo);
	                //-----将处理好的优惠平摊金额复制给所有订单的列表里面，用于计算优惠券使用
	                if (skuBeanLs.size() > 0) {
	                    copySku(skuBeanLs, totalSku2);
	                }
            	}
            }
            if (!StringUtils.isEmpty(couponId)) {//计算优惠券的金额
                AfterOrderMarketCalc.calcCouponReturnMoney2(couponInfo, totalSku2, cmd.getSkuId(), _sortNo);
            }
            afterMoney = getTotalMoney(totalSku2, cmd.getSkuId(), _sortNo);
            
            money = beforeMoney - afterMoney;
        } else {
            money = 0;
        }
        long ft = 0;

        if (money < 0) {
            throw new NegativeException(MCode.V_103, "不符合发起售后条件，建议联系商家");
        }

        int num = cmd.getBackNum();
        if (num > itemDtl.sellNum())
            num = itemDtl.sellNum();

        AfterSellFlow afterSellFlow = new AfterSellFlow();
        // 使之前同一个商品申请的失效 20171218添加
        saleAfterRepository.invalideBefore(cmd.getSkuId(), cmd.getDealerOrderId(), _sortNo);

        SaleAfterOrder afterOrder = new SaleAfterOrder(cmd.getSaleAfterNo(), cmd.getUserId(), cmd.getOrderId(),
                cmd.getDealerOrderId(), cmd.getDealerId(), cmd.getGoodsId(), cmd.getSkuId(), cmd.getReason()
                , num, status, orderType, money, cmd.getReasonCode(), ft
                , _sortNo);
        afterOrder.addApply();
        saleAfterRepository.save(afterOrder);
        afterSellFlow.add(cmd.getSaleAfterNo(), 0, cmd.getUserId(), cmd.getReason(), cmd.getReasonCode(), null, null);
        afterSellFlowRepository.save(afterSellFlow);
        LOGGER.info("新增加售后申请成功！");
    }
    /***
     * 分离出带营销的商品来
     * @param mId
     * @param totalSku
     * @return
     */
    private List<SkuNumBean> splitByMarketId(String mId, List<SkuNumBean> totalSku) {
    	List<SkuNumBean> mkIdSkus = new ArrayList<SkuNumBean>();
    	for (SkuNumBean bean : totalSku) {
			if (bean.getStatus() == 0) {
				bean.setDiscountMoney(0);
			}
			if(mId.equals(bean.getMarketId())){
				mkIdSkus.add(bean);
			}
		}
    	
    	return mkIdSkus;
    }

    /***
     * 申请售后退货或退款提示
     *
     * @param cmd
     */
    public void applySaleAfter(AddSaleAfterCmd cmd) {
        // 获取订单SKU详情看是否满足售后申请
        // 计算需要退的金额
    }

    /***
     * 同意售后申请
     *
     * @param cmd
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, NegativeException.class})
    @EventListener(isListening = true)
    public void agreeApply(AproveSaleAfterCmd cmd, String attach) throws NegativeException {
        SaleAfterOrder order = saleAfterRepository.getSaleAfterOrderByNo(cmd.getSaleAfterNo(), cmd.getDealerId());
        if (order == null) {
            throw new NegativeException(MCode.V_101, "无此售后单！");
        }
        if (!order.canAgree()) {
            throw new NegativeException(MCode.V_101, "此售后单状态不可进行同意操作！");
        }
        long money = 0;
        DealerOrderDtl itemDtl = saleAfterRepository.getDealerOrderDtlBySku(order.dealerOrderId(),
                order.skuId(), order.sortNo());
        if (order.orderType() != 0) {

            //SimpleMarket marketInfo = saleOrderQuery.getMarketBySkuIdAndOrderId(order.skuId(), order.orderId(), order.sortNo());
            List<SimpleMarket> markets = saleOrderQuery.getMarketsByOrderId(order.orderId());
            //SimpleCoupon couponInfo = saleOrderQuery.getCouponBySkuIdAndOrderId(order.skuId(), order.orderId(), order.sortNo());
            /*List<SkuNumBean> totalSku = saleOrderQuery.getTotalSku(order.orderId());//所有的商品详情
            long discountMoney = 0;
            money = itemDtl.sumGoodsMoney();
            if (marketInfo != null) {//计算售后需要退的钱
                List<SkuNumBean> skuBeanLs = saleOrderQuery.getOrderDtlByMarketId(marketInfo.getMarketingId(), order.orderId());
                discountMoney = OrderMarketCalc.calcReturnMoney(marketInfo, skuBeanLs, order.skuId(), order.sortNo());
              //-----将处理好的优惠平摊金额复制给所有订单的列表里面，用于计算优惠券使用
                if (skuBeanLs.size() > 0) {
                    copySku(skuBeanLs, totalSku);
                }
            }

            if (marketInfo != null && !marketInfo.isFull()) {
                // 更新已使用营销 为不可用状态
                saleAfterRepository.disabledOrderMarket(order.orderId(), marketInfo.getMarketingId());
            }
            long couponDiscountMoney = 0;
            if (couponInfo!=null && !StringUtils.isEmpty(couponInfo.getCouponId())) {//计算优惠券的金额
                couponDiscountMoney = OrderCouponCalc.calcReturnMoney(couponInfo, totalSku, order.skuId(), order.sortNo());//计算退款金额
            }
            if (couponInfo != null && !couponInfo.isFull()) {
                // 更新已使用营销 为不可用状态
                saleAfterRepository.disabledOrderCoupon(order.orderId(), couponInfo.getCouponId());
            }
            
            money = money - discountMoney - couponDiscountMoney;*/
            List<SkuNumBean> totalSku1 = saleOrderQuery.getTotalSkuForAfterConfirm(order.orderId(), order.sortNo(), order.skuId());//获取满足条件的所有的商品详情
            StringBuffer couponIdBuffer = new StringBuffer(50);
            List<SkuNumBean> totalSku2 = copyList(totalSku1, couponIdBuffer);
            SimpleCoupon couponInfo = saleOrderQuery.getCouponById(couponIdBuffer.toString(), order.orderId());
            
            long beforeMoney = 0;//退前的钱
            long afterMoney = 0;//退后的钱
            /*if (marketInfo != null) {//计算售后需要退的钱
                List<SkuNumBean> skuBeanLs = splitByMarketId(marketInfo.getMarketingId(), totalSku1);
                AfterOrderMarketCalc.calcReturnMoney1(marketInfo, skuBeanLs);
                //-----将处理好的优惠平摊金额复制给所有订单的列表里面，用于计算优惠券使用
                if (skuBeanLs.size() > 0) {
                    copySku(skuBeanLs, totalSku1);
                }
            }*/
            if (markets != null && markets.size() > 0) {//计算售后需要退的钱
            	for(SimpleMarket marketInfo : markets) {
	                List<SkuNumBean> skuBeanLs = splitByMarketId(marketInfo.getMarketingId(), totalSku1);
	                AfterOrderMarketCalc.calcReturnMoney1(marketInfo, skuBeanLs);
	                //-----将处理好的优惠平摊金额复制给所有订单的列表里面，用于计算优惠券使用
	                if (skuBeanLs.size() > 0) {
	                    copySku(skuBeanLs, totalSku1);
	                }
            	}
            }
            
            if (couponInfo != null) {//计算优惠券的金额
                AfterOrderMarketCalc.calcCouponReturnMoney1(couponInfo, totalSku1);
            }
            //得到钱的总额
            beforeMoney = getTotalMoney(totalSku1);
            
            if (markets != null && markets.size() > 0) {//计算售后需要退的钱
            	for(SimpleMarket marketInfo : markets) {
	                List<SkuNumBean> skuBeanLs = splitByMarketId(marketInfo.getMarketingId(), totalSku2);
	                AfterOrderMarketCalc.calcReturnMoney2(marketInfo, skuBeanLs, order.skuId(), order.sortNo());
	                //-----将处理好的优惠平摊金额复制给所有订单的列表里面，用于计算优惠券使用
	                if (skuBeanLs.size() > 0) {
	                    copySku(skuBeanLs, totalSku2);
	                }
            	}
            }
            if (markets != null && markets.size() > 0) {//计算售后需要退的钱
            	for(SimpleMarket marketInfo : markets) {
		            if (marketInfo != null && !marketInfo.isFull()) {
		                // 更新已使用营销 为不可用状态
		                saleAfterRepository.disabledOrderMarket(order.orderId(), marketInfo.getMarketingId());
		            }
            	}
            }
            
            if (couponInfo != null) {//计算优惠券的金额
                AfterOrderMarketCalc.calcCouponReturnMoney2(couponInfo, totalSku2, order.skuId(), order.sortNo());
            }
            if (couponInfo != null && !couponInfo.isFull()) {
                // 更新已使用营销 为不可用状态
                saleAfterRepository.disabledOrderCoupon(order.orderId(), couponInfo.getCouponId());
            }
            afterMoney = getTotalMoney(totalSku2, order.skuId(), order.sortNo());
            
            money = beforeMoney - afterMoney;
            
            if (money < 0) {
                throw new NegativeException(MCode.V_103, "不符合发起售后条件，建议联系商家");
            }
        }
        if (StringUtils.isNotEmpty(attach))
            operationLogManager.operationLog("同意售后申请", attach, order);
        order.updateBackMoney(money);
        double frt = cmd.getRtFreight();
        if (order.isOnlyRtMoney()) {
            DealerOrderMoneyBean odb = saleOrderQuery.getDealerOrderById(order.dealerOrderId());
            if (odb != null && odb.getStatus() == 1) {
                if (frt * (long) Utils.DIVIDE > odb.getOrderFreight())
                    frt = odb.getOrderFreight();
                else
                    frt = (frt * (long) Utils.DIVIDE);
            } else
                frt = 0;
        }
        if (order.agreeApply(cmd.getUserId(), (long) frt)) {
            itemDtl.returnInventory(cmd.getSaleAfterNo(), order.getBackNum(), order.orderType());
            AfterSellFlow afterSellFlow = new AfterSellFlow();
            saleAfterRepository.updateSaleAfterOrder(order);
            afterSellFlow.add(cmd.getSaleAfterNo(), 4, cmd.getUserId(), null, null, null, null);
            afterSellFlowRepository.save(afterSellFlow);
        } else {
            throw new NegativeException(MCode.V_101, "售后单状态不正确或已经同意过了！");
        }
        // 售后同意推送消息
        MainOrder mOrder = orderRepository.getOrderById(order.orderId());
        Map extraMap = new HashMap<>();
        extraMap.put("afterSellOrderId", cmd.getSaleAfterNo());
        extraMap.put("dealerOrderId", order.dealerOrderId());
        extraMap.put("orderId", order.orderId());
        extraMap.put("optType", 5);
        orderService.msgPush(2, mOrder.userId(), JsonUtils.toStr(extraMap), cmd.getDealerId());
    }

    /**
     * 拒绝售后申请
     *
     * @param cmd
     * @throws NegativeException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, NegativeException.class})
    @EventListener(isListening = true)
    public void rejectApply(AproveSaleAfterCmd cmd, String attach) throws NegativeException {
        SaleAfterOrder order = saleAfterRepository.getSaleAfterOrderByNo(cmd.getSaleAfterNo(), cmd.getDealerId());
        if (order == null) {
            throw new NegativeException(MCode.V_101, "无此售后单！");
        }
        if (StringUtils.isNotEmpty(attach))
            operationLogManager.operationLog("拒绝售后申请", attach, order);
        AfterSellFlow afterSellFlow = new AfterSellFlow();
        order.rejectSute(cmd.getRejectReason(), cmd.getRejectReasonCode(), cmd.getUserId());
        saleAfterRepository.updateSaleAfterOrder(order);
        afterSellFlow.add(cmd.getSaleAfterNo(), 3, cmd.getUserId(), null, null, cmd.getRejectReason(), cmd.getRejectReasonCode());
        afterSellFlowRepository.save(afterSellFlow);

        // 售后审核拒绝推送消息
        MainOrder mOrder = orderRepository.getOrderById(order.orderId());
        Map extraMap = new HashMap<>();
        extraMap.put("afterSellOrderId", cmd.getSaleAfterNo());
        extraMap.put("dealerOrderId", order.dealerOrderId());
        extraMap.put("orderId", order.orderId());
        extraMap.put("optType", 8);
        orderService.msgPush(2, mOrder.userId(), JsonUtils.toStr(extraMap), order.dealerId());
    }

    /***
     * 客户退货发货
     *
     * @param cmd
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, NegativeException.class})
    @EventListener(isListening = true)
    public void userShipGoods(SaleAfterShipCmd cmd) throws NegativeException {
        SaleAfterOrder order = saleAfterRepository.getSaleAfterOrderByNo(cmd.getSaleAfterNo());
        if (order == null || !order.isSame(cmd.getSkuId())) {
            throw new NegativeException(MCode.V_101, "无此售后单！");
        }
        if (!order.clientShip(cmd.getExpressInfo(), cmd.getUserId(), cmd.getExpressCode(), cmd.getExpressNo())) {
            throw new NegativeException(MCode.V_103, "状态不正确，不能进行发货操作！");
        }
        saleAfterRepository.updateSaleAfterOrder(order);
        AfterSellFlow afterSellFlow = new AfterSellFlow();
        afterSellFlow.save(cmd.getSaleAfterNo(), 5, cmd.getUserId(), null, null, null, null, null, null, cmd.getExpressNo(), cmd.getExpressName());
        afterSellFlowRepository.save(afterSellFlow);
    }

    /***
     * 商家换货发货
     *
     * @param cmd
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, NegativeException.class})
    @EventListener(isListening = true)
    public void dealerShipGoods(SaleAfterShipCmd cmd, String attach) throws NegativeException {
        SaleAfterOrder order = saleAfterRepository.getSaleAfterOrderByNo(cmd.getSaleAfterNo());
        if (order == null || !order.isSame(cmd.getSkuId())) {
            throw new NegativeException(MCode.V_101, "无此售后单！");
        }
        if (StringUtils.isNotEmpty(attach))
            operationLogManager.operationLog("商家换货发货", attach, order);

        if (!order.dealerShip(cmd.getSdExpressInfo(), cmd.getUserId(), cmd.getOrderId(), cmd.getShopName(), cmd.getExpressCode(), cmd.getExpressNo())) {
            throw new NegativeException(MCode.V_103, "状态不正确，不能进行发货操作！");
        }
        saleAfterRepository.updateSaleAfterOrder(order);
        AfterSellFlow afterSellFlow = new AfterSellFlow();
        afterSellFlow.save(cmd.getSaleAfterNo(), 7, cmd.getUserId(), null, null, null, null, cmd.getExpressNo(), cmd.getExpressName(), null, null);
        afterSellFlowRepository.save(afterSellFlow);

        // 售后发货推送消息
        MainOrder mOrder = orderRepository.getOrderById(order.orderId());
        Map extraMap = new HashMap<>();
        extraMap.put("afterSellOrderId", cmd.getSaleAfterNo());
        extraMap.put("dealerOrderId", order.dealerOrderId());
        extraMap.put("orderId", order.orderId());
        extraMap.put("optType", 6);
        orderService.msgPush(2, mOrder.userId(), JsonUtils.toStr(extraMap), order.dealerId());
    }


    /***
     * 确认退款
     *
     * @param cmd
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, NegativeException.class})
    @EventListener(isListening = true)
    public void approveReturnMoney(SaleAfterCmd cmd) throws NegativeException {
        SaleAfterOrder order = saleAfterRepository.getSaleAfterOrderByNo(cmd.getSaleAfterNo());
        if (order == null || !order.isSame(cmd.getSkuId())) {
            throw new NegativeException(MCode.V_101, "无此售后单！");
        }
        //operationLogManager.operationLog("商家换货发货", attach, order);
        if (!order.confirmBackMoney(cmd.getUserId())) {
            throw new NegativeException(MCode.V_103, "状态不正确，不能进行发货操作！");
        }
        saleAfterRepository.updateSaleAfterOrder(order);
     //   AfterSellFlow afterSellFlow = new AfterSellFlow();
     //   afterSellFlow.add(cmd.getSaleAfterNo(), 10, cmd.getUserId(), null, null, null, null);
     //   afterSellFlowRepository.save(afterSellFlow);
    }

    /***
     * 退款成功
     *
     * @param cmd
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, NegativeException.class})
    @EventListener(isListening = true)
    public void refundSuccess(RefundEvtBean bean) throws NegativeException {
        LOGGER.info("===fanjc==afterSellOrderId==" + bean.getAfterSellOrderId());
        SaleAfterOrder order = saleAfterRepository.getSaleAfterOrderByNo(bean.getAfterSellOrderId());
        if (order == null) {
            throw new NegativeException(MCode.V_101, "无此售后单！");
        }
        Date d = new Date(bean.getRefundTime());
        if (!order.updateRefound(bean.getOrderRefundId(), d)) {
            throw new NegativeException(MCode.V_103, "状态不正确，不能进行退款操作！");
        }
        saleAfterRepository.updateSaleAfterOrder(order);
    }

    /***
     * 退款失败
     *
     * @param cmd
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, NegativeException.class})
    @EventListener(isListening = true)
    public void refundFailed(String afterSaleNo, String userId) throws NegativeException {
        LOGGER.info("===fanjc==afterSellOrderId==" + afterSaleNo);
        SaleAfterOrder order = saleAfterRepository.getSaleAfterOrderByNo(afterSaleNo);

        if (order == null) {
            throw new NegativeException(MCode.V_101, "无此售后单！");
        }

        if (!order.refundFailed(afterSaleNo, userId)) {
            throw new NegativeException(MCode.V_103, "状态不正确，不能进行此操作！");
        }
        saleAfterRepository.updateSaleAfterOrder(order);
    }

    public void scanDtlGoods(RefundEvtBean bean) throws NegativeException {
        LOGGER.info("===fanjc==afterSellOrderId==" + bean.getAfterSellOrderId());
        //检查本单的完成状态
        saleAfterRepository.scanDtlGoods(bean.getAfterSellOrderId());
    }

    /***
     * 同意退款
     *
     * @param cmd
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, NegativeException.class})
    @EventListener(isListening = true)
    public void agreeBackMoney(SaleAfterCmd cmd, String attach) throws NegativeException {
        SaleAfterOrder order = saleAfterRepository.getSaleAfterOrderByNo(cmd.getSaleAfterNo());
        if (order == null || !order.isSame(cmd.getSkuId())) {
            throw new NegativeException(MCode.V_101, "无此售后单！");
        }
        String payNo = saleOrderQuery.getMainOrderPayNo(order.orderId());
        if (StringUtils.isEmpty(payNo)) {
            throw new NegativeException(MCode.V_101, "订单未来支付！");
        }
        if (StringUtils.isNotEmpty(attach))
            operationLogManager.operationLog("商家同意退款", attach, order);
        if (!order.agreeBackMoney(cmd.getUserId(), payNo)) {
            throw new NegativeException(MCode.V_103, "状态不正确，不能进行此操作！");
        }
        saleAfterRepository.updateSaleAfterOrder(order);
        AfterSellFlow afterSellFlow = new AfterSellFlow();
        afterSellFlow.add(cmd.getSaleAfterNo(), 9, cmd.getUserId(), null, null, null, null);
        System.out.println(afterSellFlow);
        afterSellFlowRepository.save(afterSellFlow);

        // 售后确认退款推送消息
        Map extraMap = new HashMap<>();
        MainOrder mOrder = orderRepository.getOrderById(order.orderId());
        extraMap.put("afterSellOrderId", cmd.getSaleAfterNo());
        extraMap.put("dealerOrderId", order.dealerOrderId());
        extraMap.put("orderId", order.orderId());
        extraMap.put("optType", 7);
        orderService.msgPush(2, mOrder.userId(), JsonUtils.toStr(extraMap), order.dealerId());
    }

    /***
     * 用户确认收货
     *
     * @param cmd
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, NegativeException.class})
    @EventListener(isListening = true)
    public void userConfirmRev(SaleAfterCmd cmd) throws NegativeException {
        SaleAfterOrder order = saleAfterRepository.getSaleAfterOrderByNo(cmd.getSaleAfterNo());
        if (order == null || !order.isSame(cmd.getSkuId())) {
            throw new NegativeException(MCode.V_101, "无此售后单！");
        }
        if (!order.userConfirmRev(cmd.getUserId())) {
            throw new NegativeException(MCode.V_103, "状态不正确，不能进行收货操作！");
        }
        saleAfterRepository.updateSaleAfterOrder(order);
        AfterSellFlow afterSellFlow = new AfterSellFlow();
        afterSellFlow.add(cmd.getSaleAfterNo(), 8, cmd.getUserId(), null, null, null, null);
        afterSellFlowRepository.save(afterSellFlow);
    }

    /***
     * 商家确认收货
     *
     * @param cmd
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, NegativeException.class})
    @EventListener(isListening = true)
    public void dealerConfirmRev(SaleAfterCmd cmd, String attach) throws NegativeException {
        SaleAfterOrder order = saleAfterRepository.getSaleAfterOrderByNo(cmd.getSaleAfterNo());
        if (order == null || !order.isSame(cmd.getSkuId())) {
            throw new NegativeException(MCode.V_101, "无此售后单！");
        }
        if (StringUtils.isNotEmpty(attach))
            operationLogManager.operationLog("商家确认收货", attach, order);
        if (!order.dealerConfirmRev(cmd.getUserId())) {
            throw new NegativeException(MCode.V_103, "状态不正确，不能进行收货操作！");
        }
        saleAfterRepository.updateSaleAfterOrder(order);
    }

    /**
     * 商家同意退款或是换货商家已发出态下7天变更为交易完成
     *
     * @throws NegativeException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, NegativeException.class})
    public void updataStatusAgreeAfterSale() throws NegativeException {
        int hour = 168;
        try {
            hour = Integer.parseInt(GetDisconfDataGetter.getDisconfProperty("sale.after.dealer.agree"));
            if (hour < 1)
                hour = 1;
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<SaleAfterOrder> saleAfterOrders = saleAfterRepository.getSaleAfterOrderStatusAgree(hour);
        AfterSellFlow afterSellFlow = new AfterSellFlow();
        if (saleAfterOrders.size() == 0)
            throw new NegativeException(MCode.V_1, "没有满足条件的商家订单.");

        for (SaleAfterOrder afterOrder : saleAfterOrders) {
            jobUpdateSaleAfter(afterOrder, afterSellFlow);
        }
    }

    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, NegativeException.class}, propagation = Propagation.REQUIRES_NEW)
    private void jobUpdateSaleAfter(SaleAfterOrder afterOrder, AfterSellFlow afterSellFlow) {
        afterOrder.updateStatusAgreeAfterSale();
        saleAfterRepository.save(afterOrder);
        afterSellFlow.add(afterOrder.getSaleAfterNo(), 11, SCM_JOB_USER, null, null, null, null);
        afterSellFlowRepository.save(afterSellFlow);
    }

    /**
     * 申请的售后3天未来同意，则需要取消
     *
     * @throws NegativeException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, NegativeException.class})
    public void cancelApply(String userId) throws NegativeException {
        int hour = 72;
        try {
            hour = Integer.parseInt(GetDisconfDataGetter.getDisconfProperty("sale.after.cancel.apply"));
            if (hour < 1)
                hour = 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<SaleAfterOrder> saleAfterOrders = saleAfterRepository.getSaleAfterApplyed(hour);
        AfterSellFlow afterSellFlow = new AfterSellFlow();
        if (saleAfterOrders.size() == 0)
            return;

        for (SaleAfterOrder afterOrder : saleAfterOrders) {
            jobCancelAfterOrder(afterOrder, afterSellFlow);
        }
    }

    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, NegativeException.class}, propagation = Propagation.REQUIRES_NEW)
    private void jobCancelAfterOrder(SaleAfterOrder afterOrder, AfterSellFlow afterSellFlow) {
        if (afterOrder.cancel()) {
            saleAfterRepository.save(afterOrder);
            afterSellFlow.add(afterOrder.getSaleAfterNo(), -1, SCM_JOB_USER, null, null, null, null);
            afterSellFlowRepository.save(afterSellFlow);
        }

        // 售后商家未处理推送消息
        MainOrder mOrder = orderRepository.getOrderById(afterOrder.orderId());
        Map extraMap = new HashMap<>();
        extraMap.put("afterSellOrderId", afterOrder.getSaleAfterNo());
        extraMap.put("dealerOrderId", afterOrder.dealerOrderId());
        extraMap.put("orderId", afterOrder.orderId());
        extraMap.put("optType", 9);
        orderService.msgPush(2, mOrder.userId(), JsonUtils.toStr(extraMap), afterOrder.dealerId());
    }

    /**
     * 售后申请同意后，七天没有确认退款的则直接退款
     *
     * @throws NegativeException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, NegativeException.class})
    public void afterAgreed(String userId) throws NegativeException {
        int hour = 168;
        try {
            hour = Integer.parseInt(GetDisconfDataGetter.getDisconfProperty("sale.after.apply.agreed"));
            if (hour < 1)
                hour = 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<SaleAfterOrder> saleAfterOrders = saleAfterRepository.getAgreeRtMoney(hour);
        AfterSellFlow afterSellFlow = new AfterSellFlow();
        if (saleAfterOrders.size() == 0)
            return;
        try {
            for (SaleAfterOrder afterOrder : saleAfterOrders) {
                jobAfterAgreed(afterOrder, userId, afterSellFlow);
            }
        } catch (NegativeException e) {
            e.printStackTrace();
        }
    }

    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, NegativeException.class}, propagation = Propagation.REQUIRES_NEW)
    private void jobAfterAgreed(SaleAfterOrder afterOrder, String userId, AfterSellFlow afterSellFlow) throws NegativeException {
        String payNo = saleOrderQuery.getMainOrderPayNo(afterOrder.orderId());
        if (StringUtils.isEmpty(payNo)) {
            throw new NegativeException(MCode.V_101, "售后单状态不正确！");
        }
        if (!afterOrder.agreeBackMoney(userId, payNo)) {
            throw new NegativeException(MCode.V_103, "状态不正确，不能进行此操作！");
        }

        saleAfterRepository.save(afterOrder);
        afterSellFlow.add(afterOrder.getSaleAfterNo(), 9, SCM_JOB_USER, null, null, null, null);
        afterSellFlowRepository.save(afterSellFlow);
    }

    /**
     * 当商家同意售后， 退货类型且用户发货， 过七天需要自动收货商家
     *
     * @throws NegativeException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, NegativeException.class})
    public void dealerAutoRec(String userId) throws NegativeException {
        int hour = 168;
        try {
            hour = Integer.parseInt(GetDisconfDataGetter.getDisconfProperty("sale.after.dealer.autoRec"));
            if (hour < 1)
                hour = 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<SaleAfterOrder> saleAfterOrders = saleAfterRepository.getUserSend(hour);
        if (saleAfterOrders.size() == 0)
            return;
        try {
            for (SaleAfterOrder afterOrder : saleAfterOrders) {
                jobDealerAutoRec(afterOrder, userId);
            }
        } catch (NegativeException e) {
            e.printStackTrace();
        }
    }

    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, NegativeException.class}, propagation = Propagation.REQUIRES_NEW)
    private void jobDealerAutoRec(SaleAfterOrder afterOrder, String userId) throws NegativeException {

        if (!afterOrder.dealerConfirmRev(userId)) {
            throw new NegativeException(MCode.V_103, "状态不正确，不能进行此操作！");
        }

        saleAfterRepository.save(afterOrder);
    }

    /**
     * 当商家同意售后， 换货类型且商家发货， 过七天需要用户自动收货
     *
     * @throws NegativeException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, NegativeException.class})
    public void userAutoRec(String userId) throws NegativeException {
        int hour = 168;
        try {
            hour = Integer.parseInt(GetDisconfDataGetter.getDisconfProperty("sale.after.user.autoRec"));
            if (hour < 1)
                hour = 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<SaleAfterOrder> saleAfterOrders = saleAfterRepository.getDealerSend(hour);
        AfterSellFlow afterSellFlow = new AfterSellFlow();
        if (saleAfterOrders.size() == 0)
            return;
        try {
            for (SaleAfterOrder afterOrder : saleAfterOrders) {
                jobUserAutoRec(afterOrder, userId, afterSellFlow);
            }
        } catch (NegativeException e) {
            e.printStackTrace();
        }
    }

    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, NegativeException.class}, propagation = Propagation.REQUIRES_NEW)
    private void jobUserAutoRec(SaleAfterOrder afterOrder, String userId, AfterSellFlow afterSellFlow) throws NegativeException {

        if (!afterOrder.userConfirmRev(userId)) {
            throw new NegativeException(MCode.V_103, "状态不正确，不能进行此操作！");
        }
        // 查询出订单详情中的对应记录 标记为完成状态
        // adfsaf;
        saleAfterRepository.save(afterOrder);
        afterSellFlow.add(afterOrder.getSaleAfterNo(), 8, SCM_JOB_USER, null, null, null, null);
    }

    /**
     * 当售后单完成其对应的商品也应该是完成状态。
     *
     * @throws NegativeException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, NegativeException.class})
    public void afterSaleCompleteUpdated(String userId) throws NegativeException {
        int hour = 168;
        try {
            hour = Integer.parseInt(GetDisconfDataGetter.getDisconfProperty("sale.after.complete.upstream"));
            if (hour < 1)
                hour = 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Long> list = saleAfterRepository.getSpecifiedDtlGoods(hour);
        LOGGER.info("=fanjc=处于完成售后的条数为==" + (list == null ? 0 : list.size()));
        return;
    }

    /**
     * 当售后单完成其对应的商品也应该是完成状态。
     *
     * @throws NegativeException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, NegativeException.class})
    public void scanOrderDtlUpdated(String userId, String saleOrderId) throws NegativeException {
        saleAfterRepository.scanDtlGoods(saleOrderId);
        return;
    }

    /**
     * 将处理的skubean赋值数据
     *
     * @param skuBeanLs
     * @param totalSku
     */
    private void copySku(List<SkuNumBean> skuBeanLs, List<SkuNumBean> totalSku) {
        for (int i = 0; i < skuBeanLs.size(); i++) {
            for (int j = 0; j < totalSku.size(); j++) {
                if (skuBeanLs.get(i).getSkuId().equals(totalSku.get(j).getSkuId())) {
                    totalSku.get(j).setDiscountMoney(skuBeanLs.get(i).getDiscountMoney());
                }
            }
        }
    }
    /***
     * 不包括运费的优惠后的金额
     * @param totalSku
     * @return
     */
    private long getTotalMoney(List<SkuNumBean> totalSku) {
    	long totalMoney = 0;
    	if (null == totalSku || totalSku.size() < 1)
    		return totalMoney;
    	for (SkuNumBean bean : totalSku) {
    		totalMoney += (bean.getGoodsAmount() - bean.getDiscountMoney() - bean.getCouponMoney());
    	}
    	
    	return totalMoney;
    }
    
    /***
     * 不包括运费的优惠后的金额
     * @param totalSku
     * @return
     */
    private long getTotalMoney(List<SkuNumBean> totalSku, String skuId, int sortNo) {
    	long totalMoney = 0;
    	if (null == totalSku || totalSku.size() < 1)
    		return totalMoney;
    	for (SkuNumBean bean : totalSku) {
    		if (skuId.equals(bean.getSkuId()) && sortNo == bean.getSortNo()) {
    			continue;
    		}
    		totalMoney += (bean.getGoodsAmount() - bean.getDiscountMoney() - bean.getCouponMoney());
    	}
    	
    	return totalMoney;
    }
    /***
     * 拷贝列表
     * @param totalSku
     * @return
     */
    private List<SkuNumBean> copyList(List<SkuNumBean> totalSku, StringBuffer sb) {
    	List<SkuNumBean> skus = null;
    	if (totalSku == null || totalSku.size() < 1) {
    		return skus;
    	}
    	skus = new ArrayList<SkuNumBean>();
    	for (SkuNumBean bean : totalSku) {
    		if (sb != null && StringUtils.isNotEmpty(bean.getCouponId()) && sb.length() < 1) {
    			sb.append(bean.getCouponId());
    		}
    		skus.add(bean.clone());
    	}
    	return skus;
    }
    
    /**
     * 检查退款失败或超过某个时间段还没有退的，重新发事件退款
     *
     * @throws NegativeException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, NegativeException.class})
    @EventListener(isListening = true)
    public void checkReturnMoneyFail(String userId) throws NegativeException {
        int minute = 30;
        try {
        	minute = Integer.parseInt(GetDisconfDataGetter.getDisconfProperty("sale.after.user.return.money.check"));
            if (minute < 1)
            	minute = 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<SaleAfterOrder> saleAfterOrders = saleAfterRepository.getReturnMoneyOrder(minute);
        if (saleAfterOrders.size() == 0)
            return;
        try {
            for (SaleAfterOrder afterOrder : saleAfterOrders) {
            	jobCheckReturnMoney(afterOrder, userId);
            }
        } catch (NegativeException e) {
            e.printStackTrace();
        }
    }
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, NegativeException.class}, propagation = Propagation.REQUIRES_NEW)
    private void jobCheckReturnMoney(SaleAfterOrder order, String userId) throws NegativeException {
    	
    	String payNo = saleOrderQuery.getMainOrderPayNo(order.orderId());
        if (StringUtils.isEmpty(payNo)) {
            throw new NegativeException(MCode.V_101, "订单未来支付！");
        }
        
        if (!order.checkAgreeBackMoney(userId, payNo)) {
            throw new NegativeException(MCode.V_103, "状态不正确，不能进行此操作！");
        }
        saleAfterRepository.updateSaleAfterOrder(order);
    }
}
