package com.yejunyu.sunnet;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2022/11/25
 **/
public class Sunnet {

    int workerNum = 3;
    List<Worker> workers = new ArrayList<>();
    List<Thread> workerThreads = new ArrayList<>();
    /**
     * 服务列表
     */
    Map<Integer, Service> serviceMap = new HashMap<>();
    /**
     * 指明了下次服务的最大id
     * 比如现在有三个service那maxId为4
     */
    int maxId = 0;
    /**
     * 读写锁
     */
    ReentrantReadWriteLock serviceLock;

    /**
     * 全局队列
     */
    Queue<Service> globalQue;

    // 全局队列的锁
    Lock globalLock;


    private Sunnet() {
    }

    public static class Singleton {
        public static Sunnet instance = new Sunnet();
    }

    public static Sunnet getInstance() {
        return Singleton.instance;
    }

    public void start() {
        System.out.println("Hello Sunnet");
        // 初始化锁
        // 服务的锁
        serviceLock = new ReentrantReadWriteLock();
        // 全局队列的锁
        globalLock = new ReentrantLock();
        startWorker();
    }


    private void startWorker() {
        for (int i = 0; i < workerNum; i++) {
            Worker worker = new Worker();
            worker.id = i;
            worker.eachNum = 2 << i;
            System.out.println("start work thread work-" + i);
            Thread thread = new Thread(worker, "work-" + i);
            // 添加到数组
            workers.add(worker);
            workerThreads.add(thread);
            thread.start();
        }
    }

    /**
     * 获取service
     *
     * @param id service id
     * @return service
     */
    Service getService(int id) {
        // 获取服务,加读锁
        Service service = null;
        try {
            if (serviceLock.readLock().tryLock()) {
                service = serviceMap.get(id);
            }
        } finally {
            serviceLock.readLock().unlock();
        }
        return service;
    }

    /**
     * 服务的管理,可以理解为对服务的增删改查
     *
     * @param type 服务类型
     * @return service id
     */
    int newService(String type) {
        Service service = new Service();
        service.type = type;
        // 创建服务加写锁
        try {
            if (serviceLock.writeLock().tryLock()) {
                service.id = maxId++;
                serviceMap.put(service.id, service);
            }
        } finally {
            serviceLock.writeLock().unlock();
        }
        service.onInit();
        return service.id;
    }

    /**
     * 删除服务
     *
     * @param id
     */
    void killService(int id) {
        Service service = getService(id);
        if (service == null) {
            return;
        }
        // 退出前
        service.onExit();
        service.isExiting = true;
        try {
            if (serviceLock.writeLock().tryLock()) {
                serviceMap.remove(id);
            }
        } finally {
            serviceLock.writeLock().unlock();
        }
    }

    /**
     * 发送消息
     *
     * @param toId
     * @param msg
     */
    void send(int toId, BaseMsg msg) {
        Service service = getService(toId);
        if (service == null) {
            System.out.println("service is null");
            return;
        }
        // 消息插入服务消息队列
        service.pushMsg(msg);
        //
    }

    Service popGlobalQue() {
        Service srv = null;
        try {
            if (globalLock.tryLock() && !globalQue.isEmpty()) {
                srv = globalQue.poll();
            }
        } finally {
            globalLock.unlock();
        }
        return srv;
    }

    void pushGlobalQue(Service srv) {
        try {
            if (globalLock.tryLock()) {
                globalQue.add(srv);
            }
        } finally {
            globalLock.unlock();
        }
    }

    // 队列长度
    int globalLen() {
        return globalQue.size();
    }
}
