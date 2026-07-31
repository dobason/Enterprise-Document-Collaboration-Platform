package com.edms.api.controller;

import com.edms.api.dto.DashboardStatsResponse;
import com.edms.application.service.DashboardApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "📊 Dashboard", description = "Thống kê tổng quan hệ thống EDMS")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardApplicationService dashboardService;

    public DashboardController(DashboardApplicationService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    @Operation(
        summary = "Lấy thống kê tổng quan",
        description = "Trả về các chỉ số thống kê tổng quan: tổng số tài liệu, số đang chờ duyệt, số được phê duyệt trong tháng, phân loại theo phòng ban và trạng thái"
    )
    public ResponseEntity<DashboardStatsResponse> getStats() {
        DashboardStatsResponse stats = dashboardService.getStats();
        return ResponseEntity.ok(stats);
    }
}
