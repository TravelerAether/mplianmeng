package com.shop.lianmeng.service.impl;

import com.meituan.api.DefaultMeiTuanClient;
import com.shop.lianmeng.config.MtConfig;
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
public class MtWxMpMessageServiceImpl implements WxMpMessageService {
    
    @Autowired
    private MtConfig mtConfig;
    
    private String getSign() {
        return null;
    }
    
    @Override
    public String handle(String content) {
        new DefaultMeiTuanClient(mtConfig.getServerUrl(), mtConfig.getAppKey(), mtConfig.getAppSecret(),
                mtConfig.getSiteId());
    return null;
    }
    
    private String getClickLink(String content) {
        return null;
    }
    
    /**
     * 获取优惠券url
     *
     * @param keyWord
     * @return
     */
    private String getCouponLink(String keyWord) {
        return null;
    }
    
    
}
