package com.ccshop.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String refreshToken;
    private String deviceId;
    private Long userId;
    private String username;
    private Integer role;

    public LoginResponse(String token, String refreshToken, Long userId, String username, Integer role) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.username = username;
        this.role = role;
    }
}
