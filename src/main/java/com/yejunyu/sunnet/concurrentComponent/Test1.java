package com.yejunyu.sunnet.concurrentComponent;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2023/2/12
 **/
public class Test1 {

    /**
     * 队列超出阈值时
     */
    public static void addTest() throws InterruptedException {
        ProducerAndConsumerComponent<String> producerAndConsumerComponent = new ProducerAndConsumerComponent<>(2, 3, 12000, 10, new ProcessorImpl());
//        Thread.sleep(1L);
        for (int i = 0; i < 10; i++) {
            producerAndConsumerComponent.add("1");
        }
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void timeout(){
        try {
            ProducerAndConsumerComponent<String> producerAndConsumerComponent = new ProducerAndConsumerComponent<>(2, 1000, 2000, 10, new ProcessorImpl());
            for (int i = 0; i < 100; i++) {
                producerAndConsumerComponent.add("1");
                TimeUnit.MILLISECONDS.sleep(new Random().nextInt(50));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        addTest();
//        timeout();
    }
}
