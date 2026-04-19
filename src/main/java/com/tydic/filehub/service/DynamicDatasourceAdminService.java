package com.tydic.filehub.service;

import com.tydic.filehub.datasource.DynamicDataSourceManager;
import com.tydic.filehub.dto.ConfigPullDatasource;
import com.tydic.filehub.mapper.uoc.ConfigPullDatasourceMapper;
import com.tydic.filehub.utils.AesDecryptUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DynamicDatasourceAdminService {

    private final ConfigPullDatasourceMapper configPullDatasourceMapper;
    private final DynamicDataSourceManager dynamicDataSourceManager;

    @Value("${filehub.crypto.aes-key}")
    private String aesKey;

    public List<ConfigPullDatasource> list(String keyword) {
        return configPullDatasourceMapper.selectAll(keyword);
    }

    public List<ConfigPullDatasource> listByCreatedBy(String keyword, String createdBy) {
        return configPullDatasourceMapper.selectAllByCreatedBy(keyword, createdBy);
    }

    public ConfigPullDatasource find(String datasourceCode) {
        return !StringUtils.hasText(datasourceCode)
                ? new ConfigPullDatasource()
                : configPullDatasourceMapper.selectAnyByCode(datasourceCode);
    }

    public ConfigPullDatasource findByCodeAndCreatedBy(String datasourceCode, String createdBy) {
        return !StringUtils.hasText(datasourceCode)
                ? new ConfigPullDatasource()
                : configPullDatasourceMapper.selectByCodeAndCreatedBy(datasourceCode, createdBy);
    }

    public void save(ConfigPullDatasource datasource) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        ConfigPullDatasource existing = datasource.getId() == null ? null : configPullDatasourceMapper.selectAnyByCode(datasource.getDatasourceCode());
        if (datasource.getState() == null) {
            datasource.setState(1);
        }
        if (StringUtils.hasText(datasource.getPassword())) {
            datasource.setPassword(AesDecryptUtil.encrypt(datasource.getPassword(), aesKey));
        } else if (existing != null) {
            datasource.setPassword(existing.getPassword());
        }
        if (datasource.getId() == null) {
            datasource.setCreateTime(now);
            datasource.setUpdateTime(now);
            configPullDatasourceMapper.insertSelective(datasource);
        } else {
            datasource.setUpdateTime(now);
            configPullDatasourceMapper.updateByPrimaryKeySelective(datasource);
        }
        dynamicDataSourceManager.removeDataSource(datasource.getDatasourceCode());
    }

    public boolean testConnection(String datasourceCode, String employeeId) {
        dynamicDataSourceManager.removeDataSource(datasourceCode);
        return dynamicDataSourceManager.testConnection(datasourceCode);
    }

    public void toggle(String datasourceCode, String createdBy) {
        ConfigPullDatasource existing = configPullDatasourceMapper.selectByCodeAndCreatedBy(datasourceCode, createdBy);
        if (existing == null) {
            return;
        }
        existing.setState(existing.getState() != null && existing.getState() == 1 ? 0 : 1);
        existing.setUpdateTime(LocalDateTime.now());
        configPullDatasourceMapper.updateByPrimaryKeySelective(existing);
        dynamicDataSourceManager.removeDataSource(datasourceCode);
    }
}
