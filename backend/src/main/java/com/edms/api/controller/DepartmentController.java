package com.edms.api.controller;

import com.edms.api.dto.CreateDepartmentRequest;
import com.edms.api.dto.DepartmentDto;
import com.edms.api.dto.UserDto;
import com.edms.api.dto.UserListResponse;
import com.edms.infrastructure.persistence.entity.DepartmentEntity;
import com.edms.infrastructure.persistence.entity.UserEntity;
import com.edms.infrastructure.persistence.repository.DepartmentJpaRepository;
import com.edms.infrastructure.persistence.repository.UserJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/departments")
@Tag(name = "🏢 Departments", description = "Quản lý phòng ban")
@SecurityRequirement(name = "bearerAuth")
public class DepartmentController {

    private final DepartmentJpaRepository departmentRepository;
    private final UserJpaRepository userRepository;

    public DepartmentController(DepartmentJpaRepository departmentRepository, UserJpaRepository userRepository) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách phòng ban")
    public ResponseEntity<List<DepartmentDto>> getDepartments() {
        List<DepartmentDto> dtos = departmentRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    @Operation(summary = "Tạo phòng ban mới (Admin)")
    public ResponseEntity<DepartmentDto> createDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        if (departmentRepository.findByCode(request.getCode()).isPresent()) {
            throw new IllegalArgumentException("Department code already exists");
        }

        DepartmentEntity dept = DepartmentEntity.builder()
                .id(UUID.randomUUID().toString())
                .code(request.getCode())
                .name(request.getName())
                .createdAt(Instant.now())
                .build();

        DepartmentEntity saved = departmentRepository.save(dept);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDto(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật phòng ban (Admin)")
    public ResponseEntity<DepartmentDto> updateDepartment(
            @PathVariable("id") String id,
            @Valid @RequestBody CreateDepartmentRequest request) {
        
        DepartmentEntity dept = departmentRepository.findById(id)
                .orElseThrow(() -> new com.edms.api.exception.ResourceNotFoundException("Department not found"));
        
        dept.setCode(request.getCode());
        dept.setName(request.getName());
        DepartmentEntity saved = departmentRepository.save(dept);
        return ResponseEntity.ok(mapToDto(saved));
    }

    @GetMapping("/{id}/users")
    @Operation(summary = "Lấy danh sách người dùng trong phòng ban")
    public ResponseEntity<UserListResponse> getDepartmentUsers(@PathVariable("id") String id) {
        List<UserEntity> users = userRepository.findByDepartmentId(id);
        List<UserDto> dtos = users.stream().map(u -> UserDto.builder()
                .id(u.getId())
                .email(u.getEmail())
                .name(u.getName())
                .role(u.getRole().name())
                .department(u.getDepartment())
                .avatar(u.getAvatar())
                .build()).collect(Collectors.toList());
        return ResponseEntity.ok(UserListResponse.builder().items(dtos).build());
    }

    private DepartmentDto mapToDto(DepartmentEntity dept) {
        return DepartmentDto.builder()
                .id(dept.getId())
                .code(dept.getCode())
                .name(dept.getName())
                .createdAt(dept.getCreatedAt())
                .build();
    }
}
