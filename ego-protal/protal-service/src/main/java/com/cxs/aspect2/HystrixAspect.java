package com.cxs.aspect2;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/3/30 14:59
 * solr雪崩的切面
 */
@Component
@Aspect
public class HystrixAspect {

    private Random RDM = new Random();

    //    @Autowired
    private Hystrix hystrix = new Hystrix();

    @Around("execution(* com.cxs.service.impl.SearchServiceImpl.searchGoods(..))")
    public Object solrHystrix(ProceedingJoinPoint point) {
        System.out.println("进入断路器");
        switch (hystrix.getStatus()) {
            case OPEN:
                return noInvoke(point);
            case CLOSED:
                return doInvoke(point);
            case HALF_OPEN:
                return testInvoke(point);
            default:
                return null;
        }
    }

    /**
     * 测试调用 半开状态
     *
     * @param point
     * @return
     */
    private Object testInvoke(ProceedingJoinPoint point) {
        Object object = null;
        //搞一个3%的概率调用
        if (RDM.nextInt(100) < 3) {
            System.out.println("概率到了 我要调用");
            //就调用一次试试
            try {
                object = point.proceed(point.getArgs());
                //如果成功了 就设置状态为关闭
                System.out.println("测试好了 将断路器关闭");
                hystrix.setStatus(Hystrix.Status.CLOSED);
                synchronized (hystrix.lock) {
                    //当断路器关闭的时候 唤醒线程等待的锁
                    hystrix.lock.notifyAll();
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                //如果进来就说明调用还是失败的
                System.out.println("未通过测试");
            }
        }
        System.out.println("概率没到 不调用");

        return object;
    }

    /**
     * 调用
     *
     * @param point
     * @return
     */
    private Object doInvoke(ProceedingJoinPoint point) {
        Object object = null;
        try {
            object = point.proceed(point.getArgs());
            System.out.println("调用成功");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            System.out.println("调用失败");
            int failCount = hystrix.getAtomicInteger().incrementAndGet();
            if (failCount >= Hystrix.FAIL_COUNT) {
                //当失败次数大于等于极限的时候
                hystrix.setStatus(Hystrix.Status.OPEN);
            }
        }
        return object;
    }

    /**
     * 不调用
     *
     * @param point
     * @return
     */
    private Object noInvoke(ProceedingJoinPoint point) {
        //在一定时间内不调用 让这个时间趋近于0 设置为半开状态
        hystrix.setStatus(Hystrix.Status.HALF_OPEN);
        return null;
    }

}
