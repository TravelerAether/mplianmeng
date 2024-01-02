package com.shop.lianmeng.service.impl;

import com.jd.open.api.sdk.DefaultJdClient;
import com.jd.open.api.sdk.JdClient;
import com.jd.open.api.sdk.domain.kplunion.promotioncommon.PromotionService.request.get.PromotionCodeReq;
import com.jd.open.api.sdk.request.kplunion.UnionOpenPromotionCommonGetRequest;
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

}
