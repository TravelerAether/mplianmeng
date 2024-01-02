package com.shop.lianmeng.service;

import com.shop.lianmeng.common.enums.MessageHandleEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutNewsMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@Service
@Slf4j
public class LianMengService {

    @Autowired
    private WxMpService wxMpService;

    @Autowired
    Map<String, WxMpMessageService> wxMpMessageServiceMap;


    public void wxResponse(HttpServletRequest request, HttpServletResponse response, String echostr, String signature,
                           String timestamp, String nonce) throws IOException {
        log.info("微信请求：echoStr:{},singture:{},time:{},nonce:{}", echostr, signature, timestamp, nonce);
        PrintWriter out = response.getWriter();
        if (wxMpService.checkSignature(timestamp, nonce, signature)) {
            log.info("微信消息验证成功");
            if (StringUtils.isNotBlank(echostr)) {
                // 微信校验
                out.print(echostr);
                return;
            } else {
                // 加密类型
                String encryptType = StringUtils.isBlank(request.getParameter("encrypt_type")) ? "raw"
                        : request.getParameter("encrypt_type");
                WxMpXmlMessage inMessage = null;
                log.info("消息类型：{}", encryptType);
                if ("raw".equals(encryptType)) {
                    // 明文传输的消息
                    inMessage = WxMpXmlMessage.fromXml(request.getInputStream());
                } else if ("aes".equals(encryptType)) {
                    // 是aes加密的消息
                    String msgSignature = request.getParameter("msg_signature");
                    inMessage = WxMpXmlMessage.fromEncryptedXml(request.getInputStream(),
                            wxMpService.getWxMpConfigStorage(), timestamp, nonce, msgSignature);
                } else {
                    response.getWriter().println("不可识别的加密类型");
                    return;
                }
                if (inMessage != null) {
                    try {
                        String str = message(inMessage);
                        response.getWriter().print(str);
                    } catch (Exception e) {
                        log.error("错误", e);
                    }
                }

            }

        } else {
            log.info("微信消息验证失败");
            response.getWriter().println("校验失败");
        }
        out.close();
        out = null;
    }


    private String message(WxMpXmlMessage inMessage) {


        String content = inMessage.getContent();
        log.info("上下文信息【{}】", content);
        String valueByKey = MessageHandleEnum.getValueByKey(content);
        WxMpMessageService wxMpMessageService = wxMpMessageServiceMap.get(valueByKey);
        String result = wxMpMessageService.handle(content);
        
        WxMpXmlOutNewsMessage wxMpXmlOutNewsMessage = new WxMpXmlOutNewsMessage();
        wxMpXmlOutNewsMessage.setToUserName(inMessage.getFromUser());
        wxMpXmlOutNewsMessage.setFromUserName(inMessage.getToUser());
        wxMpXmlOutNewsMessage.setCreateTime(System.currentTimeMillis() / 1000);
        
        WxMpXmlOutNewsMessage.Item item = new WxMpXmlOutNewsMessage.Item();
        item.setDescription(result);
        wxMpXmlOutNewsMessage.addArticle(item);
        
        
        
        WxMpXmlOutTextMessage wxMpXmlOutTextMessage = new WxMpXmlOutTextMessage();
        wxMpXmlOutTextMessage.setContent(result);
        wxMpXmlOutTextMessage.setToUserName(inMessage.getFromUser());
        wxMpXmlOutTextMessage.setFromUserName(inMessage.getToUser());
        wxMpXmlOutTextMessage.setCreateTime(System.currentTimeMillis() / 1000);
         return wxMpXmlOutTextMessage.toXml();

    }


}
