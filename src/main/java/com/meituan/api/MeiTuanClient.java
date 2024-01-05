package com.meituan.api;

/**
 * @desc: 这是一个简单的类
 * @author: lqc
 * @date: 2024/1/5 10:08
 */
public interface MeiTuanClient {
    <T extends MeiTuanResponse> T execute(MeiTuanRequest<T> request) ;
    
    <T extends MeiTuanResponse> T execute(MeiTuanRequest<T> request, String session) ;
    
}
