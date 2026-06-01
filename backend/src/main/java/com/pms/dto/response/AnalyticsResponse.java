package com.pms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// OOP — ENCAPSULATION: private fields with Lombok @Data — data carrier for API layer
public class AnalyticsResponse {
    private List<Map<String, Object>> teamPerformance;
    private List<Map<String, Object>> recentActivities;
    private List<Map<String, Object>> workloadDistribution;
}
