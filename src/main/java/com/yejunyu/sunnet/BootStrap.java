package com.yejunyu.sunnet;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2023/2/23
 **/
public class BootStrap {

    public static void test(){

        String pingType = "ping";

    }

    public static void main(String[] args) throws InterruptedException {
        String pingType = "ping";
        Sunnet sunnet = Sunnet.getInstance();
        sunnet.start();
        int ping1 = sunnet.newService(pingType);
        int ping2 = sunnet.newService(pingType);
        int pong = sunnet.newService(pingType);
        Thread.sleep(100000L);
    }
}
