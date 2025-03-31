package com.adanxing.ad.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class SpringAsyncConfig {

    @Bean("deviceThreadExecutor")
    public AsyncTaskExecutor deviceThreadExecutor() {
        ThreadPoolTaskExecutor mediaThreadExecutor = new ThreadPoolTaskExecutor();
        mediaThreadExecutor.setMaxPoolSize(32);
        mediaThreadExecutor.setCorePoolSize(16);
        mediaThreadExecutor.setQueueCapacity(25600);
        mediaThreadExecutor.setKeepAliveSeconds(5);
        mediaThreadExecutor.setThreadNamePrefix("device-thread-pool-");
        mediaThreadExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 线程池对拒绝任务的处理策略：此处采用了CallerRunsPolicy策略，当线程池没有处理能力的时候，该策略会直接在execute方法的调用线程中运行被拒绝的任务；如果执行程序已被关闭，则会丢弃该任务
        mediaThreadExecutor.setAwaitTerminationSeconds(5); // 设置线程池中任务的等待时间，如果超过这个时候还没有销毁就强制销毁，以确保应用最后能够被关闭，而不是阻塞住
        mediaThreadExecutor.setWaitForTasksToCompleteOnShutdown(true); // 设置线程池关闭的时候等待所有任务都完成再继续销毁其他的Bean
        mediaThreadExecutor.initialize();
        return mediaThreadExecutor;
    }
}