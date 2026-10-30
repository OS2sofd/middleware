package dk.digitalidentity.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

// this ensures that no single long-running task will block the smaller faster-running tasks
@Configuration
public class SchedulerConfiguration { // implements SchedulingConfigurer {

	// this gave to many issues, so for now we just run a single thread

	/*
    @Bean
    public Executor taskExecutor() {
        return Executors.newScheduledThreadPool(4);
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
    }
    */
}
