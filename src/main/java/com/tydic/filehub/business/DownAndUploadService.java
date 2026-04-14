package com.tydic.filehub.business;

import com.tydic.filehub.config.FileHubProperties;
import com.tydic.filehub.datasource.DynamicSqlExecutor;
import com.tydic.filehub.dto.CodeFileOper;
import com.tydic.filehub.mapper.uoc.BaseSqlMapper;
import com.tydic.filehub.mapper.uoc.CodeFileOperMapper;
import com.tydic.filehub.scheduler.TriggerType;
import com.tydic.filehub.service.impl.CodeFIleOperService;
import com.tydic.filehub.service.impl.FileFormatParser;
import com.tydic.filehub.utils.ExceptionUtil;
import com.tydic.filehub.utils.JasyptEncryptorUtils;
import com.tydic.filehub.utils.SFTPUtils;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class DownAndUploadService {

    private final CodeFileOperMapper codeFileOperMapper;
    private final BaseSqlMapper baseSqlMapper;
    private final DynamicSqlExecutor dynamicSqlExecutor;
    private final OssUploadService ossUploadService;
    private final CodeFIleOperService codeFIleOperService;
    private final FileFormatParser fileFormatParser;
    private final ApplicationContext applicationContext;
    private final FileHubProperties properties;

    public void executeJob(String fileOperCode, TriggerType triggerType) throws Exception {
        log.info("开始执行任务: fileOperCode={}, triggerType={}", fileOperCode, triggerType);
        if (!StringUtils.hasText(fileOperCode)) {
            throw new IllegalArgumentException("定时任务参数为空");
        }
        CodeFileOper codeFileOper = codeFileOperMapper.selectByFileOperCode(fileOperCode);
        if (codeFileOper == null) {
            throw new IllegalArgumentException("未查询到配置的文件操作信息");
        }
        if (codeFileOper.getOperType() == null) {
            throw new IllegalArgumentException("未查询到配置的文件操作类型");
        }
        executeInternal(codeFileOper);
        log.info("任务执行完成: {}", fileOperCode);
    }

    private void executeInternal(CodeFileOper codeFileOper) throws Exception {
        String serverUserName = decodeSecret(codeFileOper.getServerUserName());
        String serverPassword = decodeSecret(codeFileOper.getServerPassword());
        String filePath = codeFileOper.getFilePath();
        String fileName = codeFileOper.getFileName();
        Integer operType = codeFileOper.getOperType();
        Integer isUploadOss = codeFileOper.getIsUploadOss();
        Integer isDelete = codeFileOper.getIsDelete();
        List<String> filesToProcess = resolveFilesToProcess(codeFileOper, serverUserName, serverPassword);

        if (operType == 1) {
            codeFIleOperService.prepareForBatchInsert(codeFileOper);
        }

        for (String currentFileName : filesToProcess) {
            fileName = currentFileName;
            SFTPUtils sftpUtils = new SFTPUtils(codeFileOper.getServerIp(), codeFileOper.getServerPort(), serverUserName, serverPassword);
            if (operType == 1) {
                handleDownload(codeFileOper, sftpUtils, filePath, fileName, isUploadOss, isDelete);
            } else if (operType == 2) {
                handleUpload(codeFileOper, sftpUtils, filePath, fileName, isUploadOss);
            } else {
                throw new IllegalArgumentException("不支持的操作类型: " + operType);
            }
        }
    }

    private List<String> resolveFilesToProcess(CodeFileOper codeFileOper, String serverUserName, String serverPassword) {
        List<String> filesToProcess = new ArrayList<>();
        if (codeFileOper.getOperType() == 1 && useFileListShellPath(codeFileOper, codeFileOper.getFileListShell())) {
            SFTPUtils sftpForExec = new SFTPUtils(codeFileOper.getServerIp(), codeFileOper.getServerPort(), serverUserName, serverPassword);
            sftpForExec.connect();
            try {
                List<String> fromShell = sftpForExec.execCommand(codeFileOper.getFilePath(), codeFileOper.getFileListShell().trim());
                if (CollectionUtils.isEmpty(fromShell)) {
                    return filesToProcess;
                }
                for (String fn : fromShell) {
                    if (fn != null && !fn.contains("..") && !fn.contains("/") && !fn.contains("\\")) {
                        filesToProcess.add(fn.trim());
                    }
                }
            } catch (Exception e) {
                throw new IllegalStateException("执行 file_list_shell 失败：" + e.getMessage(), e);
            } finally {
                sftpForExec.disconnect();
            }
            return filesToProcess;
        }

        String extFileName = "";
        if (StringUtils.hasText(codeFileOper.getFileNameExtSql())) {
            List<Map<String, Object>> maps = executeSelect(codeFileOper.getFileNameExtSql(), codeFileOper);
            if (!CollectionUtils.isEmpty(maps) && maps.get(0).get("EXTFILENAME") != null) {
                extFileName = maps.get(0).get("EXTFILENAME").toString();
            }
        }
        String[] extFileNameArr = StringUtils.hasText(extFileName) ? extFileName.split(",") : new String[]{""};
        String nameTemplate = StringUtils.hasText(codeFileOper.getFileName()) ? codeFileOper.getFileName() : "";
        for (String ext : extFileNameArr) {
            filesToProcess.add(nameTemplate.replace("{ext}", ext));
        }
        return filesToProcess;
    }

    private void handleDownload(CodeFileOper codeFileOper, SFTPUtils sftpUtils, String filePath, String fileName,
                                Integer isUploadOss, Integer isDelete) throws Exception {
        sftpUtils.connect();
        String localFilePath = properties.getStorage().getLocalDownloadDir() + "/" + fileName;
        boolean needLocalTempFile = isUploadOss != null && isUploadOss == 1
                || isExcelFormat(codeFileOper.getFileFormat());
        File localTempFile = needLocalTempFile ? new File(localFilePath) : null;

        Map<String, Object> beforeExtra = new HashMap<>();
        beforeExtra.put("fileName", fileName);
        beforeExtra.put("localFilePath", localFilePath);
        runFileOperGroovy(codeFileOper, codeFileOper.getFileOperGroovyBefore(),
                codeFileOper.getFileOperGroovyBeforeEnable(), "beforeDownload", beforeExtra);

        try {
            if (needLocalTempFile) {
                ensureParentDirectory(localTempFile);
                sftpUtils.downloadFile(filePath, fileName, properties.getStorage().getLocalDownloadDir() + "/", fileName);
            }

            String format = codeFileOper.getFileFormat() != null ? codeFileOper.getFileFormat() : "CSV";
            String splitLabel = Pattern.quote(codeFileOper.getSplitLabel());
            int skipHeaderLines = codeFileOper.getSkipHeaderLines() != null ? Math.max(0, codeFileOper.getSkipHeaderLines()) : 0;

            List<Map<String, Object>> rows;
            if (isExcelFormat(format)) {
                try (InputStream inputStream = Files.newInputStream(localTempFile.toPath())) {
                    rows = fileFormatParser.parseFile(format, inputStream, splitLabel, skipHeaderLines);
                }
            } else {
                try (InputStream inputStream = needLocalTempFile
                        ? Files.newInputStream(localTempFile.toPath())
                        : sftpUtils.getFileInputStream(filePath, fileName)) {
                    rows = fileFormatParser.parseFile(format, inputStream, splitLabel, skipHeaderLines);
                }
            }

            if (rows.isEmpty()) {
                return;
            }
            codeFIleOperService.doRecordFileData(codeFileOper, rows);

            if (needLocalTempFile) {
                ossUploadService.upload(localTempFile, fileName, codeFileOper.getRemark());
            }
            Map<String, Object> afterExtra = new HashMap<>();
            afterExtra.put("fileName", fileName);
            afterExtra.put("localFilePath", localFilePath);
            runFileOperGroovy(codeFileOper, codeFileOper.getFileOperGroovyAfter(),
                    codeFileOper.getFileOperGroovyAfterEnable(), "afterDownload", afterExtra);
            if (isDelete != null && isDelete == 1) {
                sftpUtils.deleteSFTP(filePath, fileName);
            }
            updateExecutionInfo(codeFileOper.getId(), "SUCCESS", "处理成功");
        } catch (Exception ex) {
            updateExecutionInfo(codeFileOper.getId(), "FAILED", "下载文件入库失败：" + ExceptionUtil.getStackTrace(ex));
            throw ex;
        } finally {
            sftpUtils.disconnect();
            if (needLocalTempFile && localTempFile != null && localTempFile.exists()) {
                Files.deleteIfExists(localTempFile.toPath());
            }
        }
    }

    private boolean isExcelFormat(String format) {
        return "XLS".equalsIgnoreCase(format) || "XLSX".equalsIgnoreCase(format);
    }

    private void handleUpload(CodeFileOper codeFileOper, SFTPUtils sftpUtils, String filePath, String fileName,
                              Integer isUploadOss) throws Exception {
        List<Map<String, Object>> resultList = executeSelect(codeFileOper.getQuerySql(), codeFileOper);
        if (resultList == null) {
            throw new IllegalArgumentException("未查询到配置的文件数据源信息");
        }
        File uploadDir = new File(properties.getStorage().getLocalUploadDir());
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        File localFile = new File(uploadDir, fileName);

        String format = codeFileOper.getFileFormat() != null ? codeFileOper.getFileFormat() : "CSV";
        String splitLabel = codeFileOper.getSplitLabel() != null ? codeFileOper.getSplitLabel() : ",";
        fileFormatParser.writeFile(format, localFile, splitLabel, resultList);

        Map<String, Object> beforeExtra = new HashMap<>();
        beforeExtra.put("fileName", fileName);
        beforeExtra.put("localFilePath", localFile.getAbsolutePath());
        beforeExtra.put("resultList", resultList);
        runFileOperGroovy(codeFileOper, codeFileOper.getFileOperGroovyBefore(),
                codeFileOper.getFileOperGroovyBeforeEnable(), "beforeUpload", beforeExtra);

        sftpUtils.connect();
        boolean uploadOk;
        try {
            uploadOk = sftpUtils.uploadFile(filePath, fileName, properties.getStorage().getLocalUploadDir() + "/", fileName);
        } finally {
            sftpUtils.disconnect();
        }
        if (!uploadOk) {
            throw new IllegalStateException("上传文件失败");
        }

        Map<String, Object> afterExtra = new HashMap<>();
        afterExtra.put("fileName", fileName);
        afterExtra.put("localFilePath", localFile.getAbsolutePath());
        afterExtra.put("uploadSuccess", true);
        runFileOperGroovy(codeFileOper, codeFileOper.getFileOperGroovyAfter(),
                codeFileOper.getFileOperGroovyAfterEnable(), "afterUpload", afterExtra);
        if (isUploadOss != null && isUploadOss == 1) {
            ossUploadService.upload(localFile, fileName, codeFileOper.getRemark());
        }
        Files.deleteIfExists(localFile.toPath());
        updateExecutionInfo(codeFileOper.getId(), "SUCCESS", "上传文件成功");
    }

    private void updateExecutionInfo(Long id, String status, String message) {
        CodeFileOper update = new CodeFileOper();
        update.setId(id);
        update.setDealTime(LocalDateTime.now());
        update.setDealRemark(message);
        update.setLastStatus(status);
        update.setLastMessage(message);
        update.setLastFinishTime(LocalDateTime.now());
        codeFileOperMapper.updateByPrimaryKeySelective(update);
    }

    private String decodeSecret(String secret) {
        if (!StringUtils.hasText(secret)) {
            return secret;
        }
        try {
            return JasyptEncryptorUtils.decode(secret);
        } catch (Exception ex) {
            return secret;
        }
    }

    private boolean useFileListShellPath(CodeFileOper op, String fileListShell) {
        Integer enabled = op.getFileListShellEnable();
        if (enabled != null && enabled == 0) {
            return false;
        }
        return StringUtils.hasText(fileListShell);
    }

    private void runFileOperGroovy(CodeFileOper oper, String script, Integer enableFlag, String phase,
                                   Map<String, Object> extra) {
        if (enableFlag == null || enableFlag != 1 || !StringUtils.hasText(script)) {
            return;
        }
        try {
            Binding binding = new Binding();
            binding.setVariable("applicationContext", applicationContext);
            binding.setVariable("codeFileOper", oper);
            if (extra != null) {
                for (Map.Entry<String, Object> entry : extra.entrySet()) {
                    binding.setVariable(entry.getKey(), entry.getValue());
                }
            }
            new GroovyShell(binding).evaluate(script);
        } catch (Exception ex) {
            log.error("file_oper groovy [{}] 执行异常", phase, ex);
            throw new IllegalStateException("Groovy脚本(" + phase + ")执行失败：" + ex.getMessage(), ex);
        }
    }

    private List<Map<String, Object>> executeSelect(String sql, CodeFileOper codeFileOper) {
        if (!StringUtils.hasText(sql)) {
            return List.of();
        }
        if (StringUtils.hasText(codeFileOper.getDatasourceCode())) {
            return dynamicSqlExecutor.selectStr(codeFileOper.getDatasourceCode(), sql);
        }
        return baseSqlMapper.selectStr(sql);
    }

    private void ensureParentDirectory(File file) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            Files.createDirectories(parent.toPath());
        }
    }
}
