package com.meituan.api;

import java.util.Map;

/**
 * @desc: 这是一个简单的类
 * @author: lqc
 * @date: 2024/1/5 10:19
 */
public interface MeiTuanRequest<T extends MeiTuanResponse> {
    
    /**
     * 获取路径。
     *
     * @return API名称
     */
    public String getApiPath();
    
    /**
     * 获取Http method，例如GET,POST
     *
     * @return
     */
    public String getTopHttpMethod();
    
    public void setTopHttpMethod(String topHttpMethod);
    
    /**
     * 获取请求时间戳（为空则用系统当前时间）
     */
    public Long getTimestamp();
    
    /**
     * 获取被调用的目标AppKey
     */
    public String getTargetAppKey();
    
    /**
     * 获取具体响应实现类的定义。
     */
    public Class<T> getResponseClass();
    
    /**
     * 获取自定义HTTP请求头参数。
     */
    public Map<String, String> getHeaderMap();
    
    /**
     * 客户端参数检查，减少服务端无效调用。
     */
    public void check();
    
    /**
     * 获取所有的Key-Value形式的文本请求参数集合。其中：
     * <ul>
     * <li>Key: 请求参数名</li>
     * <li>Value: 请求参数值</li>
     * </ul>
     *
     * @return 文本请求参数集合
     */
    public Map<String, Object> getTextParams();
}
