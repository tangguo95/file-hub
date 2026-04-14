package com.tydic.filehub.mapper.uoc;

import com.tydic.filehub.dto.ConfigPullDatasource;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ConfigPullDatasourceMapper {

    ConfigPullDatasource selectByCode(@Param("datasourceCode") String datasourceCode);

    List<ConfigPullDatasource> selectAll(@Param("keyword") String keyword);

    ConfigPullDatasource selectAnyByCode(@Param("datasourceCode") String datasourceCode);

    int insertSelective(ConfigPullDatasource record);

    int updateByPrimaryKeySelective(ConfigPullDatasource record);
}
