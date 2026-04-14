package com.tydic.filehub.service;

import com.tydic.filehub.dto.JobExecutionLog;
import com.tydic.filehub.mapper.uoc.JobExecutionLogMapper;
import com.tydic.filehub.scheduler.TriggerType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobExecutionLogService {

    private final JobExecutionLogMapper jobExecutionLogMapper;

    public JobExecutionLog start(String fileOperCode, TriggerType triggerType) {
        JobExecutionLog log = new JobExecutionLog();
        log.setFileOperCode(fileOperCode);
        log.setTriggerType(triggerType.name());
        log.setStatus("RUNNING");
        log.setSummary("任务开始执行");
        log.setStartTime(LocalDateTime.now());
        log.setCreateTime(LocalDateTime.now());
        jobExecutionLogMapper.insert(log);
        return log;
    }

    public void finish(JobExecutionLog log, String status, String summary, String errorStack) {
        log.setStatus(status);
        log.setSummary(summary);
        log.setErrorStack(errorStack);
        log.setEndTime(LocalDateTime.now());
        jobExecutionLogMapper.updateCompletion(log);
    }

    public List<JobExecutionLog> recentByFileOperCode(String fileOperCode, int limit) {
        return jobExecutionLogMapper.selectRecentByFileOperCode(fileOperCode, limit);
    }

    public List<JobExecutionLog> recent(int limit) {
        return jobExecutionLogMapper.selectRecent(limit);
    }
}
