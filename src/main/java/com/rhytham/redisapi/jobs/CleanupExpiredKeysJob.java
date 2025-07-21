package com.rhytham.redisapi.jobs;

import com.rhytham.redisapi.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CleanupExpiredKeysJob implements Job {

    private final RedisService redisService;

    @Override
    public void execute(JobExecutionContext context){
        redisService.cleanupExpiredKeys();
    }
}
