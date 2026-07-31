package com.edms.api.controller;

import com.edms.api.dto.AuthResponse;
import com.edms.api.dto.LoginRequest;
import com.edms.api.dto.MessageResponse;
import com.edms.api.dto.UserDto;
import com.edms.api.dto.UserMeResponse;
import com.edms.application.service.AuthApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "🔐 Authentication", description = "Đăng nhập, đăng xuất và lấy thông tin tài khoản hiện tại")
public class AuthController {

    private final AuthApplicationService authService;

    public AuthController(AuthApplicationService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(
        summary = "Đăng nhập hệ thống",
        description = "Trả về JWT token để sử dụng cho các API khác. Nhấn **Authorize 🔓** ở trên góc phải để điền token.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "👑 Owner Account", value = "{\"email\":\"owner@edms.vn\",\"password\":\"Password123!\"}"),
                    @ExampleObject(name = "✏️ Editor Account", value = "{\"email\":\"editor@edms.vn\",\"password\":\"Password123!\"}"),
                    @ExampleObject(name = "👀 Viewer Account", value = "{\"email\":\"viewer@edms.vn\",\"password\":\"Password123!\"}"),
                    @ExampleObject(name = "📋 Manager Account", value = "{\"email\":\"manager@edms.vn\",\"password\":\"Password123!\"}"),
                    @ExampleObject(name = "🛡️ Admin Account", value = "{\"email\":\"admin@edms.vn\",\"password\":\"Password123!\"}")
                }
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Đăng nhập thành công, trả về JWT token"),
            @ApiResponse(responseCode = "401", description = "Sai email hoặc mật khẩu")
        }
    )
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(
        summary = "Đăng xuất",
        description = "Đăng xuất tài khoản hiện tại (Stateless - xóa token phía client)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MessageResponse> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        authService.logout(token);
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }

    @GetMapping("/me")
    @Operation(
        summary = "Lấy thông tin tài khoản hiện tại",
        description = "Trả về thông tin người dùng đang đăng nhập dựa trên JWT token",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "401", description = "Token không hợp lệ hoặc hết hạn")
        }
    )
    public ResponseEntity<UserMeResponse> getCurrentUser(@RequestHeader("Authorization") String token) {
        UserDto user = authService.getCurrentUser(token);
        return ResponseEntity.ok(UserMeResponse.builder().user(user).build());
    }
}
