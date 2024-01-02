package com.shop.lianmeng.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.jd.open.api.sdk.DefaultJdClient;
import com.jd.open.api.sdk.JdClient;
import com.jd.open.api.sdk.domain.kplunion.GoodsService.request.query.GoodsReq;
import com.jd.open.api.sdk.domain.kplunion.GoodsService.response.query.Coupon;
import com.jd.open.api.sdk.domain.kplunion.GoodsService.response.query.GoodsResp;
import com.jd.open.api.sdk.domain.kplunion.promotioncommon.PromotionService.request.get.PromotionCodeReq;
import com.jd.open.api.sdk.request.kplunion.UnionOpenGoodsQueryRequest;
import com.jd.open.api.sdk.request.kplunion.UnionOpenPromotionCommonGetRequest;
import com.jd.open.api.sdk.response.kplunion.UnionOpenGoodsQueryResponse;
import com.jd.open.api.sdk.response.kplunion.UnionOpenPromotionCommonGetResponse;
import com.shop.lianmeng.config.JdConfig;
import com.shop.lianmeng.service.WxMpMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @desc: 京东联盟 https://union.jd.com/manager/webMng
 * @author: lqc
 * @date: 2023/12/27 11:42
 */
@Slf4j
@Service
public class JdWxMpMessageServiceImpl implements WxMpMessageService {

    @Autowired
    private JdConfig jdConfig;

    @Override
    public String handle(String content) {
        return getClickLink(content);
        
    }
    
    private String getClickLink(String content) {
        JdClient client = new DefaultJdClient(jdConfig.getServerUrl(), jdConfig.getAccessToken(), jdConfig.getAppKey(),
                jdConfig.getAppSecret());
        UnionOpenPromotionCommonGetRequest request = new UnionOpenPromotionCommonGetRequest();
        PromotionCodeReq promotionCodeReq = new PromotionCodeReq();
        promotionCodeReq.setMaterialId(content);
        promotionCodeReq.setSiteId(jdConfig.getSiteId());
        promotionCodeReq.setCommand(1);
        
        request.setPromotionCodeReq(promotionCodeReq);
        
        UnionOpenPromotionCommonGetResponse response = null;
        try {
            response = client.execute(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        log.info(response.getGetResult().getData().getJCommand());
        return response.getGetResult().getData().getClickURL();
    }
    
    /**
     * 获取优惠券url
     *
     * @param keyWord
     * @return
     */
    private String getCouponLink(String keyWord) {
        JdClient client = new DefaultJdClient(jdConfig.getServerUrl(), jdConfig.getAccessToken(), jdConfig.getAppKey(),
                jdConfig.getAppSecret());
        UnionOpenGoodsQueryRequest request = new UnionOpenGoodsQueryRequest();
        GoodsReq goodsReqDTO = new GoodsReq();
        goodsReqDTO.setKeyword(keyWord);
        goodsReqDTO.setPageSize(1);
        goodsReqDTO.setSortName("commission");
        
        request.setGoodsReqDTO(goodsReqDTO);
        
        UnionOpenGoodsQueryResponse response = null;
        try {
            response = client.execute(request);
        } catch (Exception e) {
            log.error("===== 查询京东优惠券异常 =====", e);
        }
        GoodsResp[] data = response.getQueryResult().getData();
        
        if (ObjectUtil.isNotEmpty(data)) {
            Coupon[] couponList = data[0].getCouponInfo().getCouponList();
            if (ObjectUtil.isNotEmpty(couponList)) {
                return couponList[0].getLink();
            }
        }
        
        return null;
    }
    
    
}
