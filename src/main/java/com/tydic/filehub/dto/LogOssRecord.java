package com.tydic.filehub.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LogOssRecord {
    private Long id;
    private String fileUrl;
    private LocalDateTime createTime;
    private String fileName;
}
