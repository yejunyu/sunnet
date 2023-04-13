package com.yejunyu.sunnet.concurrentComponent;

import java.util.List;

/**
 * @Author yjy
 * @Description //回调接口
 * @Date 2023/2/12
 **/
public interface Processor<T> {
    /**
     * 回调接口
     * @param list list
     */
    void process(List<T> list);
}
