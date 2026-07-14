package com.ccshop.user.service;

import com.ccshop.common.tracker.UserActionEvent;
import com.ccshop.user.entity.UserActionLog;
import com.ccshop.user.mapper.UserActionLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionLogService {

    private final UserActionLogMapper logMapper;

    public void log(UserActionEvent event) {
        try {
            UserActionLog l = new UserActionLog();
            l.setUserId(event.getUserId());
            l.setAction(event.getAction());
            l.setTargetType(event.getTargetType());
            l.setTargetId(event.getTargetId());
            l.setExtra(event.getExtra());
            logMapper.insert(l);
        } catch (Exception e) {
            log.warn("埋点落库失败: {}", e.getMessage());
        }
    }
}
