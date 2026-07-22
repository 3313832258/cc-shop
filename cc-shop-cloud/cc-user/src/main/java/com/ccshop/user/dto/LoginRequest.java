package com.ccshop.user.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String phone;
    private String password;
    private String captchaId;
    private String captchaAnswer;
}
