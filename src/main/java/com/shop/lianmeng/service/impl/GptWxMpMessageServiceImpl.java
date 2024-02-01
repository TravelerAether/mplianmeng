package com.shop.lianmeng.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.shop.lianmeng.service.WxMpMessageService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @desc: 京东联盟 https://union.jd.com/manager/webMng
 * @author: lqc
 * @date: 2023/12/27 11:42
 */
@Slf4j
@Service
public class GptWxMpMessageServiceImpl implements WxMpMessageService {
    
    @Value("${gpt.url:https://ai.fakeopen.com/api/conversation}")
    private String OFFICIAL_CONVERSATION_URL;
    @Value("${gpt.proxy.hostName}")
    private String proxyHostName;
    @Value("${gpt.proxy.port}")
    private Integer proxyPort;
    
    private String accessToken;
    
    private String parentMessageId;
    
    private String conversationId;
    
    protected X509TrustManager trustManager;
    
    @Override
    public String handle(String content) {
        try {
            HttpRequest post = HttpRequest.post(OFFICIAL_CONVERSATION_URL)
                    .body(JSONUtil.toJsonStr(buildChatGPT(content)))
                    
                    .setReadTimeout(50000).setConnectionTimeout(50000).addHeaders(getChatGPTHeaders())
                    .setHostnameVerifier(getHostNameVerifier()).setSSLSocketFactory(getSslContext().getSocketFactory())
                    .setHttpProxy(proxyHostName,proxyPort);
            
            
            Request request = new Request.Builder()
                    .url(OFFICIAL_CONVERSATION_URL)
                    .headers(Headers.of(getChatGPTHeaders()))
                    .post(RequestBody.create(JSONUtil.toJsonStr(buildChatGPT(content)).getBytes(StandardCharsets.UTF_8),
                            MediaType.parse("application/json")))
                    .build();
            OpenAISettingsState instance = OpenAISettingsState.getInstance();
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(Integer.parseInt(instance.connectionTimeout), TimeUnit.MILLISECONDS)
                    .readTimeout(Integer.parseInt(instance.readTimeout), TimeUnit.MILLISECONDS);
            builder.hostnameVerifier(getHostNameVerifier());
            builder.sslSocketFactory(getSslContext().getSocketFactory(), (X509TrustManager) getTrustAllManager());
            
            
            EventSourceListener listener = new EventSourceListener() {
                
                boolean handler = false;
                
                @Override
                public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
                    LOG.info("ChatGPT: conversation open. Url = {}",eventSource.request().url());
                }
                
                @Override
                public void onClosed(@NotNull EventSource eventSource) {
                    LOG.info("ChatGPT: conversation close. Url = {}",eventSource.request().url());
                    if (!handler) {
                        component.setContent("Connection to remote server failed. There are usually several reasons for this:<br />1. Request too frequently, please try again later.<br />2. It may be necessary to set up a proxy to request.");
                    }
                    mainPanel.aroundRequest(false);
                    component.scrollToBottom();
                    mainPanel.getExecutorService().shutdown();
                }
                
                @Override
                public void onEvent(@NotNull EventSource eventSource, String id,  String type, @NotNull String data) {
                    handler = true;
                    if (StringUtil.isEmpty(data)) {
                        return;
                    }
                    if (data.contains("[DONE]")) {
                        return;
                    }
                    if (mainPanel.isChatGPTModel() && !data.contains("message")) {
                        return;
                    }
                    try {
                        OfficialParser.ParseResult parseResult;
                        if (mainPanel.isChatGPTModel()) {
                            parseResult = OfficialParser.
                                    parseChatGPT(myProject, component, data);
                        } else {
                            parseResult = OfficialParser.
                                    parseGPT35TurboWithStream(component, data);
                        }
                        if (parseResult == null) {
                            return;
                        }
                        if (!mainPanel.isChatGPTModel()) {
                            if (data.contains("\"finish_reason\":\"stop\"")) {
                                mainPanel.getContentPanel().getMessages().add(OfficialBuilder.assistantMessage(gpt35Stack.pop()));
                                gpt35Stack.clear();
                            } else {
                                gpt35Stack.push(parseResult.getSource());
                            }
                        }
                        // Copy action only needed source content
                        component.setSourceContent(parseResult.getSource());
                        component.setContent(parseResult.getHtml());
                    } catch (Exception e) {
                        LOG.error("ChatGPT: Parse response error, e={}, message={}", e, e.getMessage());
                        component.setContent(e.getMessage());
                    } finally {
                        mainPanel.getExecutorService().shutdown();
                    }
                }
                
                @Override
                public void onFailure(@NotNull EventSource eventSource,  Throwable t,  Response response) {
                    if (t != null) {
                        if (t instanceof StreamResetException) {
                            LOG.info("ChatGPT: Request failure, throwable StreamResetException, cause: {}", t.getMessage());
                            component.setContent("Request failure, cause: " + t.getMessage());
                            mainPanel.aroundRequest(false);
                            t.printStackTrace();
                            return;
                        }
                        LOG.info("ChatGPT: conversation failure. Url={}, response={}, errorMessage={}",eventSource.request().url(), response, t.getMessage());
                        component.setContent("Response failure, cause: " + t.getMessage() + ", please try again. <br><br> Tips: if proxy is enabled, please check if the proxy server is working.");
                        mainPanel.aroundRequest(false);
                        t.printStackTrace();
                    } else {
                        String responseString = "";
                        if (response != null) {
                            try {
                                responseString = response.body().string();
                            } catch (IOException e) {
                                mainPanel.aroundRequest(false);
                                LOG.error("ChatGPT: parse response error, cause: {}", e.getMessage());
                                component.setContent("Response failure, cause: " + e.getMessage());
                                throw new RuntimeException(e);
                            }
                        }
                        LOG.info("ChatGPT: conversation failure. Url={}, response={}",eventSource.request().url(), response);
                        component.setContent("Response failure, please try again. Error message: " + responseString);
                    }
                    mainPanel.aroundRequest(false);
                    component.scrollToBottom();
                    mainPanel.getExecutorService().shutdown();
                }
            };
            EventSource.Factory factory = EventSources.createFactory(httpClient);
            return factory.newEventSource(request, listener);
            
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }
        return null;
        
    }
    
    
    public TrustManager getTrustAllManager() {
        if (trustManager != null) {
            return trustManager;
        }
        trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }
            
            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }
            
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
            }
        };
        return trustManager;
    }
    
    private SSLContext getSslContext() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, new TrustManager[] {getTrustAllManager()}, new java.security.SecureRandom());
        return sslContext;
    }
    
    private Map<String, String> getChatGPTHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "text/event-stream");
        headers.put("Authorization", "Bearer " + accessToken);
        headers.put("Content-Type", "application/json");
        headers.put("X-Openai-Assistant-App-Id", "");
        headers.put("Connection", "close");
        headers.put("Accept-Language", "en-US,en;q=0.9");
        headers.put("Referer", "https://chat.openai.com/chat");
        headers.put("User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.1 Safari/605.1.15");
        return headers;
    }
    
    private JsonObject buildChatGPT(String text) {
        JsonObject result = new JsonObject();
        result.addProperty("action", "next");
        
        JsonArray messages = new JsonArray();
        JsonObject message0 = new JsonObject();
        message0.addProperty("id", UUID.randomUUID().toString());
        message0.addProperty("role", "user");
        
        JsonObject content = new JsonObject();
        content.addProperty("content_type", "text");
        JsonArray parts = new JsonArray();
        parts.add(text);
        content.add("parts", parts);
        
        JsonObject author = new JsonObject();
        author.addProperty("role", "user");
        message0.add("content", content);
        message0.add("author", author);
        messages.add(message0);
        result.add("messages", messages);
        
        result.addProperty("parent_message_id", parentMessageId);
        if (StrUtil.isNotEmpty(conversationId)) {
            result.addProperty("conversation_id", conversationId);
        }
        result.addProperty("model", "text-davinci-002-render-sha");
        return result;
    }
    
    private HostnameVerifier getHostNameVerifier() {
        return (hostname, session) -> true;
    }
    

}
