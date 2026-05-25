package com.pms.controller;

import com.pms.dto.response.AnalyticsResponse;
import com.pms.dto.response.ApiResponse;
import com.pms.dto.response.DashboardResponse;
import com.pms.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboardStats() {
        DashboardResponse stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats retrieved successfully.", stats));
    }

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getAnalytics() {
        AnalyticsResponse analytics = dashboardService.getAnalytics();
        return ResponseEntity.ok(ApiResponse.success("Analytics retrieved successfully.", analytics));
    }
}
