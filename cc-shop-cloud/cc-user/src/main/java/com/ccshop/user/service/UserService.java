package com.ccshop.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ccshop.common.core.BusinessException;
import com.ccshop.common.jwt.JwtUtil;
import com.ccshop.user.dto.LoginRequest;
import com.ccshop.user.dto.LoginResponse;
import com.ccshop.user.dto.RegisterRequest;
import com.ccshop.user.dto.UserVO;
import com.ccshop.user.entity.User;
import com.ccshop.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import cn.hutool.crypto.digest.BCrypt;
import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String LOGIN_FAIL_PREFIX = "login:fail:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh:";
    private static final int MAX_FAIL_COUNT = 5;
    private static final int LOCK_MINUTES = 15;

    public Long register(RegisterRequest req) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.eq(User::getUsername, req.getUsername());
        if (userMapper.selectCount(qw) > 0) {
            throw new BusinessException(400, "用户名已存在");
        }
        if (req.getPhone() != null && !req.getPhone().isBlank()) {
            LambdaQueryWrapper<User> pqw = new LambdaQueryWrapper<>();
            pqw.eq(User::getPhone, req.getPhone());
            if (userMapper.selectCount(pqw) > 0) {
                throw new BusinessException(400, "手机号已被注册");
            }
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPasswordHash(BCrypt.hashpw(req.getPassword()));
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());
        userMapper.insert(user);
        return user.getId();
    }

    public LoginResponse login(LoginRequest req) {
        // 确定登录标识
        String identifier;
        if (req.getPhone() != null && !req.getPhone().isBlank()) {
            identifier = "phone:" + req.getPhone();
        } else {
            identifier = "user:" + req.getUsername();
        }

        // 检查是否被锁定
        checkLoginLock(identifier);

        // 查询用户
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        if (req.getPhone() != null && !req.getPhone().isBlank()) {
            qw.eq(User::getPhone, req.getPhone());
        } else {
            qw.eq(User::getUsername, req.getUsername());
        }
        User user = userMapper.selectOne(qw);
        if (user == null) {
            recordLoginFail(identifier);
            throw new BusinessException(400, "用户不存在，请先注册");
        }
        if (!BCrypt.checkpw(req.getPassword(), user.getPasswordHash())) {
            recordLoginFail(identifier);
            int remaining = getRemainingAttempts(identifier);
            throw new BusinessException(400, "用户名或密码错误，还可尝试" + remaining + "次");
        }

        // 登录成功，清除失败记录
        redisTemplate.delete(LOGIN_FAIL_PREFIX + identifier);

        return buildLoginResponse(user);
    }

    /**
     * 手机号 + 验证码登录（不存在则自动注册）
     */
    public LoginResponse loginByPhone(String phone) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.eq(User::getPhone, phone);
        User user = userMapper.selectOne(qw);

        if (user == null) {
            // 自动注册
            user = new User();
            user.setUsername("phone_" + phone.substring(phone.length() - 4));
            user.setPasswordHash(BCrypt.hashpw("auto_generated"));
            user.setPhone(phone);
            userMapper.insert(user);
        }

        return buildLoginResponse(user);
    }

    /**
     * 刷新 AccessToken（用 RefreshToken 换新 AccessToken）
     */
    public LoginResponse refreshToken(String refreshToken, String deviceId) {
        try {
            Long userId = JwtUtil.getUserId(refreshToken);
            String username = JwtUtil.getUsername(refreshToken);

            // 检查 Redis 中是否存在该 refreshToken
            String redisKey = REFRESH_TOKEN_PREFIX + userId + ":" + deviceId;
            String storedToken = redisTemplate.opsForValue().get(redisKey);
            if (storedToken == null || !storedToken.equals(refreshToken)) {
                throw new BusinessException(401, "RefreshToken 已失效，请重新登录");
            }

            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException(401, "用户不存在");
            }

            int role = user.getRole() != null ? user.getRole() : 0;
            String newAccessToken = JwtUtil.generate(userId, username, role);
            return new LoginResponse(newAccessToken, refreshToken, deviceId, userId, username, role);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(401, "RefreshToken 无效");
        }
    }

    /**
     * 登出当前设备
     */
    public void logout(Long userId, String deviceId) {
        if (deviceId != null && !deviceId.isBlank()) {
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId + ":" + deviceId);
        }
    }

    /**
     * 踢出指定设备
     */
    public void kickDevice(Long userId, String deviceId) {
        String key = REFRESH_TOKEN_PREFIX + userId + ":" + deviceId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.delete(key);
        } else {
            throw new BusinessException(404, "设备不存在或已下线");
        }
    }

    /**
     * 查询用户所有活跃设备
     */
    public List<Map<String, Object>> listDevices(Long userId) {
        Set<String> keys = redisTemplate.keys(REFRESH_TOKEN_PREFIX + userId + ":*");
        List<Map<String, Object>> devices = new ArrayList<>();
        if (keys == null) return devices;

        for (String key : keys) {
            String deviceId = key.substring(key.lastIndexOf(":") + 1);
            String token = redisTemplate.opsForValue().get(key);
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            Map<String, Object> info = new HashMap<>();
            info.put("deviceId", deviceId);
            info.put("expiresIn", ttl != null ? ttl : 0);
            // 从 token 中提取登录时间
            if (token != null) {
                try {
                    var claims = JwtUtil.parse(token);
                    info.put("loginTime", claims.getIssuedAt());
                } catch (Exception ignored) {}
            }
            devices.add(info);
        }
        return devices;
    }

    /**
     * 构建登录响应（生成 AccessToken + RefreshToken，存储 RefreshToken 到 Redis）
     */
    private LoginResponse buildLoginResponse(User user) {
        int role = user.getRole() != null ? user.getRole() : 0;
        String accessToken = JwtUtil.generate(user.getId(), user.getUsername(), role);
        String refreshToken = JwtUtil.generate(user.getId(), user.getUsername(), role, JwtUtil.REFRESH_EXPIRATION_MS);
        String deviceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        // 存储 refreshToken 到 Redis（7天过期），key 带 deviceId 支持多设备
        String redisKey = REFRESH_TOKEN_PREFIX + user.getId() + ":" + deviceId;
        redisTemplate.opsForValue().set(redisKey, refreshToken, 7, TimeUnit.DAYS);

        return new LoginResponse(accessToken, refreshToken, deviceId, user.getId(), user.getUsername(), role);
    }

    /**
     * 重置密码（忘记密码）
     */
    public void resetPassword(String phone, String newPassword) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.eq(User::getPhone, phone);
        User user = userMapper.selectOne(qw);
        if (user == null) {
            throw new BusinessException(400, "该手机号未注册");
        }
        user.setPasswordHash(BCrypt.hashpw(newPassword));
        userMapper.updateById(user);
        // 清除登录失败记录
        redisTemplate.delete(LOGIN_FAIL_PREFIX + "phone:" + phone);
        redisTemplate.delete(LOGIN_FAIL_PREFIX + "user:" + user.getUsername());
    }

    // ========== 登录限制 ==========

    private void checkLoginLock(String identifier) {
        String key = LOGIN_FAIL_PREFIX + identifier;
        String countStr = redisTemplate.opsForValue().get(key);
        if (countStr != null) {
            int count = Integer.parseInt(countStr);
            if (count >= MAX_FAIL_COUNT) {
                Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                long minutes = ttl != null ? (ttl / 60) + 1 : LOCK_MINUTES;
                throw new BusinessException(423, "账号已锁定，请" + minutes + "分钟后再试");
            }
        }
    }

    private void recordLoginFail(String identifier) {
        String key = LOGIN_FAIL_PREFIX + identifier;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, LOCK_MINUTES, TimeUnit.MINUTES);
        }
    }

    private int getRemainingAttempts(String identifier) {
        String key = LOGIN_FAIL_PREFIX + identifier;
        String countStr = redisTemplate.opsForValue().get(key);
        if (countStr == null) return MAX_FAIL_COUNT;
        int count = Integer.parseInt(countStr);
        return Math.max(0, MAX_FAIL_COUNT - count);
    }

    // ========== 其他方法 ==========

    public UserVO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    public UserVO updateAvatar(Long userId, String avatar) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setAvatar(avatar);
        userMapper.updateById(user);
        return getProfile(userId);
    }

    public User findUsername(String username) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.eq(User::getUsername, username);
        return userMapper.selectOne(qw);
    }

    public Long createMerchant(String username, String password, String phone) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.eq(User::getUsername, username);
        if (userMapper.selectCount(qw) > 0) {
            throw new BusinessException(400, "用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(BCrypt.hashpw(password));
        user.setPhone(phone);
        user.setRole(1);
        userMapper.insert(user);
        return user.getId();
    }

    public java.util.List<User> listMerchants() {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.eq(User::getRole, 1).orderByDesc(User::getCreatedAt);
        return userMapper.selectList(qw);
    }
}
