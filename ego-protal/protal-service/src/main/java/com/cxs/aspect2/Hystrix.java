package com.cxs.aspect2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/3/30 15:02
 */
public class Hystrix {
    /**
     * 默认是正常状态 ，断路器是关闭的
     */
    private Status status = Status.CLOSED;

    /**
     * 锁
     */
    public Object lock = new Object();
    /**
     * Eg：3秒内失败10次就打开断路器
     */
    public static final int MAX_TIME_RANGE = 3000;
    /**
     * 最大的失败次数
     */
    public static final int FAIL_COUNT = 3;
    /**
     * 默认服务调用失败的清零
     */
    private AtomicInteger currentFail = new AtomicInteger(0);
    /**
     * 用一个线程去监控服务的调用结果
     */
    private ExecutorService pool = Executors.newFixedThreadPool(1);

    {
        //提交一个任务
        pool.submit(() -> {
            //3s循环一次
            while (true) {
                //如果断路器是打开或者是半开状态 就不用统计失败次数 好的时候才统计
                if (status == Status.OPEN || status == Status.HALF_OPEN) {
                    synchronized (lock) {
                        lock.wait();
                    }
                }
                //让该线程睡
                Thread.sleep(MAX_TIME_RANGE);
                //清空统计的次数
                currentFail = new AtomicInteger(0);
                System.out.println("测试调用被清空了");
            }
        });
    }

    public enum Status {
        OPEN(),
        CLOSED(),
        HALF_OPEN()
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public AtomicInteger getAtomicInteger() {
        return currentFail;
    }

    public void setAtomicInteger(AtomicInteger currentFail) {
        this.currentFail = currentFail;
    }
}
