package com.yejunyu.sunnet.concurrentComponent;

import java.util.List;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2023/2/12
 **/
public class ProcessorImpl implements Processor<String> {
    @Override
    public void process(List<String> list) {
        System.out.println("回调接口");
    }
}
