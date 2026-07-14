package com.ccshop.user.controller;

import com.ccshop.common.core.PageResult;
import com.ccshop.common.core.Result;
import com.ccshop.user.entity.Message;
import com.ccshop.user.service.MessageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/message")
@RequiredArgsConstructor
@Tag(name = "消息通知")
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    public Result<PageResult<Message>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(messageService.list(page, size));
    }

    @GetMapping("/unread")
    public Result<Integer> unreadCount() {
        return Result.success(messageService.unreadCount());
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        messageService.markRead(id);
        return Result.success();
    }

    @PutMapping("/read-all")
    public Result<Void> markAllRead() {
        messageService.markAllRead();
        return Result.success();
    }
}
