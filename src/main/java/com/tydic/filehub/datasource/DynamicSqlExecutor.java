package com.tydic.filehub.datasource;

import com.tydic.filehub.dto.ConfigPullDatasource;
import com.tydic.filehub.mapper.uoc.ConfigPullDatasourceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicSqlExecutor {

    private final DynamicDataSourceManager dynamicDataSourceManager;
    private final ConfigPullDatasourceMapper configPullDatasourceMapper;

    public List<Map<String, Object>> selectStr(String datasourceCode, String sql) {
        DataSource ds = dynamicDataSourceManager.getDataSource(datasourceCode);
        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return resultSetToList(rs);
        } catch (Exception e) {
            log.error("动态数据源查询失败: datasourceCode={}", datasourceCode, e);
            throw new RuntimeException("动态数据源查询失败: " + e.getMessage(), e);
        }
    }

    public int insertStr(String datasourceCode, String sql) {
        return update(datasourceCode, sql, "插入");
    }

    public int deleteSqlStr(String datasourceCode, String sql) {
        return update(datasourceCode, sql, "删除");
    }

    public List<Map<String, String>> queryTableColType(String datasourceCode, String tableName) {
        ConfigPullDatasource config = configPullDatasourceMapper.selectByCode(datasourceCode);
        if (config == null || config.getDatasourceType() == null || config.getDatasourceType() != 2) {
            return new ArrayList<>();
        }
        String sql = "SELECT column_name AS COLUMN_NAME, data_type AS DATA_TYPE FROM all_tab_columns WHERE table_name = UPPER('"
                + tableName.replace("'", "''") + "')";
        List<Map<String, Object>> list = selectStr(datasourceCode, sql);
        List<Map<String, String>> result = new ArrayList<>();
        for (Map<String, Object> row : list) {
            Map<String, String> mapped = new LinkedHashMap<>();
            mapped.put("COLUMN_NAME", row.getOrDefault("COLUMN_NAME", "").toString());
            mapped.put("DATA_TYPE", row.getOrDefault("DATA_TYPE", "").toString());
            result.add(mapped);
        }
        return result;
    }

    public Integer getDatasourceType(String datasourceCode) {
        if (!StringUtils.hasText(datasourceCode)) {
            return null;
        }
        ConfigPullDatasource config = configPullDatasourceMapper.selectByCode(datasourceCode);
        return config != null ? config.getDatasourceType() : null;
    }

    private int update(String datasourceCode, String sql, String action) {
        DataSource ds = dynamicDataSourceManager.getDataSource(datasourceCode);
        try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(sql);
        } catch (Exception e) {
            log.error("动态数据源{}失败: datasourceCode={}", action, datasourceCode, e);
            throw new RuntimeException("动态数据源" + action + "失败: " + e.getMessage(), e);
        }
    }

    private List<Map<String, Object>> resultSetToList(ResultSet rs) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                if (!StringUtils.hasText(columnName)) {
                    columnName = metaData.getColumnName(i);
                }
                row.put(columnName, rs.getObject(i));
            }
            list.add(row);
        }
        return list;
    }
}
