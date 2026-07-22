package com.ccshop.trade.controller;

import com.ccshop.common.core.Result;
import com.ccshop.trade.dto.LogisticsVO;
import com.ccshop.trade.service.LogisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/logistics")
@RequiredArgsConstructor
@Tag(name = "物流")
public class LogisticsController {

    private final LogisticsService logisticsService;

    @GetMapping("/{orderId}")
    @Operation(summary = "查询物流信息")
    public Result<LogisticsVO> getLogistics(@PathVariable Long orderId) {
        return Result.success(logisticsService.getLogistics(orderId));
    }
}
