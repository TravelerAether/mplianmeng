package com.shop.lianmeng.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @desc:
 * @author: wmw
 * @date: 2023-12-15 18:14:18
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "tb")
public class TbConfig {

    private String appKey;
    private String appSecret;
    private String serverUrl;

}
