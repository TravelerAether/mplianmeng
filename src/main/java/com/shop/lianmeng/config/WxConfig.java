package com.shop.lianmeng.config;

import lombok.Data;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.WxMpConfigStorage;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * @desc:
 * @author: wmw
 * @date: 2023-12-15 17:59:51
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wx.mp")
public class WxConfig {

    private String appid;
    private String secret;
    private String token;
    @Bean
    public WxMpService wxMpService(){
        WxMpDefaultConfigImpl wxMpConfigStorage = new WxMpDefaultConfigImpl();
        wxMpConfigStorage.setSecret(secret);
        wxMpConfigStorage.setAppId(appid);
        wxMpConfigStorage.setToken(token);
        WxMpServiceImpl wxMpService =  new WxMpServiceImpl();
        HashMap<String, WxMpConfigStorage> wxMpConfigStorageHashMap = new HashMap<>();
        wxMpConfigStorageHashMap.put(appid,wxMpConfigStorage);
        wxMpService.setMultiConfigStorages(wxMpConfigStorageHashMap);
        return wxMpService;
    }
}
