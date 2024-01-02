package com.shop.lianmeng.controller;

import com.shop.lianmeng.service.LianMengService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/wx")
public class WechatMpController {

    @Autowired
    private LianMengService lmService;


    /**
     * 响应微信消息，
     * 接口回调
     *
     * @param request
     * @param response
     * @param echostr
     * @param signature
     * @param timestamp
     * @param nonce
     * @throws Exception
     */
    @RequestMapping(value = "/echo", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public void echo(HttpServletRequest request, HttpServletResponse response, String echostr, String signature,
                     String timestamp, String nonce) throws Exception {
        lmService.wxResponse(request, response, echostr, signature, timestamp, nonce);
    }
}
