package com.tydic.filehub.datasource;

import com.tydic.filehub.config.FileHubProperties;
import com.tydic.filehub.dto.ConfigPullDatasource;
import com.tydic.filehub.mapper.uoc.ConfigPullDatasourceMapper;
import com.tydic.filehub.utils.AesDecryptUtil;
import com.tydic.filehub.utils.JasyptEncryptorUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicDataSourceManager {

    private final ConfigPullDatasourceMapper configPullDatasourceMapper;
    private final FileHubProperties properties;
    private final Map<String, DataSource> dataSourceCache = new ConcurrentHashMap<>();

    public DataSource getDataSource(String datasourceCode) {
        if (!StringUtils.hasText(datasourceCode)) {
            throw new IllegalArgumentException("数据源编码不能为空");
        }
        return dataSourceCache.computeIfAbsent(datasourceCode, this::createDataSource);
    }

    public boolean testConnection(String datasourceCode) {
        try (Connection ignored = getDataSource(datasourceCode).getConnection()) {
            return true;
        } catch (Exception ex) {
            log.warn("动态数据源连接测试失败: {}", datasourceCode, ex);
            return false;
        }
    }

    public void removeDataSource(String datasourceCode) {
        DataSource ds = dataSourceCache.remove(datasourceCode);
        if (ds instanceof HikariDataSource hikariDataSource) {
            hikariDataSource.close();
        }
    }

    private DataSource createDataSource(String datasourceCode) {
        ConfigPullDatasource config = configPullDatasourceMapper.selectByCode(datasourceCode);
        if (config == null) {
            throw new IllegalArgumentException("未找到有效数据源配置: " + datasourceCode);
        }
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getDataIp());
        hikariConfig.setUsername(decrypt(config.getUsername()));
        hikariConfig.setPassword(decrypt(config.getPassword()));
        hikariConfig.setDriverClassName(resolveDriver(config));
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setMaximumPoolSize(5);
        hikariConfig.setConnectionTimeout(10_000);
        hikariConfig.setPoolName("dynamic-" + datasourceCode);
        return new HikariDataSource(hikariConfig);
    }

    private String resolveDriver(ConfigPullDatasource config) {
        if (StringUtils.hasText(config.getDriveName())) {
            return config.getDriveName();
        }
        return config.getDatasourceType() != null && config.getDatasourceType() == 2
                ? "oracle.jdbc.OracleDriver"
                : "com.mysql.cj.jdbc.Driver";
    }

    private String decrypt(String ciphertext) {
        if (!StringUtils.hasText(ciphertext)) {
            return "";
        }
        try {
            if ("aes".equalsIgnoreCase(properties.getCrypto().getDecryptType())) {
                return AesDecryptUtil.decrypt(ciphertext, properties.getCrypto().getAesKey());
            }
            return JasyptEncryptorUtils.decode(ciphertext);
        } catch (Exception ex) {
            log.warn("动态数据源凭据解密失败，回退原文");
            return ciphertext;
        }
    }
}
