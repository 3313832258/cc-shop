package com.ccshop.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ccshop.user.entity.LoginLog;
import com.ccshop.user.mapper.LoginLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginLogService {

    private final LoginLogMapper loginLogMapper;

    /**
     * 记录登录日志
     */
    public void record(Long userId, String username, String loginType, boolean success,
                       String failReason, String ip, String userAgent) {
        LoginLog log = new LoginLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setLoginType(loginType);
        log.setStatus(success ? 1 : 0);
        log.setFailReason(failReason);
        log.setIp(ip);
        log.setUserAgent(userAgent != null && userAgent.length() > 500 ? userAgent.substring(0, 500) : userAgent);
        loginLogMapper.insert(log);
    }

    /**
     * 查询用户最近 20 条登录日志
     */
    public List<LoginLog> queryByUserId(Long userId) {
        LambdaQueryWrapper<LoginLog> qw = new LambdaQueryWrapper<>();
        qw.eq(LoginLog::getUserId, userId)
          .orderByDesc(LoginLog::getCreatedAt)
          .last("LIMIT 20");
        return loginLogMapper.selectList(qw);
    }
}
