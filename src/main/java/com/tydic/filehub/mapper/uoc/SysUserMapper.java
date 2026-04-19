package com.tydic.filehub.mapper.uoc;

import com.tydic.filehub.dto.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface SysUserMapper {
    SysUser selectByUsername(@Param("username") String username);
    void insert(SysUser user);
    void updateLastLoginTime(@Param("username") String username, @Param("lastLoginTime") LocalDateTime lastLoginTime);
}