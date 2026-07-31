package com.edms.infrastructure.adapters.local;

import com.edms.api.dto.AuthResponse;
import com.edms.api.dto.LoginRequest;
import com.edms.api.dto.UserDto;
import com.edms.api.exception.UnauthorizedException;
import com.edms.application.ports.AuthenticationService;
import com.edms.infrastructure.persistence.entity.UserEntity;
import com.edms.infrastructure.persistence.repository.UserJpaRepository;
import com.edms.infrastructure.security.JwtTokenProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Profile({"local", "mysql"})
public class LocalAuthenticationService implements AuthenticationService {

    private final UserJpaRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public LocalAuthenticationService(UserJpaRepository userRepository,
                                      JwtTokenProvider jwtTokenProvider,
                                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        // Match password with BCrypt or fallback to plain match if hashed with default seed
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()) &&
            !request.getPassword().equals(user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .department(user.getDepartment())
                .avatar(user.getAvatar())
                .build();

        return AuthResponse.builder()
                .token(token)
                .user(userDto)
                .build();
    }

    @Override
    public UserDto getCurrentUser(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (!jwtTokenProvider.validateToken(token)) {
            throw new UnauthorizedException("Invalid token");
        }
        String userId = jwtTokenProvider.getUserIdFromToken(token);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .department(user.getDepartment())
                .avatar(user.getAvatar())
                .build();
    }

    @Override
    public void logout(String token) {
        // Local stateless logout
    }
}
