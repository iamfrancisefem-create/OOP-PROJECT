package com.pms.service;

import com.pms.dto.response.AnalyticsResponse;
import com.pms.dto.response.DashboardResponse;

// OOP — ABSTRACTION: Contract for dashboard stats and analytics
public interface DashboardService {
    DashboardResponse getDashboardStats();
    AnalyticsResponse getAnalytics();
}
