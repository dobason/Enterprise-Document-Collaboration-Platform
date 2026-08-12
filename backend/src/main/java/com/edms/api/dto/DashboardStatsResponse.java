package com.edms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalDocuments;
    private long totalUsers;
    private long totalFolders;
    private long pendingApprovals;
    private long approvedThisMonth;
    private long totalDepartments;
    private List<DeptCount> docsByDepartment;
    private List<StatusCount> docsByStatus;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeptCount {
        private String name;
        private long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusCount {
        private String status;
        private long count;
    }
}
