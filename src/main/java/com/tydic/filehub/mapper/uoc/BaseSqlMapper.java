package com.tydic.filehub.mapper.uoc;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository("UocBaseSqlMapper")
public interface BaseSqlMapper {

    List<Map<String, Object>> selectStr(@Param("sqlStr") String sqlStr);

    List<Map<String, Object>> queryDtsWarnPhoneNo();

    int insertStr(@Param("sqlStr") String sqlStr);

    int deleteSqlStr(@Param("sqlStr") String sqlStr);
}
