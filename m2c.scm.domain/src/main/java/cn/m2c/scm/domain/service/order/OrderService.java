package cn.m2c.scm.domain.service.order;

import java.util.List;
import java.util.Map;

/***
 * 订单领域服务
 * @author fanjc
 * created date 2017年10月16日
 * copyrighted@m2c
 */
public interface OrderService {
	/***
	 * 判断商品sku库存
	 * @param skus key:sku, val:sl
	 * @return
	 */
	public Map<String, Object> judgeStock(Map<String, Object> skus);
	/***
	 * 锁定商品sku库存
	 * @param skus key:sku, val:sl
	 * @return
	 */
	public void lockStock(Map<String, Object> skus);
	/***
	 * 解锁优惠券
	 * @param couponsIds
	 * @param userId
	 * @return
	 */
	public void unlockCoupons(List<String> couponsIds, String userId);
	/***
	 * 解锁商品sku库存
	 * @param skus key:sku, val:sl
	 * @return
	 */
	public void unlockStock(Map<String, Object> skus);
	/***
	 * 锁定优惠券
	 * @param couponsIds
	 * @return
	 */
	public void lockCoupons(List<String> couponsIds);
	/***
	 * 获取与商品或商家相关的所有的营策略
	 * @param goodses(goodsId, dealerId)
	 * @return
	 */
	public Map<String, Object> getMarketings(Map<String, Object> goodses);
	
	/***
	 * 获取用户购物车下单的商品数据
	 * @param userId
	 * @return
	 */
	public List<Map<String, Object>> getShopCarGoods(String userId);
	/***
	 * 获取商品详情
	 * @param skus key:sku, val:goodsId
	 * @return
	 */
	public List<Map<String, Object>> getGoodsDtl(Map<String, Object> skus);
	/***
	 * 获取营销规则列表
	 * @param goodsId, typeId, dealerId
	 * @return
	 */
	public Map<String, Object> getMarketingsByGoods(Map<String, Object> skus);
	
	/***
	 * 获取营销规则列表通过营销ID
	 * @param marketingIds
	 * @return
	 */
	public Map<String, Object> getMarketingsByIds(List<String> marketingIds);
	
	/***
	 * 获取商品的供货价
	 * @param goods 商品ID, 营销Id
	 * @return
	 */
	public Map<String, Object> getSupplyPriceByIds(List<Map<String, String>> goods);
	
	/***
	 * 获取媒体相关信息
	 * @param resIds
	 * @return
	 */
	public Map<String, Object> getMediaBdByResIds(List<String> resIds);
	
}