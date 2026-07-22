package com.ccshop.admin.controller;

import com.ccshop.common.core.BusinessException;
import com.ccshop.common.core.Result;
import com.ccshop.common.jwt.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
@Tag(name = "管理后台 - 认证")
public class AdminAuthController {

    private final JdbcTemplate jdbcTemplate;

    @PostMapping("/login")
    @Operation(summary = "管理后台登录（商家/管理员）")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> req) {
        String username = req.get("username");
        String password = req.get("password");

        if (username == null || password == null) {
            throw new BusinessException(400, "用户名和密码不能为空");
        }

        // 查用户
        var users = jdbcTemplate.queryForList(
                "SELECT id, username, password_hash, role FROM user WHERE username = ?", username);
        if (users.isEmpty()) {
            throw new BusinessException(400, "用户不存在");
        }

        var user = users.get(0);
        String passwordHash = (String) user.get("password_hash");
        Integer role = (Integer) user.get("role");

        // 校验密码（Hutool BCrypt）
        if (!cn.hutool.crypto.digest.BCrypt.checkpw(password, passwordHash)) {
            throw new BusinessException(400, "密码错误");
        }

        // 校验角色
        if (role == null || role < 1) {
            throw new BusinessException(403, "权限不足，需要商家或管理员身份");
        }

        Long userId = ((Number) user.get("id")).longValue();
        String token = JwtUtil.generate(userId, username, role);

        return Result.success(Map.of(
                "token", token,
                "userId", userId,
                "username", username,
                "role", role
        ));
    }
}
