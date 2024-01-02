package com.shop.lianmeng.service.impl;

import com.shop.lianmeng.config.JdConfig;
import com.shop.lianmeng.service.WxMpMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @desc: 其他消息
 * @author: lqc
 * @date: 2023/12/27 11:42
 */
@Slf4j
@Service
public class OtherWxMpMessageServiceImpl implements WxMpMessageService {

    @Autowired
    private JdConfig jdConfig;

    @Override
    public String handle(String content) {
        return "";
    }

}
