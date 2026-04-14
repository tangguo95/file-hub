package com.tydic.filehub.service;

import com.tydic.filehub.dto.CodeFileOper;
import com.tydic.filehub.mapper.uoc.CodeFileOperMapper;
import com.tydic.filehub.scheduler.SchedulerManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CodeFileOperAdminService {

    private final CodeFileOperMapper codeFileOperMapper;
    private final SchedulerManager schedulerManager;

    public List<CodeFileOper> search(String keyword, Integer state) {
        return codeFileOperMapper.search(keyword, state);
    }

    public CodeFileOper findById(Long id) {
        return id == null ? new CodeFileOper() : codeFileOperMapper.selectByPrimaryKey(id);
    }

    public void save(CodeFileOper record) {
        LocalDateTime now = LocalDateTime.now();
        if (!StringUtils.hasText(record.getFileOperCode())) {
            record.setFileOperCode("JOB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        if (!StringUtils.hasText(record.getConcurrentMode())) {
            record.setConcurrentMode("SERIAL");
        }
        if (record.getState() == null) {
            record.setState(1);
        }
        if (record.getJobEnabled() == null) {
            record.setJobEnabled(0);
        }
        if (record.getId() == null) {
            record.setCreateTime(now);
            record.setUpdateTime(now);
            codeFileOperMapper.insertSelective(record);
        } else {
            record.setUpdateTime(now);
            codeFileOperMapper.updateByPrimaryKeySelective(record);
        }
        schedulerManager.refreshJob(record.getFileOperCode());
    }

    public void delete(Long id) {
        CodeFileOper existing = codeFileOperMapper.selectByPrimaryKey(id);
        if (existing == null) {
            return;
        }
        codeFileOperMapper.deleteByPrimaryKey(id);
        schedulerManager.removeJob(existing.getFileOperCode());
    }
}
