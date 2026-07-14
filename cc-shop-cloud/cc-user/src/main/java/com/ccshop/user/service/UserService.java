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
import org.springframework.stereotype.Service;

import cn.hutool.crypto.digest.BCrypt;
import org.springframework.beans.BeanUtils;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

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
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.eq(User::getUsername, req.getUsername());
        User user = userMapper.selectOne(qw);
        if (user == null) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        if (!BCrypt.checkpw(req.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        String token = JwtUtil.generate(user.getId(), user.getUsername());
        return new LoginResponse(token, user.getId(), user.getUsername());
    }

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
}
