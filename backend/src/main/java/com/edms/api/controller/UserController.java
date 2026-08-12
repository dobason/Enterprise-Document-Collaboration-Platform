package com.edms.api.controller;

import com.edms.api.dto.UserDto;
import com.edms.api.dto.UserListResponse;
import com.edms.infrastructure.persistence.entity.UserEntity;
import com.edms.infrastructure.persistence.entity.DepartmentEntity;
import com.edms.infrastructure.persistence.repository.UserJpaRepository;
import com.edms.infrastructure.persistence.repository.DepartmentJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@Tag(name = "👥 Users", description = "Danh bạ người dùng hệ thống")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserJpaRepository userRepository;
    private final DepartmentJpaRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserJpaRepository userRepository, DepartmentJpaRepository departmentRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    @Operation(
        summary = "Lấy danh sách người dùng",
        description = "Trả về danh bạ toàn bộ người dùng đã đăng ký trong hệ thống"
    )
    public ResponseEntity<UserListResponse> getUsers() {
        List<UserDto> items = userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(UserListResponse.builder().items(items).build());
    }

    @PostMapping
    @Operation(
        summary = "Tạo mới người dùng (Admin)",
        description = "Admin tạo tài khoản mới cho người dùng. Mật khẩu mặc định là Password123!"
    )
    public ResponseEntity<UserDto> createUser(@RequestBody Map<String, String> body) {
        if (userRepository.existsByEmail(body.get("email"))) {
            throw new IllegalArgumentException("Email is already registered");
        }

        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID().toString())
                .email(body.get("email"))
                .name(body.get("name"))
                .password(passwordEncoder.encode("Password123!"))
                .role(com.edms.domain.enums.UserRole.valueOf(body.getOrDefault("role", "VIEWER")))
                .createdAt(java.time.Instant.now())
                .updatedAt(java.time.Instant.now())
                .build();

        if (body.containsKey("departmentId") && !body.get("departmentId").isEmpty()) {
            departmentRepository.findById(body.get("departmentId")).ifPresent(dept -> {
                user.setDepartment(dept.getName());
                user.setDepartmentId(dept.getId());
            });
        }

        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDto(user));
    }

    @PutMapping("/{id}/role")
    @Operation(
        summary = "Cập nhật quyền của người dùng (Admin)",
        description = "Chỉ định Role mới (ADMIN, USER) cho người dùng."
    )
    public ResponseEntity<UserDto> updateUserRole(
            @PathVariable("id") String id,
            @RequestBody Map<String, String> body) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new com.edms.api.exception.ResourceNotFoundException("User not found"));
        
        String roleStr = body.get("role");
        if (roleStr != null) {
            user.setRole(com.edms.domain.enums.UserRole.valueOf(roleStr));
            user.setUpdatedAt(java.time.Instant.now());
            userRepository.save(user);
        }
        return ResponseEntity.ok(mapToDto(user));
    }

    @PutMapping("/{id}/department")
    @Operation(
        summary = "Cập nhật phòng ban của người dùng (Admin)",
        description = "Chỉ định phòng ban mới cho người dùng."
    )
    public ResponseEntity<UserDto> updateUserDepartment(
            @PathVariable("id") String id,
            @RequestBody Map<String, String> body) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new com.edms.api.exception.ResourceNotFoundException("User not found"));
        
        String deptId = body.get("departmentId");
        if (deptId != null && !deptId.isEmpty()) {
            departmentRepository.findById(deptId).ifPresent(dept -> {
                user.setDepartment(dept.getName());
                user.setDepartmentId(dept.getId());
                user.setUpdatedAt(java.time.Instant.now());
                userRepository.save(user);
            });
        } else if (body.containsKey("departmentId")) {
            user.setDepartment(null);
            user.setDepartmentId(null);
            user.setUpdatedAt(java.time.Instant.now());
            userRepository.save(user);
        }
        
        return ResponseEntity.ok(mapToDto(user));
    }


    private UserDto mapToDto(UserEntity user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .department(user.getDepartment())
                .avatar(user.getAvatar())
                .build();
    }
}
