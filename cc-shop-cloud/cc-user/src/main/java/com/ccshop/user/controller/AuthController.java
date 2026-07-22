package com.ccshop.user.controller;

import com.ccshop.common.core.Constants;
import com.ccshop.common.core.Result;
import com.ccshop.common.core.UserContext;
import com.ccshop.user.dto.LoginRequest;
import com.ccshop.user.dto.LoginResponse;
import com.ccshop.user.dto.RegisterRequest;
import com.ccshop.user.entity.LoginLog;
import com.ccshop.user.entity.User;
import com.ccshop.user.service.CaptchaService;
import com.ccshop.user.service.LoginLogService;
import com.ccshop.user.service.SmsService;
import com.ccshop.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "认证")
public class AuthController {

    private final UserService userService;
    private final SmsService smsService;
    private final CaptchaService captchaService;
    private final LoginLogService loginLogService;

    @GetMapping("/auth/captcha")
    @Operation(summary = "获取数学验证码")
    public Result<Map<String, String>> captcha() {
        return Result.success(captchaService.generate());
    }

    @PostMapping("/auth/register")
    @Operation(summary = "注册")
    public Result<Long> register(@Valid @RequestBody RegisterRequest req) {
        captchaService.verify(req.getCaptchaId(), req.getCaptchaAnswer());
        return Result.success(userService.register(req));
    }

    @PostMapping("/auth/login")
    @Operation(summary = "登录（用户名/手机号 + 密码）")
    public Result<LoginResponse> login(@RequestBody LoginRequest req, HttpServletRequest request) {
        captchaService.verify(req.getCaptchaId(), req.getCaptchaAnswer());
        String ip = getClientIp(request);
        String ua = request.getHeader("User-Agent");
        String identifier = req.getPhone() != null && !req.getPhone().isBlank() ? req.getPhone() : req.getUsername();
        try {
            LoginResponse resp = userService.login(req);
            loginLogService.record(resp.getUserId(), identifier, "password", true, null, ip, ua);
            return Result.success(resp);
        } catch (Exception e) {
            loginLogService.record(null, identifier, "password", false, e.getMessage(), ip, ua);
            throw e;
        }
    }

    @PostMapping("/auth/sms/send")
    @Operation(summary = "发送登录验证码")
    public Result<Void> sendSmsCode(@RequestBody Map<String, String> req) {
        // 校验验证码
        captchaService.verify(req.get("captchaId"), req.get("captchaAnswer"));
        String phone = req.get("phone");
        if (phone == null || phone.isBlank()) {
            return Result.fail(400, "手机号不能为空");
        }
        smsService.sendLoginCode(phone);
        return Result.success();
    }

    @PostMapping("/auth/sms/login")
    @Operation(summary = "手机号 + 验证码登录")
    public Result<LoginResponse> smsLogin(@RequestBody Map<String, String> req, HttpServletRequest request) {
        String phone = req.get("phone");
        String code = req.get("code");
        if (phone == null || phone.isBlank() || code == null || code.isBlank()) {
            return Result.fail(400, "手机号和验证码不能为空");
        }
        smsService.verifyCode(phone, code);
        String ip = getClientIp(request);
        String ua = request.getHeader("User-Agent");
        try {
            LoginResponse resp = userService.loginByPhone(phone);
            loginLogService.record(resp.getUserId(), phone, "sms", true, null, ip, ua);
            return Result.success(resp);
        } catch (Exception e) {
            loginLogService.record(null, phone, "sms", false, e.getMessage(), ip, ua);
            throw e;
        }
    }

    @PostMapping("/auth/reset-password")
    @Operation(summary = "忘记密码 - 重置密码")
    public Result<Void> resetPassword(@RequestBody Map<String, String> req) {
        String phone = req.get("phone");
        String code = req.get("code");
        String newPassword = req.get("newPassword");
        if (phone == null || phone.isBlank() || code == null || code.isBlank()
                || newPassword == null || newPassword.isBlank()) {
            return Result.fail(400, "手机号、验证码和新密码不能为空");
        }
        if (newPassword.length() < 6) {
            return Result.fail(400, "密码长度不能少于6位");
        }
        smsService.verifyCode(phone, code);
        userService.resetPassword(phone, newPassword);
        return Result.success();
    }

    @Operation(summary = "刷新 AccessToken")
    @PostMapping("/auth/refresh")
    public Result<LoginResponse> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        String deviceId = body.get("deviceId");
        if (refreshToken == null || refreshToken.isBlank()) {
            return Result.fail("refreshToken 不能为空");
        }
        if (deviceId == null || deviceId.isBlank()) {
            return Result.fail("deviceId 不能为空");
        }
        return Result.success(userService.refreshToken(refreshToken, deviceId));
    }

    @Operation(summary = "登出")
    @PostMapping("/auth/logout")
    public Result<Void> logout(@RequestBody(required = false) Map<String, String> body) {
        Long userId = UserContext.getUserId();
        String deviceId = body != null ? body.get("deviceId") : null;
        if (userId != null) {
            userService.logout(userId, deviceId);
        }
        return Result.success();
    }

    @Operation(summary = "查询活跃设备列表")
    @GetMapping("/auth/devices")
    public Result<List<Map<String, Object>>> devices() {
        Long userId = UserContext.getUserId();
        if (userId == null) return Result.fail("未登录");
        return Result.success(userService.listDevices(userId));
    }

    @Operation(summary = "踢出指定设备")
    @DeleteMapping("/auth/devices/{deviceId}")
    public Result<Void> kickDevice(@PathVariable String deviceId) {
        Long userId = UserContext.getUserId();
        if (userId == null) return Result.fail("未登录");
        userService.kickDevice(userId, deviceId);
        return Result.success();
    }

    @Operation(summary = "查询当前用户登录日志")
    @GetMapping("/auth/login-logs")
    public Result<List<LoginLog>> loginLogs() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.fail("未登录");
        }
        return Result.success(loginLogService.queryByUserId(userId));
    }

    @PostMapping("/admin/merchant")
    @Operation(summary = "创建商家账号（管理员）")
    public Result<Long> createMerchant(@RequestBody Map<String, String> req, HttpServletRequest request) {
        checkAdmin(request);
        Long id = userService.createMerchant(req.get("username"), req.get("password"), req.get("phone"));
        return Result.success(id);
    }

    @GetMapping("/admin/merchants")
    @Operation(summary = "商家列表（管理员）")
    public Result<List<Map<String, Object>>> listMerchants(HttpServletRequest request) {
        checkAdmin(request);
        List<User> merchants = userService.listMerchants();
        List<Map<String, Object>> result = merchants.stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("phone", u.getPhone());
            m.put("email", u.getEmail());
            m.put("createdAt", u.getCreatedAt());
            return m;
        }).toList();
        return Result.success(result);
    }

    private void checkAdmin(HttpServletRequest request) {
        String roleStr = request.getHeader(Constants.HEADER_ROLE);
        int role = roleStr != null ? Integer.parseInt(roleStr) : 0;
        if (role < 2) {
            throw new com.ccshop.common.core.BusinessException(403, "权限不足，需要管理员身份");
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
