package com.tydic.filehub.mapper.uoc;

import com.tydic.filehub.dto.CodeFileOper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CodeFileOperMapper {
    int deleteByPrimaryKey(Long id);

    int insertSelective(CodeFileOper record);

    CodeFileOper selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CodeFileOper record);

    CodeFileOper selectByFileOperCode(String fileOperCode);

    List<CodeFileOper> selectAll();

    List<CodeFileOper> selectSchedulableJobs();

    List<CodeFileOper> search(@Param("keyword") String keyword, @Param("state") Integer state);
}
