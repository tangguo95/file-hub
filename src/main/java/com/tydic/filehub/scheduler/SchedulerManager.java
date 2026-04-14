package com.tydic.filehub.scheduler;

import com.tydic.filehub.business.DownAndUploadService;
import com.tydic.filehub.dto.CodeFileOper;
import com.tydic.filehub.dto.JobExecutionLog;
import com.tydic.filehub.mapper.uoc.CodeFileOperMapper;
import com.tydic.filehub.service.JobExecutionLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulerManager {

    private final TaskScheduler taskScheduler;
    private final CodeFileOperMapper codeFileOperMapper;
    private final DownAndUploadService downAndUploadService;
    private final JobExecutionLogService jobExecutionLogService;
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final Set<String> runningTasks = ConcurrentHashMap.newKeySet();

    @EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void onApplicationReady() {
        refreshAll();
    }

    public synchronized void refreshAll() {
        List<CodeFileOper> jobs = codeFileOperMapper.selectSchedulableJobs();
        scheduledTasks.keySet().forEach(this::removeJob);
        for (CodeFileOper job : jobs) {
            schedule(job);
        }
    }

    public synchronized void refreshJob(String fileOperCode) {
        removeJob(fileOperCode);
        CodeFileOper job = codeFileOperMapper.selectByFileOperCode(fileOperCode);
        if (isSchedulable(job)) {
            schedule(job);
        }
    }

    public synchronized void removeJob(String fileOperCode) {
        ScheduledFuture<?> existing = scheduledTasks.remove(fileOperCode);
        if (existing != null) {
            existing.cancel(false);
        }
    }

    public void triggerNow(String fileOperCode) {
        execute(fileOperCode, TriggerType.MANUAL);
    }

    private boolean isSchedulable(CodeFileOper job) {
        return job != null
                && job.getState() != null && job.getState() == 1
                && job.getJobEnabled() != null && job.getJobEnabled() == 1
                && StringUtils.hasText(job.getCronExpression());
    }

    private void schedule(CodeFileOper job) {
        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> execute(job.getFileOperCode(), TriggerType.AUTO),
                new CronTrigger(job.getCronExpression())
        );
        scheduledTasks.put(job.getFileOperCode(), future);
    }

    private void execute(String fileOperCode, TriggerType triggerType) {
        if (!runningTasks.add(fileOperCode)) {
            JobExecutionLog rejected = jobExecutionLogService.start(fileOperCode, triggerType);
            jobExecutionLogService.finish(rejected, "REJECTED", "任务正在执行，拒绝重复触发", null);
            return;
        }
        JobExecutionLog executionLog = jobExecutionLogService.start(fileOperCode, triggerType);
        try {
            CodeFileOper update = new CodeFileOper();
            update.setId(codeFileOperMapper.selectByFileOperCode(fileOperCode).getId());
            update.setLastTriggerTime(LocalDateTime.now());
            update.setLastStatus("RUNNING");
            update.setLastMessage("任务执行中");
            codeFileOperMapper.updateByPrimaryKeySelective(update);

            downAndUploadService.executeJob(fileOperCode, triggerType);

            CodeFileOper success = new CodeFileOper();
            success.setId(update.getId());
            success.setLastStatus("SUCCESS");
            success.setLastMessage("任务执行成功");
            success.setLastFinishTime(LocalDateTime.now());
            codeFileOperMapper.updateByPrimaryKeySelective(success);
            jobExecutionLogService.finish(executionLog, "SUCCESS", "任务执行成功", null);
        } catch (Exception ex) {
            log.error("任务执行失败: {}", fileOperCode, ex);
            CodeFileOper failed = new CodeFileOper();
            failed.setId(codeFileOperMapper.selectByFileOperCode(fileOperCode).getId());
            failed.setLastStatus("FAILED");
            failed.setLastMessage(ex.getMessage());
            failed.setLastFinishTime(LocalDateTime.now());
            codeFileOperMapper.updateByPrimaryKeySelective(failed);
            jobExecutionLogService.finish(executionLog, "FAILED", ex.getMessage(), ex.toString());
        } finally {
            runningTasks.remove(fileOperCode);
        }
    }
}
