package com.tydic.filehub.mapper.uoc;

import com.tydic.filehub.dto.CodeFileOper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CodeFileOperMapper {
    int deleteByPrimaryKey(Long id);

    int insertSelective(CodeFileOper record);

    CodeFileOper selectByPrimaryKey(Long id);

    CodeFileOper selectByIdAndCreatedBy(@Param("id") Long id, @Param("createdBy") String createdBy);

    int updateByPrimaryKeySelective(CodeFileOper record);

    CodeFileOper selectByFileOperCode(String fileOperCode);

    CodeFileOper selectByFileOperCodeAndCreatedBy(@Param("fileOperCode") String fileOperCode, @Param("createdBy") String createdBy);

    List<CodeFileOper> selectAll();

    List<CodeFileOper> selectSchedulableJobs();

    List<CodeFileOper> search(@Param("keyword") String keyword, @Param("state") Integer state, @Param("createdBy") String createdBy);

    int deleteByIdAndCreatedBy(@Param("id") Long id, @Param("createdBy") String createdBy);
}
