package com.tydic.filehub.mapper.uoc;

import com.tydic.filehub.dto.JobExecutionLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface JobExecutionLogMapper {
    int insert(JobExecutionLog record);

    int updateCompletion(JobExecutionLog record);

    List<JobExecutionLog> selectRecentByFileOperCode(@Param("fileOperCode") String fileOperCode,
                                                     @Param("limit") int limit);

    List<JobExecutionLog> selectRecent(@Param("limit") int limit);

    List<JobExecutionLog> selectRecentByCreatedBy(@Param("createdBy") String createdBy, @Param("limit") int limit);
}
