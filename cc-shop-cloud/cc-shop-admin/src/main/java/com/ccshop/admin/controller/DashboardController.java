package com.ccshop.admin.controller;

import com.ccshop.admin.dto.DashboardVO;
import com.ccshop.admin.service.DashboardService;
import com.ccshop.common.core.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "管理后台 - 仪表盘")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @Operation(summary = "获取统计数据")
    public Result<DashboardVO> getDashboard() {
        return Result.success(dashboardService.getDashboard());
    }
}
