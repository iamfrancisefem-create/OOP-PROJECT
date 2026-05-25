package com.pms.service;

import com.pms.dto.response.AnalyticsResponse;
import com.pms.dto.response.DashboardResponse;

public interface DashboardService {
    DashboardResponse getDashboardStats();
    AnalyticsResponse getAnalytics();
}
