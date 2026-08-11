package com.edms.api.controller;

import com.edms.api.dto.UserDto;
import com.edms.api.dto.UserListResponse;
import com.edms.infrastructure.persistence.entity.UserEntity;
import com.edms.infrastructure.persistence.repository.UserJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@Tag(name = "👥 Users", description = "Danh bạ người dùng hệ thống")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserJpaRepository userRepository;

    public UserController(UserJpaRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    @Operation(
        summary = "Lấy danh sách người dùng",
        description = "Trả về danh bạ toàn bộ người dùng đã đăng ký trong hệ thống (dùng cho việc cấp quyền tài liệu)"
    )
    public ResponseEntity<UserListResponse> getUsers() {
        List<UserDto> items = userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(UserListResponse.builder().items(items).build());
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
