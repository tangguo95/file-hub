package com.tydic.filehub.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CodeFileOper {
    private Long id;
    private String fileOperCode;
    private String jobName;
    private String fileName;
    private String serverIp;
    private Integer serverPort;
    private String serverUserName;
    private String serverPassword;
    private String filePath;
    private Integer operType;
    private Integer downloadDealType;
    private String downloadDataUpdateCondition;
    private String fileUrl;
    private String downloadTbName;
    private String splitLabel;
    private String fileFormat;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer state;
    private String fileNameExtSql;
    private String remark;
    private String querySql;
    private String insertColumnName;
    private String dealRemark;
    private LocalDateTime dealTime;
    private Integer isDelete;
    private Integer dataSource;
    private Integer isUploadOss;
    private String datasourceCode;
    private String fileListShell;
    private Integer fileListShellEnable;
    private String fileOperGroovyBefore;
    private Integer fileOperGroovyBeforeEnable;
    private String fileOperGroovyAfter;
    private Integer fileOperGroovyAfterEnable;
    private Integer stopOnRowError;
    private Integer maxRowErrors;
    private Integer skipHeaderLines;
    private Integer jobEnabled;
    private String cronExpression;
    private String concurrentMode;
    private LocalDateTime lastTriggerTime;
    private LocalDateTime lastFinishTime;
    private String lastStatus;
    private String lastMessage;
}
