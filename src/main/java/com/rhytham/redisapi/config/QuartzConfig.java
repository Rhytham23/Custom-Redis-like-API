package com.rhytham.redisapi.config;

import com.rhytham.redisapi.jobs.CleanupExpiredKeysJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail jobDetail(){
        return JobBuilder.newJob(CleanupExpiredKeysJob.class)
                .withIdentity("expiredKeysCleanup")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger jobTrigger(){
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(10)
                .repeatForever();

        return TriggerBuilder.newTrigger()
                .forJob(jobDetail())
                .withIdentity("expiredKeyCleanupTrigger")
                .withSchedule(scheduleBuilder)
                .build();
    }
}
