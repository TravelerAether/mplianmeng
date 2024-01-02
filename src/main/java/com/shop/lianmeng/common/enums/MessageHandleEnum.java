package com.shop.lianmeng.common.enums;

/**
 * @desc: 处理枚举
 * @author: lqc
 * @date: 2023/12/27 14:42
 */
public enum MessageHandleEnum {
    
    JD("jd.com", "jdWxMpMessageServiceImpl"),
    // TB("", "tbWxMpMessageServiceImpl"),
    OTHER("other", "otherWxMpMessageServiceImpl"),
    ;
    
    /**
     * code
     */
    public final String key;
    
    /**
     * 描述（desc）
     */
    public final String value;
    
    MessageHandleEnum(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    /**
     * 根据消息体，查询对应数据
     *
     * @param text url
     * @return
     */
    public static String getValueByKey(String text) {
        for (MessageHandleEnum value : MessageHandleEnum.values()) {
            if (text.contains(value.key)) {
                return value.value;
            }
        }
        return OTHER.value;
    }
    
}
