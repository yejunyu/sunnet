package com.yejunyu.sunnet;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2023/2/23
 **/
public class ServiceMsg extends BaseMsg{
    // 消息发送方
    int source;
    // 消息体
    char[] buff;
    // 消息大小
    int size;
}
