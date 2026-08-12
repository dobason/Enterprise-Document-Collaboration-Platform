package com.edms.infrastructure.config;

import com.edms.domain.enums.ApprovalAction;
import com.edms.domain.enums.AuditAction;
import com.edms.domain.enums.DocumentStatus;
import com.edms.domain.enums.OcrStatus;
import com.edms.domain.enums.PermissionRole;
import com.edms.domain.enums.UserRole;
import com.edms.infrastructure.persistence.entity.ApprovalHistoryEntity;
import com.edms.infrastructure.persistence.entity.AuditLogEntity;
import com.edms.infrastructure.persistence.entity.DepartmentEntity;
import com.edms.infrastructure.persistence.entity.DocumentEntity;
import com.edms.infrastructure.persistence.entity.DocumentTagEntity;
import com.edms.infrastructure.persistence.entity.DocumentVersionEntity;
import com.edms.infrastructure.persistence.entity.FolderEntity;
import com.edms.infrastructure.persistence.entity.OcrResultEntity;
import com.edms.infrastructure.persistence.entity.PermissionEntity;
import com.edms.infrastructure.persistence.entity.ShareEntity;
import com.edms.infrastructure.persistence.entity.TagEntity;
import com.edms.infrastructure.persistence.entity.UserEntity;
import com.edms.infrastructure.persistence.repository.ApprovalHistoryJpaRepository;
import com.edms.infrastructure.persistence.repository.AuditLogJpaRepository;
import com.edms.infrastructure.persistence.repository.DepartmentJpaRepository;
import com.edms.infrastructure.persistence.repository.DocumentJpaRepository;
import com.edms.infrastructure.persistence.repository.DocumentTagJpaRepository;
import com.edms.infrastructure.persistence.repository.DocumentVersionJpaRepository;
import com.edms.infrastructure.persistence.repository.FolderJpaRepository;
import com.edms.infrastructure.persistence.repository.OcrResultJpaRepository;
import com.edms.infrastructure.persistence.repository.PermissionJpaRepository;
import com.edms.infrastructure.persistence.repository.ShareJpaRepository;
import com.edms.infrastructure.persistence.repository.TagJpaRepository;
import com.edms.infrastructure.persistence.repository.UserJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
@Profile({"mysql", "aws"})
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserJpaRepository userRepository;
    private final DepartmentJpaRepository departmentRepository;
    private final FolderJpaRepository folderRepository;
    private final DocumentJpaRepository documentRepository;
    private final DocumentVersionJpaRepository versionRepository;
    private final PermissionJpaRepository permissionRepository;
    private final TagJpaRepository tagRepository;
    private final DocumentTagJpaRepository documentTagRepository;
    private final ShareJpaRepository shareRepository;
    private final ApprovalHistoryJpaRepository approvalHistoryRepository;
    private final OcrResultJpaRepository ocrResultRepository;
    private final AuditLogJpaRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserJpaRepository userRepository,
                      DepartmentJpaRepository departmentRepository,
                      FolderJpaRepository folderRepository,
                      DocumentJpaRepository documentRepository,
                      DocumentVersionJpaRepository versionRepository,
                      PermissionJpaRepository permissionRepository,
                      TagJpaRepository tagRepository,
                      DocumentTagJpaRepository documentTagRepository,
                      ShareJpaRepository shareRepository,
                      ApprovalHistoryJpaRepository approvalHistoryRepository,
                      OcrResultJpaRepository ocrResultRepository,
                      AuditLogJpaRepository auditLogRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.folderRepository = folderRepository;
        this.documentRepository = documentRepository;
        this.versionRepository = versionRepository;
        this.permissionRepository = permissionRepository;
        this.tagRepository = tagRepository;
        this.documentTagRepository = documentTagRepository;
        this.shareRepository = shareRepository;
        this.approvalHistoryRepository = approvalHistoryRepository;
        this.ocrResultRepository = ocrResultRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            log.info("Data already seeded in H2 database.");
            return;
        }

        log.info("Seeding sample data for Swagger UI testing...");

        // 1. Departments
        DepartmentEntity eng = departmentRepository.save(DepartmentEntity.builder().id("dpt1").code("ENG").name("Engineering").createdAt(Instant.now()).build());
        DepartmentEntity hr = departmentRepository.save(DepartmentEntity.builder().id("dpt2").code("HR").name("HR").createdAt(Instant.now()).build());
        DepartmentEntity mgmt = departmentRepository.save(DepartmentEntity.builder().id("dpt3").code("MGMT").name("Management").createdAt(Instant.now()).build());

        // 2. Users (Encrypted password: Password123!)
        String encodedPass = passwordEncoder.encode("Password123!");

        UserEntity owner = userRepository.save(UserEntity.builder()
                .id("u1").email("owner@edms.vn").password(encodedPass).name("Nguyen Van Owner")
                .role(UserRole.VIEWER).department("Engineering").departmentId(eng.getId()).build());

        UserEntity editor = userRepository.save(UserEntity.builder()
                .id("u2").email("editor@edms.vn").password(encodedPass).name("Tran Thi Editor")
                .role(UserRole.VIEWER).department("Engineering").departmentId(eng.getId()).build());

        UserEntity manager = userRepository.save(UserEntity.builder()
                .id("u3").email("manager@edms.vn").password(encodedPass).name("Le Van Manager")
                .role(UserRole.VIEWER).department("Management").departmentId(mgmt.getId()).build());

        UserEntity viewer = userRepository.save(UserEntity.builder()
                .id("u4").email("viewer@edms.vn").password(encodedPass).name("Pham Van Viewer")
                .role(UserRole.VIEWER).department("HR").departmentId(hr.getId()).build());

        UserEntity admin = userRepository.save(UserEntity.builder()
                .id("u5").email("admin@edms.vn").password(encodedPass).name("System Admin")
                .role(UserRole.ADMIN).department("Management").departmentId(mgmt.getId()).build());

        // 3. Folders
        FolderEntity f1 = folderRepository.save(FolderEntity.builder()
                .id("f1").name("Technical Contracts").department("Engineering").ownerId("u1").createdAt(Instant.now()).updatedAt(Instant.now()).build());

        FolderEntity f2 = folderRepository.save(FolderEntity.builder()
                .id("f2").name("HR Policies").department("HR").ownerId("u4").createdAt(Instant.now()).updatedAt(Instant.now()).build());

        // 4. Documents & Versions
        DocumentEntity doc1 = documentRepository.save(DocumentEntity.builder()
                .id("d1").title("Software Architecture Specification").type("Architecture Specification")
                .status(DocumentStatus.APPROVED).ownerId("u1").folderId("f1").content("{\"architect\":\"Clean Architecture\"}")
                .currentVersionId("v1").createdAt(Instant.now().minus(2, ChronoUnit.DAYS)).updatedAt(Instant.now()).build());

        versionRepository.save(DocumentVersionEntity.builder()
                .id("v1").documentId("d1").versionNumber(1).content("{\"architect\":\"Clean Architecture\"}").createdBy("u1").createdAt(Instant.now()).build());

        DocumentEntity doc2 = documentRepository.save(DocumentEntity.builder()
                .id("d2").title("Q3 Engineering Budget Draft").type("Financial Budget")
                .status(DocumentStatus.PENDING).ownerId("u2").folderId("f1").content("{\"total\":50000}")
                .currentVersionId("v2").createdAt(Instant.now().minus(1, ChronoUnit.DAYS)).updatedAt(Instant.now()).build());

        versionRepository.save(DocumentVersionEntity.builder()
                .id("v2").documentId("d2").versionNumber(1).content("{\"total\":50000}").createdBy("u2").createdAt(Instant.now()).build());

        // 5. Permissions
        permissionRepository.save(PermissionEntity.builder().id("p1").documentId("d1").userId("u1").role(PermissionRole.OWNER).createdAt(Instant.now()).build());
        permissionRepository.save(PermissionEntity.builder().id("p2").documentId("d1").userId("u2").role(PermissionRole.EDITOR).createdAt(Instant.now()).build());
        permissionRepository.save(PermissionEntity.builder().id("p3").documentId("d1").userId("u4").role(PermissionRole.VIEWER).createdAt(Instant.now()).build());

        // 6. Tags & DocTags
        TagEntity tagConf = tagRepository.save(TagEntity.builder().id("t1").name("Confidential").build());
        TagEntity tagTech = tagRepository.save(TagEntity.builder().id("t2").name("Technical").build());

        documentTagRepository.save(DocumentTagEntity.builder().id("dt1").documentId("d1").tagId("t1").createdAt(Instant.now()).build());
        documentTagRepository.save(DocumentTagEntity.builder().id("dt2").documentId("d1").tagId("t2").createdAt(Instant.now()).build());

        // 7. Approval Histories
        approvalHistoryRepository.save(ApprovalHistoryEntity.builder()
                .id(UUID.randomUUID().toString()).documentId("d1").action(ApprovalAction.SUBMIT)
                .fromStatus(DocumentStatus.DRAFT).toStatus(DocumentStatus.PENDING).performedBy("u1").timestamp(Instant.now().minus(1, ChronoUnit.DAYS)).build());

        approvalHistoryRepository.save(ApprovalHistoryEntity.builder()
                .id(UUID.randomUUID().toString()).documentId("d1").action(ApprovalAction.APPROVE)
                .fromStatus(DocumentStatus.PENDING).toStatus(DocumentStatus.APPROVED).performedBy("u3").timestamp(Instant.now()).build());

        // 8. OCR Results
        ocrResultRepository.save(OcrResultEntity.builder()
                .id("ocr1").documentId("d1").status(OcrStatus.COMPLETED)
                .text("ENTERPRISE DOCUMENT COLLABORATION PLATFORM ARCHITECTURE SPECIFICATION v1.0")
                .extractedAt(Instant.now()).build());

        // 9. Shares
        shareRepository.save(ShareEntity.builder()
                .id("s1").documentId("d1").sharedBy("u1").sharedWithEmail("external.partner@client.com")
                .token("share-token-123456").expiresAt(Instant.now().plus(7, ChronoUnit.DAYS)).createdAt(Instant.now()).build());

        // 10. Audit Logs
        auditLogRepository.save(AuditLogEntity.builder()
                .id("al1").documentId("d1").action(AuditAction.UPLOAD).performedBy("u1").details("Uploaded initial document").timestamp(Instant.now().minus(2, ChronoUnit.DAYS)).build());

        log.info("✅ Sample data successfully preloaded into local H2 database!");
    }
}
