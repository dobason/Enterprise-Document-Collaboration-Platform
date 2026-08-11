package com.edms.application.service;

import com.edms.api.dto.AuthResponse;
import com.edms.api.dto.LoginRequest;
import com.edms.api.dto.UserDto;
import com.edms.application.ports.AuthenticationService;
import org.springframework.stereotype.Service;

@Service
public class AuthApplicationService {

    private final AuthenticationService authenticationService;

    public AuthApplicationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public AuthResponse login(LoginRequest request) {
        return authenticationService.login(request);
    }

    public UserDto getCurrentUser(String token) {
        return authenticationService.getCurrentUser(token);
    }

    public void logout(String token) {
        authenticationService.logout(token);
    }
}
