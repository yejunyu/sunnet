package com.yejunyu.sunnet.concurrentComponent;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * @Author yjy
 * @Description //TODO
 * @Date 2023/2/12
 **/
public class ProducerAndConsumerComponent<T> {

    private final WorkThread<T>[] workThreads;

    private AtomicInteger index;

    private static final Random r = new SecureRandom();

    private static ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);

    private static ExecutorService executorService = Executors.newFixedThreadPool(4);

    public ProducerAndConsumerComponent(int threadNum, int limitSize, int period, int capacity, Processor<T> processor) throws InterruptedException {
        this.workThreads = new WorkThread[threadNum];
        if (threadNum > 1) {
            this.index = new AtomicInteger();
        }
        for (int i = 0; i < threadNum; i++) {
            WorkThread<T> workThread = new WorkThread("workThread" + "_" + i, limitSize, period, capacity, processor);
            this.workThreads[i] = workThread;
            executorService.execute(workThread);
            //开启threadNum个定时任务，每个任务各自检查各个工作线程对象内部的timeout方法，查看前后两次的timeout方法执行周期
            scheduledExecutorService.scheduleAtFixedRate(workThread::timeout, r.nextInt(50), period, TimeUnit.MILLISECONDS);
        }
    }

    public boolean add(T item) {
        int length = this.workThreads.length;
        if (length == 1) {
            return this.workThreads[0].add(item);
        } else {
            // 取模路由到不同的workThread
            int mod = this.index.incrementAndGet() % length;
            return this.workThreads[mod].add(item);
        }
    }

    private static class WorkThread<T> implements Runnable {
        /**
         * 工作线程命名
         */
        private final String threadName;
        /**
         * 队列中允许存放元素个数限制<br>
         * 超出将从队列中取出此大小的元素转成List对象
         */
        private final int queueSizeLimit;
        /**
         * 前后两个任务的执行周期
         */
        private int period;
        /**
         * 记录任务的即时处理时间
         */
        private volatile long lastFlushTime;
        /**
         * 当前工作线程对象
         */
        private volatile Thread currentThread;
        /**
         * 工作线程内部的阻塞队列
         */
        private final BlockingQueue<T> queue;
        /**
         * 回调接口
         */
        private final Processor<T> processor;

        @Override
        public void run() {
            this.currentThread = Thread.currentThread();
            this.currentThread.setName(this.threadName);
            // 当前线程没有被其他线程打断
            while (!this.currentThread.isInterrupted()) {
                //死循环的判断是否满足触发条件(队列实际大小是否超出指定阈值或距离上次任务时间是否超出指定阈值)，如果未满足将阻塞当前线程，避免死循环给系统带来性能开销
                while (!canFlush()) {
                    System.out.println(this.threadName+"线程被阻塞...");
                    LockSupport.park(this);
                }
                // 一旦add方法执行的时候判断存放的阻塞队列元素大小超出阈值或者timeout,就会调用start()里的LockSupport.unpark方法接触阻塞的线程
                // 一旦线程被解除阻塞, 就会触发此方法, 将队列元素转成list对象且调用已经注册的回调函数1
                this.flush();
            }
        }

        public WorkThread(String threadName, int queueSizeLimit, int period, int capacity, Processor<T> processor) {
            this.threadName = threadName;
            this.queueSizeLimit = queueSizeLimit;
            this.period = period;
            this.lastFlushTime = System.currentTimeMillis();
            this.processor = processor;
            this.queue = new ArrayBlockingQueue<>(capacity);
        }

        /**
         * @param item
         * @return
         */
        public boolean add(T item) {
            boolean result = this.queue.offer(item);
            this.checkQueueSize();
            return result;
        }

        private void start() {
            System.out.println(this.threadName+"执行start , 唤醒被阻塞的线程" + currentThread.getName());
            LockSupport.unpark(this.currentThread);
        }

        /**
         * 阻塞队列实际长度超过阈值时, 触发start方法
         */
        public void checkQueueSize() {
            if (this.queue.size() > this.queueSizeLimit) {
                System.out.println(this.threadName + "超出指定阈值,阈值为: " + queueSizeLimit);
                this.start();
            }
        }

        /**
         * 当前时间与上次任务处理时间差是否超过指定阈值, 超过则触发start
         */
        public void timeout() {
            System.out.println("timeout检测 ..." + currentThread.getName());
            if (System.currentTimeMillis() - this.lastFlushTime >= this.period) {
                System.out.println(this.threadName+"当前执行时间已超过阈值 ,上次执行的任务时间为: " + lastFlushTime + " 间隔已超过: " + (System.currentTimeMillis() - lastFlushTime));
                this.start();
            }
        }

        /**
         * 将队列中的元素添加到指定集合
         */
        public void flush() {
            // 记录最新的任务处理时间
            this.lastFlushTime = System.currentTimeMillis();
            if (queue.isEmpty()) {
                System.out.println(this.threadName+"队列为空, 阻塞...");
                return;
            }
            List<T> tempList = new ArrayList<>(this.queueSizeLimit);
            // 将que中的任务转移到temp
            int size = this.queue.drainTo(tempList, this.queueSizeLimit);
            if (size > 0) {
                System.out.println(currentThread.getName() + " 被唤醒, 开始执行任务: 从队列中腾出大小为 " + size + " 的数据且转成list对象");
                try {
                    this.processor.process(tempList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 判断队列实际大小是否超过指定阈值,或距离上次任务执行时间是否超过阈值
         *
         * @return
         */
        private boolean canFlush() {
            return this.queue.size() > this.queueSizeLimit || System.currentTimeMillis() - this.lastFlushTime > this.period;
        }

    }
}
