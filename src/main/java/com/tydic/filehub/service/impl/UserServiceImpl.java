package com.tydic.filehub.service.impl;

import com.tydic.filehub.dto.SysUser;
import com.tydic.filehub.dto.UserRegisterDTO;
import com.tydic.filehub.mapper.uoc.SysUserMapper;
import com.tydic.filehub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void register(UserRegisterDTO registerDTO) {
        if (userMapper.selectByUsername(registerDTO.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new RuntimeException("两次密码输入不一致");
        }

        SysUser user = new SysUser();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setNickname(registerDTO.getNickname());
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.insert(user);
    }

    @Override
    public SysUser findByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public void updateLastLoginTime(String username) {
        userMapper.updateLastLoginTime(username, LocalDateTime.now());
    }
}