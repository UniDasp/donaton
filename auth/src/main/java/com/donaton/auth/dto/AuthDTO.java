package com.donaton.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthDTO {
    private String email;
    private String password;

    public String getEmail() {
        return "";
    }

    public String getPassword() {
        return "";
    }
}