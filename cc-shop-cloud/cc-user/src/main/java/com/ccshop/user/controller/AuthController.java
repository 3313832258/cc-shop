package com.ccshop.user.controller;

import com.ccshop.common.core.Result;
import com.ccshop.user.dto.LoginRequest;
import com.ccshop.user.dto.LoginResponse;
import com.ccshop.user.dto.RegisterRequest;
import com.ccshop.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/auth")
@RequiredArgsConstructor
@Tag(name = "认证")
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "注册")
    public Result<Long> register(@Valid @RequestBody RegisterRequest req) {
        return Result.success(userService.register(req));
    }

    @PostMapping("/login")
    @Operation(summary = "登录")
    public Result<LoginResponse> login(@RequestBody LoginRequest req) {
        return Result.success(userService.login(req));
    }
}
