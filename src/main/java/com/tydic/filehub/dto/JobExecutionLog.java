package com.tydic.filehub.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobExecutionLog {
    private Long id;
    private String createdBy;
    private String fileOperCode;
    private String triggerType;
    private String status;
    private String summary;
    private String errorStack;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
}
