package cn.m2c.scm.domain.model.comment;

import cn.m2c.common.JsonUtils;
import cn.m2c.ddd.common.domain.model.ConcurrencySafeEntity;
import cn.m2c.ddd.common.domain.model.DomainEventPublisher;
import cn.m2c.scm.domain.model.comment.event.GoodsCommentAddEvent;
import cn.m2c.scm.domain.model.dealer.event.DealerReportStatisticsEvent;
import cn.m2c.scm.domain.util.DealerReportType;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品评论
 */
public class GoodsComment extends ConcurrencySafeEntity {
    /**
     * 评论编号
     */
    private String commentId;

    /**
     * 商家订单编号
     */
    private String orderId;

    /**
     * 商品id
     */
    private String goodsId;

    /**
     * 规格id
     */
    private String skuId;

    /**
     * 规格名称
     */
    private String skuName;

    /**
     * 商品数量
     */
    private Integer goodsNum;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 供应商编号
     */
    private String dealerId;

    /**
     * 供应商名称
     */
    private String dealerName;

    /**
     * 买家编号
     */
    private String buyerId;

    /**
     * 买家名称
     */
    private String buyerName;

    /**
     * 买家手机号
     */
    private String buyerPhoneNumber;

    /**
     * 买家头像
     */
    private String buyerIcon;

    /**
     * 评论
     */
    private String commentContent;

    /**
     * 评论图片
     */
    private String commentImages;

    /**
     * 评论是否有图片，1:否 2有
     */
    private Integer imageStatus;

    /**
     * 回复,1:否 2：有
     */
    private Integer replyStatus;

    /**
     * 评论级别 1好 2中 3差
     */
    private Integer commentLevel;

    /**
     * 星级 1 2 3 4 5 评价星级，1-5星，好评为5星，中评为2-4星，差评为1星
     */
    private Integer starLevel;

    /**
     * 状态 1正常  2 删除
     */
    private Integer commentStatus;

    /**
     * 差评延时24h展示，1否 2是
     */
    private Integer delayedFlag;

    /**
     * 回评
     */
    private GoodsReplyComment goodsReplyComment;

    private Integer sortNo;

    public GoodsComment() {
        super();
    }

    /**
     * 买家增加评论
     */
    public GoodsComment(String commentId, String orderId, String goodsId, String skuId, String skuName, Integer goodsNum, String goodsName,
                        String dealerId, String dealerName, String buyerId, String buyerName,
                        String buyerPhoneNumber, String buyerIcon, String commentContent, String commentImages, Integer starLevel, Integer sortNo) {
        this.commentId = commentId;
        this.orderId = orderId;
        this.goodsId = goodsId;
        this.skuId = skuId;
        this.skuName = skuName;
        this.goodsNum = goodsNum;
        this.goodsName = goodsName;
        this.dealerId = dealerId;
        this.dealerName = dealerName;
        this.buyerId = buyerId;
        this.buyerName = buyerName;
        this.buyerPhoneNumber = buyerPhoneNumber;
        this.buyerIcon = buyerIcon;
        this.commentContent = commentContent;
        this.commentImages = commentImages;
        this.sortNo = sortNo;

        if (StringUtils.isNotEmpty(commentImages)) {
            List<String> images = JsonUtils.toList(commentImages, String.class);
            if (null != images && images.size() > 0) {
                this.imageStatus = 2;
            }
        }
        // 星级 1 2 3 4 5 评价星级，1-5星，好评为5星，中评为2-4星，差评为1星
        this.starLevel = starLevel;
        if (starLevel == 5) {
            this.commentLevel = 1;  //评论级别 1好 2中 3差
        } else if (starLevel >= 2 && starLevel <= 4) {
            this.commentLevel = 2;
        } else {
            this.commentLevel = 3;
            this.delayedFlag = 2;
        }
        this.replyStatus = 1;
        this.commentStatus = 1;

        DomainEventPublisher
                .instance()
                .publish(new GoodsCommentAddEvent(this.buyerId, this.commentId, this.orderId, this.goodsId, this.skuId));

        Map<String, Map> dealerInfo = new HashMap<>();
        Map infoMap = new HashMap<>();
        infoMap.put("num", 1);
        dealerInfo.put(this.dealerId, infoMap);
        // 数据统计事件
        DomainEventPublisher.instance().publish(new DealerReportStatisticsEvent(dealerInfo, DealerReportType.GOODS_COMMENT, new Date()));
    }

    /**
     * 回评
     *
     * @param replyContent
     */
    public void replyComment(String replyContent) {
        this.replyStatus = 2;
        this.goodsReplyComment = new GoodsReplyComment(this, replyContent);
    }

    /**
     * 删除
     */
    public void remove() {
        this.commentStatus = 2;
    }

    public void over24HBadCommentStatus() {
        this.delayedFlag = 1;
    }

    public String orderId() {
        return orderId;
    }

    public String skuId() {
        return skuId;
    }

    public String goodsId() {
        return goodsId;
    }

    public String dealerId() {
        return dealerId;
    }

    public String buyerId() {
        return buyerId;
    }

    public String goodsName() {
        return goodsName;
    }

    public String commentContent() {
        return commentContent;
    }

    public Integer sortNo() {
        return sortNo;
    }
}