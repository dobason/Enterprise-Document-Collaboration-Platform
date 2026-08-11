package com.edms.application.ports;

import com.edms.api.dto.AuthResponse;
import com.edms.api.dto.LoginRequest;
import com.edms.api.dto.UserDto;

public interface AuthenticationService {
    AuthResponse login(LoginRequest request);
    UserDto getCurrentUser(String token);
    void logout(String token);
}
