package com.library.management.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置类
 *
 * 功能说明：
 * 1. 配置异步任务线程池
 * 2. 控制并发检测任务数量（最大 5 个）
 * 3. 处理异步任务异常
 *
 * 线程池参数：
 * - 核心线程数：3
 * - 最大线程数：5（同时最多 5 个检测任务）
 * - 队列容量：10（最多排队 10 个任务）
 * - 拒绝策略：CallerRunsPolicy（队列满时由调用线程执行）
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    /**
     * 配置异步任务执行器（线程池）
     *
     * @return 线程池执行器
     */
    @Override
    public Executor getAsyncExecutor() {
        log.info("初始化异步任务线程池");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数：系统启动时创建的线程数
        executor.setCorePoolSize(3);

        // 最大线程数：系统最多可以创建的线程数（控制并发检测任务数量）
        executor.setMaxPoolSize(5);

        // 队列容量：核心线程都在忙时，新任务会进入队列等待
        executor.setQueueCapacity(10);

        // 线程名称前缀：便于日志追踪
        executor.setThreadNamePrefix("BooklistCheck-");

        // 线程空闲时间：超过核心线程数的线程，空闲60秒后会被销毁
        executor.setKeepAliveSeconds(60);

        // 拒绝策略：队列满时，由调用线程执行任务
        // CallerRunsPolicy：不丢弃任务，而是由调用线程自己执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 等待时间：关闭线程池时，最多等待60秒
        executor.setAwaitTerminationSeconds(60);

        // 初始化线程池
        executor.initialize();

        log.info("异步任务线程池初始化完成：核心线程数={}, 最大线程数={}, 队列容量={}",
                executor.getCorePoolSize(),
                executor.getMaxPoolSize(),
                executor.getQueueCapacity());

        return executor;
    }

    /**
     * 异步任务异常处理器
     *
     * @return 异常处理器
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            log.error("异步任务执行异常 - 方法：{}，参数：{}，异常信息：{}",
                    method.getName(),
                    params,
                    throwable.getMessage(),
                    throwable);
        };
    }
}
