package com.tydic.filehub.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConfigPullDatasource {
    private Long id;
    private String createdBy;
    private String datasourceCode;
    private String dataIp;
    private String username;
    private String password;
    private String driveName;
    private Integer datasourceType;
    private Integer state;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String remark;
}
