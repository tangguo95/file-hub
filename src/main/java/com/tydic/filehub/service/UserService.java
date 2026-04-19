package com.tydic.filehub.service;

import com.tydic.filehub.dto.SysUser;
import com.tydic.filehub.dto.UserRegisterDTO;

public interface UserService {
    void register(UserRegisterDTO registerDTO);
    SysUser findByUsername(String username);
    void updateLastLoginTime(String username);
}