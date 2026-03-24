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
 * 2. 控制并发检测任务数量，避免全库检测把数据库和应用线程池打满
 * 3. 处理异步任务异常
 *
 * 线程池参数：
 * - 核心线程数：2
 * - 最大线程数：2（同时最多 2 个检测任务）
 * - 队列容量：100（任务进入排队，避免挤占请求线程）
 * - 拒绝策略：AbortPolicy（队列满时快速失败，而不是让请求线程同步执行）
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

        // 全库检测是长任务，这里主动限制并发，优先保证系统稳定性。
        executor.setCorePoolSize(2);

        // 核心数和最大线程数保持一致，避免在高峰期突然放大并发。
        executor.setMaxPoolSize(2);

        // 允许检测任务在后台排队，但不让排队失败时回退到请求线程执行。
        executor.setQueueCapacity(100);

        // 线程名称前缀：便于日志追踪
        executor.setThreadNamePrefix("BooklistCheck-");

        // 线程空闲时间：超过核心线程数的线程，空闲60秒后会被销毁
        executor.setKeepAliveSeconds(60);

        // 拒绝策略：队列满时快速拒绝，由上层统一回写任务失败原因。
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

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
