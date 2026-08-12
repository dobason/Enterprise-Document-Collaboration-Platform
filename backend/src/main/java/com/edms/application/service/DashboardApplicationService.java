package com.edms.application.service;

import com.edms.api.dto.DashboardStatsResponse;
import com.edms.domain.enums.DocumentStatus;
import com.edms.infrastructure.persistence.repository.DepartmentJpaRepository;
import com.edms.infrastructure.persistence.repository.DocumentJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardApplicationService {

    private final DocumentJpaRepository documentRepository;
    private final DepartmentJpaRepository departmentRepository;

    public DashboardApplicationService(DocumentJpaRepository documentRepository,
                                       DepartmentJpaRepository departmentRepository) {
        this.documentRepository = documentRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        long totalDocs = documentRepository.findAllActive().size();
        long pendingCount = documentRepository.countByStatusAndDeletedAtIsNull(DocumentStatus.PENDING);
        long approvedCount = documentRepository.countByStatusAndDeletedAtIsNull(DocumentStatus.APPROVED);
        long draftCount = documentRepository.countByStatusAndDeletedAtIsNull(DocumentStatus.DRAFT);
        long rejectedCount = documentRepository.countByStatusAndDeletedAtIsNull(DocumentStatus.REJECTED);

        Instant startOfMonth = ZonedDateTime.now(ZoneId.systemDefault())
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).toInstant();
        long approvedThisMonth = documentRepository.countApprovedThisMonth(startOfMonth);

        long totalDepartments = departmentRepository.count();

        List<DashboardStatsResponse.DeptCount> docsByDept = new ArrayList<>();
        List<Object[]> deptCounts = documentRepository.countDocumentsByDepartmentRaw();
        for (Object[] row : deptCounts) {
            String deptName = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            docsByDept.add(new DashboardStatsResponse.DeptCount(deptName, count));
        }

        List<DashboardStatsResponse.StatusCount> docsByStatus = List.of(
                new DashboardStatsResponse.StatusCount("APPROVED", approvedCount),
                new DashboardStatsResponse.StatusCount("PENDING", pendingCount),
                new DashboardStatsResponse.StatusCount("DRAFT", draftCount),
                new DashboardStatsResponse.StatusCount("REJECTED", rejectedCount)
        );

        return DashboardStatsResponse.builder()
                .totalDocuments(totalDocs)
                .pendingApprovals(pendingCount)
                .approvedThisMonth(approvedThisMonth)
                .totalDepartments(totalDepartments)
                .docsByDepartment(docsByDept)
                .docsByStatus(docsByStatus)
                .build();
    }
}
