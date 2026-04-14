package com.tydic.filehub.mapper.uoc;


import com.tydic.filehub.dto.LogOssRecord;

public interface LogOssRecordMapper {
    int deleteByPrimaryKey(Long id);

    int insert(LogOssRecord record);

    int insertSelective(LogOssRecord record);

    LogOssRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(LogOssRecord record);

    int updateByPrimaryKey(LogOssRecord record);
}