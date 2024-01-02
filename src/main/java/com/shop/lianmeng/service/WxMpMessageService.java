package com.shop.lianmeng.service;

import com.jd.open.api.sdk.JdException;

/**
 * @desc: 微信公众号消息处理
 * @author: lqc
 * @date: 2023/12/27 11:39
 */
public interface WxMpMessageService {
    
    /**
     * 消息处理器
     * @param content
     * @return
     */
    String handle(String content) ;
    
    
}
