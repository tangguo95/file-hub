package com.tydic.filehub.service.impl;

import com.tydic.filehub.datasource.DynamicSqlExecutor;
import com.tydic.filehub.dto.CodeFileOper;
import com.tydic.filehub.mapper.uoc.BaseSqlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodeFIleOperService {

    private final BaseSqlMapper baseSqlMapper;
    private final DynamicSqlExecutor dynamicSqlExecutor;

    public void prepareForBatchInsert(CodeFileOper codeFileOper) {
        if (codeFileOper.getDownloadDealType() == null || codeFileOper.getDownloadDealType() != 3) {
            return;
        }
        String downloadTbName = codeFileOper.getDownloadTbName();
        if (!StringUtils.hasText(downloadTbName)) {
            return;
        }
        String deleteSqlStr = "delete from " + downloadTbName;
        executeDelete(deleteSqlStr, codeFileOper.getDatasourceCode());
    }

    public void doRecordFileData(CodeFileOper codeFileOper, BufferedReader bf) throws Exception {
        Integer downloadDealType = codeFileOper.getDownloadDealType();
        String downloadTbName = codeFileOper.getDownloadTbName();
        String insertColumnName = StringUtils.hasText(codeFileOper.getInsertColumnName()) ? codeFileOper.getInsertColumnName() : "";
        String datasourceCode = codeFileOper.getDatasourceCode();
        String splitLabel = Pattern.quote(codeFileOper.getSplitLabel());
        List<Map<String, String>> tableColType = StringUtils.hasText(datasourceCode)
                ? queryTableColType(datasourceCode, downloadTbName)
                : List.of();

        int skipHeaderLines = codeFileOper.getSkipHeaderLines() != null ? Math.max(0, codeFileOper.getSkipHeaderLines()) : 0;
        for (int i = 0; i < skipHeaderLines; i++) {
            if (bf.readLine() == null) {
                return;
            }
        }

        if (downloadDealType == null || (downloadDealType != 1 && downloadDealType != 2 && downloadDealType != 3)) {
            throw new BusiException("9999", "下载处理类型不正确");
        }

        if (downloadDealType == 2 && !StringUtils.hasText(codeFileOper.getDownloadDataUpdateCondition())) {
            throw new BusiException("9999", "下载数据更新查询数据的条件字段不能为空");
        }

        if (downloadDealType == 1 || downloadDealType == 3) {
            batchInsert(downloadTbName, insertColumnName, datasourceCode, tableColType, splitLabel, bf, codeFileOper);
            return;
        }
        updateThenInsert(downloadTbName, insertColumnName, datasourceCode, tableColType, splitLabel, bf, codeFileOper);
    }

    public void doRecordFileData(CodeFileOper codeFileOper, List<Map<String, Object>> rows) throws Exception {
        Integer downloadDealType = codeFileOper.getDownloadDealType();
        String downloadTbName = codeFileOper.getDownloadTbName();
        String insertColumnName = StringUtils.hasText(codeFileOper.getInsertColumnName()) ? codeFileOper.getInsertColumnName() : "";
        String datasourceCode = codeFileOper.getDatasourceCode();
        String splitLabel = Pattern.quote(codeFileOper.getSplitLabel());
        List<Map<String, String>> tableColType = StringUtils.hasText(datasourceCode)
                ? queryTableColType(datasourceCode, downloadTbName)
                : List.of();

        if (downloadDealType == null || (downloadDealType != 1 && downloadDealType != 2 && downloadDealType != 3)) {
            throw new BusiException("9999", "下载处理类型不正确");
        }
        if (downloadDealType == 2 && !StringUtils.hasText(codeFileOper.getDownloadDataUpdateCondition())) {
            throw new BusiException("9999", "下载数据更新查询数据的条件字段不能为空");
        }

        if (downloadDealType == 1 || downloadDealType == 3) {
            batchInsertFromRows(downloadTbName, insertColumnName, datasourceCode, tableColType, rows, codeFileOper);
            return;
        }
        updateThenInsertFromRows(downloadTbName, insertColumnName, datasourceCode, tableColType, rows, codeFileOper);
    }

    private void batchInsertFromRows(String tableName, String columnNames, String datasourceCode,
                                     List<Map<String, String>> tableColType, List<Map<String, Object>> rows,
                                     CodeFileOper codeFileOper) throws Exception {
        String insertSqlPrefix = "insert into " + tableName + "(" + columnNames + ") values ";
        StringBuilder batchInsertSql = new StringBuilder();
        List<String> rowFragments = new ArrayList<>();
        int batchSize = 0;
        int maxBatchSize = 1000;
        boolean stopOnRowError = codeFileOper.getStopOnRowError() == null || codeFileOper.getStopOnRowError() == 1;
        Integer maxRowErrors = codeFileOper.getMaxRowErrors();
        RowErrorAccumulator rowErrors = new RowErrorAccumulator();
        int lineNo = 0;

        for (Map<String, Object> row : rows) {
            lineNo++;
            String[] split = row.values().stream()
                    .map(v -> v == null ? "" : v.toString())
                    .toArray(String[]::new);
            if (batchSize == 0) {
                batchInsertSql.append(insertSqlPrefix);
            }
            String rowFragment = buildRowValues(split, tableColType);
            batchInsertSql.append(rowFragment).append(",");
            rowFragments.add(rowFragment);
            batchSize++;
            if (batchSize >= maxBatchSize) {
                executeBatchInsertOrFallback(batchInsertSql, rowFragments, insertSqlPrefix, datasourceCode,
                        stopOnRowError, maxRowErrors, rowErrors, lineNo - rowFragments.size() + 1);
                batchInsertSql.setLength(0);
                rowFragments.clear();
                batchSize = 0;
            }
        }
        if (!rowFragments.isEmpty()) {
            executeBatchInsertOrFallback(batchInsertSql, rowFragments, insertSqlPrefix, datasourceCode,
                    stopOnRowError, maxRowErrors, rowErrors, lineNo - rowFragments.size() + 1);
        }
    }

    private void batchInsert(String tableName, String columnNames, String datasourceCode, List<Map<String, String>> tableColType,
                             String splitLabel, BufferedReader reader, CodeFileOper codeFileOper) throws Exception {
        String insertSqlPrefix = "insert into " + tableName + "(" + columnNames + ") values ";
        StringBuilder batchInsertSql = new StringBuilder();
        List<String> rowFragments = new ArrayList<>();
        String line;
        int batchSize = 0;
        int maxBatchSize = 1000;
        boolean stopOnRowError = codeFileOper.getStopOnRowError() == null || codeFileOper.getStopOnRowError() == 1;
        Integer maxRowErrors = codeFileOper.getMaxRowErrors();
        RowErrorAccumulator rowErrors = new RowErrorAccumulator();
        int lineNo = 0;

        while ((line = reader.readLine()) != null) {
            lineNo++;
            String[] split = line.split(splitLabel, -1);
            if (batchSize == 0) {
                batchInsertSql.append(insertSqlPrefix);
            }
            String rowFragment = buildRowValues(split, tableColType);
            batchInsertSql.append(rowFragment).append(",");
            rowFragments.add(rowFragment);
            batchSize++;
            if (batchSize >= maxBatchSize) {
                executeBatchInsertOrFallback(batchInsertSql, rowFragments, insertSqlPrefix, datasourceCode,
                        stopOnRowError, maxRowErrors, rowErrors, lineNo - rowFragments.size() + 1);
                batchInsertSql.setLength(0);
                rowFragments.clear();
                batchSize = 0;
            }
        }
        if (!rowFragments.isEmpty()) {
            executeBatchInsertOrFallback(batchInsertSql, rowFragments, insertSqlPrefix, datasourceCode,
                    stopOnRowError, maxRowErrors, rowErrors, lineNo - rowFragments.size() + 1);
        }
    }

    private void updateThenInsert(String tableName, String columnNames, String datasourceCode, List<Map<String, String>> tableColType,
                                  String splitLabel, BufferedReader reader, CodeFileOper codeFileOper) throws Exception {
        String[] updateConditionArr = codeFileOper.getDownloadDataUpdateCondition().split(",");
        String[] columnNameArr = columnNames.split(",");
        Map<String, Integer> conditionMap = createConditionMap(updateConditionArr, columnNameArr);
        String deleteSqlPrefix = "delete from " + tableName + " where ";
        String line;

        while ((line = reader.readLine()) != null) {
            String[] split = line.split(splitLabel, -1);
            String insertSql = "insert into " + tableName + "(" + columnNames + ") values " + buildRowValues(split, tableColType);
            String deleteSql = buildDeleteSql(deleteSqlPrefix, conditionMap, split);
            executeDelete(deleteSql, datasourceCode);
            executeInsert(insertSql, datasourceCode);
        }
    }

    private void updateThenInsertFromRows(String tableName, String columnNames, String datasourceCode,
                                          List<Map<String, String>> tableColType, List<Map<String, Object>> rows,
                                          CodeFileOper codeFileOper) throws Exception {
        String[] updateConditionArr = codeFileOper.getDownloadDataUpdateCondition().split(",");
        String[] columnNameArr = columnNames.split(",");
        Map<String, Integer> conditionMap = createConditionMap(updateConditionArr, columnNameArr);
        String deleteSqlPrefix = "delete from " + tableName + " where ";

        for (Map<String, Object> row : rows) {
            String[] values = row.values().stream()
                    .map(v -> v == null ? "" : v.toString())
                    .toArray(String[]::new);
            String insertSql = "insert into " + tableName + "(" + columnNames + ") values " + buildRowValues(values, tableColType);
            String deleteSql = buildDeleteSql(deleteSqlPrefix, conditionMap, values);
            executeDelete(deleteSql, datasourceCode);
            executeInsert(insertSql, datasourceCode);
        }
    }

    private void executeBatchInsertOrFallback(StringBuilder batchInsertSql, List<String> rowFragments, String insertSqlPrefix,
                                              String datasourceCode, boolean stopOnRowError, Integer maxRowErrors,
                                              RowErrorAccumulator rowErrors, int startLineNo) throws Exception {
        if (rowFragments.isEmpty()) {
            return;
        }
        String fullSql = batchInsertSql.substring(0, batchInsertSql.length() - 1);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            executeInsert(fullSql, datasourceCode);
            stopWatch.stop();
        } catch (Exception ex) {
            stopWatch.stop();
            if (stopOnRowError) {
                throw ex;
            }
            for (int i = 0; i < rowFragments.size(); i++) {
                try {
                    executeInsert(insertSqlPrefix + rowFragments.get(i), datasourceCode);
                } catch (Exception rowEx) {
                    rowErrors.failedRows++;
                    if (maxRowErrors != null && rowErrors.failedRows > maxRowErrors) {
                        throw new BusiException("9999",
                                "失败行数超过 max_row_errors=" + maxRowErrors + "，已中断");
                    }
                    log.warn("第 {} 行数据入库失败: {}", startLineNo + i, rowEx.getMessage());
                }
            }
        }
    }

    private String buildRowValues(String[] split, List<Map<String, String>> tableColType) {
        StringBuilder builder = new StringBuilder("(");
        for (int i = 0; i < split.length; i++) {
            String value = split[i] == null ? "" : split[i].replace("'", "''");
            String dataType = resolveDataType(tableColType, i);
            if ("NUMBER".equalsIgnoreCase(dataType) || "INT".equalsIgnoreCase(dataType)
                    || "BIGINT".equalsIgnoreCase(dataType) || "DECIMAL".equalsIgnoreCase(dataType)) {
                builder.append(StringUtils.hasText(value) ? value : "null");
            } else {
                builder.append("'").append(value).append("'");
            }
            if (i < split.length - 1) {
                builder.append(",");
            }
        }
        builder.append(")");
        return builder.toString();
    }

    private String resolveDataType(List<Map<String, String>> tableColType, int index) {
        if (index >= tableColType.size()) {
            return "VARCHAR";
        }
        return tableColType.get(index).getOrDefault("DATA_TYPE", "VARCHAR");
    }

    private Map<String, Integer> createConditionMap(String[] updateConditionArr, String[] columnNameArr) {
        Map<String, Integer> conditionMap = new LinkedHashMap<>();
        for (String condition : updateConditionArr) {
            for (int i = 0; i < columnNameArr.length; i++) {
                if (condition.trim().equalsIgnoreCase(columnNameArr[i].trim())) {
                    conditionMap.put(condition.trim(), i);
                    break;
                }
            }
        }
        return conditionMap;
    }

    private String buildDeleteSql(String deleteSqlPrefix, Map<String, Integer> conditionMap, String[] split) {
        StringBuilder deleteSql = new StringBuilder(deleteSqlPrefix);
        boolean first = true;
        for (Map.Entry<String, Integer> entry : conditionMap.entrySet()) {
            if (!first) {
                deleteSql.append(" and ");
            }
            String value = entry.getValue() < split.length ? split[entry.getValue()] : "";
            deleteSql.append(entry.getKey()).append(" = '").append(value.replace("'", "''")).append("'");
            first = false;
        }
        return deleteSql.toString();
    }

    private List<Map<String, String>> queryTableColType(String datasourceCode, String downloadTbName) {
        if (!StringUtils.hasText(datasourceCode) || !StringUtils.hasText(downloadTbName)) {
            return List.of();
        }
        String tableName = downloadTbName.contains(".")
                ? downloadTbName.substring(downloadTbName.indexOf('.') + 1)
                : downloadTbName;
        return dynamicSqlExecutor.queryTableColType(datasourceCode, tableName);
    }

    private void executeInsert(String sql, String datasourceCode) {
        if (StringUtils.hasText(datasourceCode)) {
            dynamicSqlExecutor.insertStr(datasourceCode, sql);
            return;
        }
        baseSqlMapper.insertStr(sql);
    }

    private void executeDelete(String sql, String datasourceCode) {
        if (StringUtils.hasText(datasourceCode)) {
            dynamicSqlExecutor.deleteSqlStr(datasourceCode, sql);
            return;
        }
        baseSqlMapper.deleteSqlStr(sql);
    }

    private static final class RowErrorAccumulator {
        private int failedRows;
    }
}
