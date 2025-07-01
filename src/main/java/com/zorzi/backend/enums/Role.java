package com.zorzi.backend.enums;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    PLAYER,
    ADMIN;

    @Override
    public String getAuthority() {
        return name();
    }
}
