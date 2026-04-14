package com.tydic.filehub.business;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.tydic.filehub.config.FileHubProperties;
import com.tydic.filehub.dto.LogOssRecord;
import com.tydic.filehub.mapper.uoc.LogOssRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OssUploadService {

    private final LogOssRecordMapper logOssRecordMapper;
    private final FileHubProperties properties;

    public void upload(File file, String fileName, String remark) {
        if (!properties.getOss().isEnabled()) {
            log.info("OSS 未启用，跳过上传: {}", fileName);
            return;
        }
        OSS ossClient = null;
        try {
            FileHubProperties.Oss oss = properties.getOss();
            ossClient = new OSSClientBuilder().build(oss.getEndpoint(), oss.getAccessKeyId(), oss.getAccessKeySecret());
            String objectName = oss.getFolderName() + "/" + fileName;
            ossClient.putObject(new PutObjectRequest(oss.getBucketName(), objectName, file));
            LogOssRecord record = new LogOssRecord();
            record.setFileUrl(oss.getAddress() + fileName);
            record.setFileName(remark);
            record.setCreateTime(LocalDateTime.now());
            logOssRecordMapper.insertSelective(record);
        } catch (Exception e) {
            log.error("上传 OSS 失败", e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
