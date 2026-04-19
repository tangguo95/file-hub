package com.tydic.filehub.security;

import com.tydic.filehub.dto.SysUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {
    private final String nickname;

    public CustomUserDetails(SysUser user, Collection<? extends GrantedAuthority> authorities) {
        super(user.getUsername(), user.getPassword(),
              user.getStatus() == 1, true, true, true, authorities);
        this.nickname = user.getNickname();
    }
}