package com.meituan.api;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;

import java.util.Map;
import java.util.TreeMap;

/**
 * @desc: 这是一个简单的类
 * @author: lqc
 * @date: 2024/1/5 10:07
 */
public class DefaultMeiTuanClient implements MeiTuanClient {
    
    private String serverUrl;
    
    private String appKey;
    
    private String appSecret;
    
    private String siteId;
    
    protected String format = "json";
    
    protected int connectTimeout = 15000; // 默认连接超时时间为15秒
    
    protected int readTimeout = 30000; // 默认响应超时时间为30秒
    
    protected boolean needCheckRequest = true; // 是否在客户端校验请求
    
    
    public DefaultMeiTuanClient(String serverUrl, String appKey, String appSecret, String siteId) {
        this.serverUrl = serverUrl;
        this.appKey = appKey;
        this.appSecret = appSecret;
        this.siteId = siteId;
    }
    
    @Override
    public <T extends MeiTuanResponse> T execute(MeiTuanRequest<T> request) {
        return execute(request, null);
    }
    
    @Override
    public <T extends MeiTuanResponse> T execute(MeiTuanRequest<T> request, String session) {
        return _execute(request, session);
    }
    
    private <T extends MeiTuanResponse> T _execute(MeiTuanRequest<T> meiTuanRequest, String session) {
        long start = System.currentTimeMillis();
        String url = this.serverUrl + meiTuanRequest.getApiPath();
        
        // 本地校验请求参数
        if (this.needCheckRequest) {
            meiTuanRequest.check();
        }
        
        Map<String, Object> textParams = meiTuanRequest.getTextParams();
        setSign(textParams);
        
        HttpRequest request = switch (meiTuanRequest.getTopHttpMethod()) {
            case "GET" -> HttpRequest.get(url).form(textParams);
            case "POST" -> HttpRequest.post(url).body(JSONUtil.toJsonStr(textParams));
            default -> throw new RuntimeException("暂不支持");
        };
        
        HttpResponse response = request.setConnectionTimeout(connectTimeout).setReadTimeout(readTimeout)
                .header("Cookie", "JSESSIONID=" + session).execute();
        
        return JSONUtil.toBean(response.body(), meiTuanRequest.getResponseClass());
    }
    
    private void setSign(Map<String, Object> textParams) {
        Map<String, Object> sortedMap = new TreeMap<>(textParams);
        // 按照字典序升序排列参数
        // 构建拼接字符串
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : sortedMap.entrySet()) {
            stringBuilder.append(entry.getKey()).append(entry.getValue());
        }
        
        // 追加密钥
        stringBuilder.append(appSecret);
        
        // 最终的签名字符串
        String signature = stringBuilder.toString();
        SecureUtil.md5(signature);
        textParams.put("sid", signature);
    }
    
    
}
