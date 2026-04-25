package com.example.customermanagement.config;

import java.util.Map;
import java.util.concurrent.Executor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    private final AppProperties appProperties;

    public AsyncConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Bean(name = "importTaskExecutor")
    public Executor importTaskExecutor() {
        AppProperties.Async asyncProperties = appProperties.getAsync();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(asyncProperties.getCorePoolSize());
        executor.setMaxPoolSize(asyncProperties.getMaxPoolSize());
        executor.setQueueCapacity(asyncProperties.getQueueCapacity());
        executor.setThreadNamePrefix("import-worker-");
        executor.setTaskDecorator(mdcTaskDecorator());
        executor.initialize();
        return executor;
    }

    private TaskDecorator mdcTaskDecorator() {
        return new TaskDecorator() {
            @Override
            public Runnable decorate(final Runnable runnable) {
                // Capture request-scoped logging context before work moves to another thread.
                final Map<String, String> contextMap = MDC.getCopyOfContextMap();
                return new Runnable() {
                    @Override
                    public void run() {
                        if (contextMap != null) {
                            MDC.setContextMap(contextMap);
                        }
                        try {
                            runnable.run();
                        } finally {
                            // Worker threads are reused, so always clear context after each task.
                            MDC.clear();
                        }
                    }
                };
            }
        };
    }
}
