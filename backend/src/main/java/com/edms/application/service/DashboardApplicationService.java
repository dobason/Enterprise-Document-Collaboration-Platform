package com.edms.application.service;

import com.edms.api.dto.DashboardStatsResponse;
import com.edms.api.dto.MyDocumentDto;
import com.edms.api.dto.MyDocumentsResponse;
import com.edms.domain.enums.DocumentStatus;
import com.edms.infrastructure.persistence.entity.DocumentEntity;
import com.edms.infrastructure.persistence.repository.DepartmentJpaRepository;
import com.edms.infrastructure.persistence.repository.DocumentJpaRepository;
import com.edms.infrastructure.persistence.repository.FolderJpaRepository;
import com.edms.infrastructure.persistence.repository.PermissionJpaRepository;
import com.edms.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardApplicationService {

    private final DocumentJpaRepository documentRepository;
    private final DepartmentJpaRepository departmentRepository;
    private final UserJpaRepository userRepository;
    private final FolderJpaRepository folderRepository;
    private final PermissionJpaRepository permissionRepository;

    public DashboardApplicationService(DocumentJpaRepository documentRepository,
                                       DepartmentJpaRepository departmentRepository,
                                       UserJpaRepository userRepository,
                                       FolderJpaRepository folderRepository,
                                       PermissionJpaRepository permissionRepository) {
        this.documentRepository = documentRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.folderRepository = folderRepository;
        this.permissionRepository = permissionRepository;
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
        long totalUsers = userRepository.count();
        long totalFolders = folderRepository.count();

        List<DashboardStatsResponse.DeptCount> docsByDept = new ArrayList<>();
        List<Object[]> deptCounts = documentRepository.countDocumentsByDepartmentRaw();
        for (Object[] row : deptCounts) {
            String deptName = row[0] != null ? (String) row[0] : "Unassigned";
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
                .totalUsers(totalUsers)
                .totalFolders(totalFolders)
                .pendingApprovals(pendingCount)
                .approvedThisMonth(approvedThisMonth)
                .totalDepartments(totalDepartments)
                .docsByDepartment(docsByDept)
                .docsByStatus(docsByStatus)
                .build();
    }

    @Transactional(readOnly = true)
    public MyDocumentsResponse getMyDocuments(String userId) {
        List<DocumentEntity> docs = documentRepository.findAllSharedWithUser(userId);

        List<MyDocumentDto> items = docs.stream()
                .map(doc -> MyDocumentDto.builder()
                        .id(doc.getId())
                        .title(doc.getTitle())
                        .status(doc.getStatus() != null ? doc.getStatus().name() : "UNKNOWN")
                        .folderId(doc.getFolderId())
                        .role(permissionRepository.findByDocumentIdAndUserId(doc.getId(), userId)
                                .map(p -> p.getRole().name())
                                .orElse(null))
                        .createdAt(doc.getCreatedAt())
                        .updatedAt(doc.getUpdatedAt())
                        .build())
                .sorted((a, b) -> {
                    if (a.getUpdatedAt() == null) return 1;
                    if (b.getUpdatedAt() == null) return -1;
                    return b.getUpdatedAt().compareTo(a.getUpdatedAt());
                })
                .collect(Collectors.toList());

        long approved = items.stream().filter(d -> "APPROVED".equals(d.getStatus())).count();
        long pending = items.stream().filter(d -> "PENDING".equals(d.getStatus())).count();
        long rejected = items.stream().filter(d -> "REJECTED".equals(d.getStatus())).count();

        return MyDocumentsResponse.builder()
                .total(items.size())
                .approved(approved)
                .pending(pending)
                .rejected(rejected)
                .items(items)
                .build();
    }
}
