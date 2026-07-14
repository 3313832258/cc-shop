package com.ccshop.user.controller;

import com.ccshop.common.core.Result;
import com.ccshop.common.core.UserContext;
import com.ccshop.user.dto.UserVO;
import com.ccshop.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/profile")
@RequiredArgsConstructor
@Tag(name = "用户信息")
public class UserController {

    private final UserService userService;

    @GetMapping
    public Result<UserVO> me() {
        return Result.success(userService.getProfile(UserContext.getUserId()));
    }

    @PutMapping("/avatar")
    public Result<UserVO> updateAvatar(@RequestParam String avatar) {
        return Result.success(userService.updateAvatar(UserContext.getUserId(), avatar));
    }

    /** 供其他服务 Feign 调用查询用户名 */
    @GetMapping("/username")
    public Result<String> findByUsername(@RequestParam String username) {
        return Result.success(userService.findUsername(username) != null ? username : null);
    }
}
