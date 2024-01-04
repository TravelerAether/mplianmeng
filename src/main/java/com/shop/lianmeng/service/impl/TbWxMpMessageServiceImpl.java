package com.shop.lianmeng.service.impl;

import com.shop.lianmeng.config.TbConfig;
import com.shop.lianmeng.service.WxMpMessageService;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.TbkDgMaterialOptionalRequest;
import com.taobao.api.response.TbkDgMaterialOptionalResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @desc: 这是一个简单的类
 * @author: lqc
 * @date: 2024/1/3 18:00
 */
@Slf4j
@Service
public class TbWxMpMessageServiceImpl implements WxMpMessageService {
    
    @Autowired
    private TbConfig tbConfig;
    
    @Override
    public String handle(String content) {
        return null;
    }
    
    /**
     * 转链
     *
     * @param content
     * @return
     */
    private String getClickLink(String content) {
        TaobaoClient client = new DefaultTaobaoClient(tbConfig.getServerUrl(), tbConfig.getAppKey(),
                tbConfig.getAppSecret());

        
        return null;
    }
    
    /**
     * 获取优惠券url
     *
     * @param keyWord
     * @return
     */
    private String getCouponLink(String keyWord) {
        TaobaoClient client = new DefaultTaobaoClient(tbConfig.getServerUrl(), tbConfig.getAppKey(),
                tbConfig.getAppSecret());
        TbkDgMaterialOptionalRequest req = new TbkDgMaterialOptionalRequest();
        req.setPageSize(1L);
        req.setSort("tk_rate_des");
        req.setHasCoupon(true);
        req.setQ(keyWord);
        req.setBizSceneId("1");
        req.setPromotionType("2");
        TbkDgMaterialOptionalResponse rsp = null;
        try {
            rsp = client.execute(req);
        } catch (ApiException e) {
            log.error("请求淘宝失败 ",e);
        }
        System.out.println(rsp.getBody());
        
        
        return null;
    }
  
}
