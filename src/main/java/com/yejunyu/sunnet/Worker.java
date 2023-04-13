package com.yejunyu.sunnet;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2023/2/2
 **/
public class Worker implements Runnable {

    int id;
    int eachNum;


    @Override
    public void run() {
        while (true) {
            System.out.println(Thread.currentThread().getName());
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public static void main(String[] args) {

    }
}
