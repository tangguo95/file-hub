package com.tydic.filehub.mapper.uoc;

import com.tydic.filehub.dto.ConfigPullDatasource;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ConfigPullDatasourceMapper {

    ConfigPullDatasource selectByCode(@Param("datasourceCode") String datasourceCode);

    ConfigPullDatasource selectByCodeAndCreatedBy(@Param("datasourceCode") String datasourceCode, @Param("createdBy") String createdBy);

    List<ConfigPullDatasource> selectAll(@Param("keyword") String keyword);

    List<ConfigPullDatasource> selectAllByCreatedBy(@Param("keyword") String keyword, @Param("createdBy") String createdBy);

    ConfigPullDatasource selectAnyByCode(@Param("datasourceCode") String datasourceCode);

    int insertSelective(ConfigPullDatasource record);

    int updateByPrimaryKeySelective(ConfigPullDatasource record);
}
