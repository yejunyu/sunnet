package com.yejunyu.sunnet;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2023/2/23
 **/
public class Service {
    // 唯一id
    int id;
    // 服务类型
    String type;
    // 是否正在退出
    boolean isExiting;
    // 消息队列和锁
    Queue<BaseMsg> msgQueue;
    Lock queueLock;

    AtomicBoolean inGlobal;

    public Service() {
        this.msgQueue = new PriorityQueue<>();
        this.queueLock = new ReentrantLock();
    }

    void onInit() {
        System.out.println("id: " + id + " onInit");
    }

    void onMsg(BaseMsg msg) {
        System.out.println("id: " + id + " onMsg");

    }

    void onExit() {
        System.out.println("id: " + id + " onExit");

    }

    /**
     * 插入消息
     *
     * @param msg
     */
    void pushMsg(BaseMsg msg) {
        try {
            if (queueLock.tryLock()) {
                msgQueue.add(msg);
            }
        } finally {
            queueLock.unlock();
        }
    }

    /**
     * 执行消息
     *
     * @return
     */
    boolean processMsg() {
        BaseMsg msg = popMsg();
        if (msg != null) {
            onMsg(msg);
            return true;
        }
        return false;
    }

    void processMsgs(int max) {
        for (int i = 0; i < max; i++) {
            boolean success = processMsg();
            if (!success) {
                break;
            }
        }
    }

    /**
     * 取出消息
     *
     * @return
     */
    BaseMsg popMsg() {
        BaseMsg msg = null;
        try {
            if (queueLock.tryLock() && !msgQueue.isEmpty()) {
                msg = msgQueue.poll();
            }
        } finally {
            queueLock.unlock();
        }
        return msg;
    }

    public void setInGlobal(boolean isIn) {
        inGlobal.getAndSet(isIn);
    }
}
