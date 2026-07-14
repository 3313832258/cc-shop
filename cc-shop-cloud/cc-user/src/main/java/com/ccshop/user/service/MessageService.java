package com.ccshop.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ccshop.common.core.PageResult;
import com.ccshop.common.core.UserContext;
import com.ccshop.user.entity.Message;
import com.ccshop.user.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageMapper messageMapper;

    public PageResult<Message> list(int page, int size) {
        LambdaQueryWrapper<Message> qw = new LambdaQueryWrapper<>();
        qw.eq(Message::getUserId, UserContext.getUserId())
          .orderByDesc(Message::getCreatedAt);
        Page<Message> p = messageMapper.selectPage(new Page<>(page, size), qw);
        return new PageResult<>(p.getRecords(), p.getTotal());
    }

    public int unreadCount() {
        LambdaQueryWrapper<Message> qw = new LambdaQueryWrapper<>();
        qw.eq(Message::getUserId, UserContext.getUserId())
          .eq(Message::getIsRead, 0);
        return Math.toIntExact(messageMapper.selectCount(qw));
    }

    public void markRead(Long id) {
        LambdaUpdateWrapper<Message> uw = new LambdaUpdateWrapper<>();
        uw.eq(Message::getId, id)
          .eq(Message::getUserId, UserContext.getUserId())
          .set(Message::getIsRead, 1);
        messageMapper.update(null, uw);
    }

    public void markAllRead() {
        LambdaUpdateWrapper<Message> uw = new LambdaUpdateWrapper<>();
        uw.eq(Message::getUserId, UserContext.getUserId())
          .set(Message::getIsRead, 1);
        messageMapper.update(null, uw);
    }

    public void send(Long userId, String type, String title, String content) {
        Message m = new Message();
        m.setUserId(userId);
        m.setType(type);
        m.setTitle(title);
        m.setContent(content);
        m.setIsRead(0);
        messageMapper.insert(m);
    }
}
