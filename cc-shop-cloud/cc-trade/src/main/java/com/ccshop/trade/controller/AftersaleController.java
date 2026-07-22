package com.ccshop.trade.controller;

import com.ccshop.common.core.Result;
import com.ccshop.common.core.UserContext;
import com.ccshop.trade.dto.AftersaleApplyRequest;
import com.ccshop.trade.dto.AftersaleVO;
import com.ccshop.trade.service.AftersaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aftersale")
@RequiredArgsConstructor
@Tag(name = "售后")
public class AftersaleController {

    private final AftersaleService aftersaleService;

    @PostMapping("/apply")
    @Operation(summary = "申请售后")
    public Result<Void> apply(@RequestBody AftersaleApplyRequest req) {
        aftersaleService.apply(UserContext.getUserId(), req);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "售后列表")
    public Result<List<AftersaleVO>> list(@RequestParam(required = false) Long orderId) {
        return Result.success(aftersaleService.getAftersales(UserContext.getUserId(), orderId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "售后详情")
    public Result<AftersaleVO> getAftersale(@PathVariable Long id) {
        return Result.success(aftersaleService.getAftersale(UserContext.getUserId(), id));
    }

    @PutMapping("/cancel/{id}")
    @Operation(summary = "取消售后")
    public Result<Void> cancel(@PathVariable Long id) {
        aftersaleService.cancel(UserContext.getUserId(), id);
        return Result.success();
    }
}
