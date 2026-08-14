package com.edms.infrastructure.aws;

import com.edms.api.dto.AuthResponse;
import com.edms.api.dto.LoginRequest;
import com.edms.api.dto.UserDto;
import com.edms.api.exception.UnauthorizedException;
import com.edms.application.ports.AuthenticationService;
import com.edms.infrastructure.persistence.entity.UserEntity;
import com.edms.infrastructure.persistence.repository.UserJpaRepository;
import com.edms.infrastructure.security.TokenClaims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChallengeNameType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.RespondToAuthChallengeRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.RespondToAuthChallengeResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@Profile({"mysql", "aws"})
public class CognitoAuthenticationService implements AuthenticationService {

    private final CognitoIdentityProviderClient cognitoClient;
    private final CognitoJwtValidator jwtValidator;
    private final UserJpaRepository userRepository;
    private final String clientId;

    public CognitoAuthenticationService(CognitoIdentityProviderClient cognitoClient,
                                        CognitoJwtValidator jwtValidator,
                                        UserJpaRepository userRepository,
                                        @Value("${aws.cognito.client-id}") String clientId) {
        this.cognitoClient = cognitoClient;
        this.jwtValidator = jwtValidator;
        this.userRepository = userRepository;
        this.clientId = clientId;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Map<String, String> authParams = new HashMap<>();
        authParams.put("USERNAME", request.getEmail());
        authParams.put("PASSWORD", request.getPassword());

        try {
            InitiateAuthResponse response = cognitoClient.initiateAuth(InitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .clientId(clientId)
                    .authParameters(authParams)
                    .build());

            AuthenticationResultType authResult = response.authenticationResult();
            if (authResult == null) {
                // Nếu Cognito trả về challenge (vd NEW_PASSWORD_REQUIRED), tự động đổi mật khẩu
                // sang chính mật khẩu vừa dùng để đăng nhập, rồi lấy token.
                if (response.challengeName() == ChallengeNameType.NEW_PASSWORD_REQUIRED
                        && response.session() != null) {
                    Map<String, String> challengeResponses = new HashMap<>();
                    challengeResponses.put("USERNAME", request.getEmail());
                    challengeResponses.put("NEW_PASSWORD", request.getPassword());
                    RespondToAuthChallengeResponse challengeResp = cognitoClient.respondToAuthChallenge(
                            RespondToAuthChallengeRequest.builder()
                                    .challengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
                                    .clientId(clientId)
                                    .challengeResponses(challengeResponses)
                                    .session(response.session())
                                    .build());
                    authResult = challengeResp.authenticationResult();
                }
                if (authResult == null) {
                    throw new UnauthorizedException("Yêu cầu đăng nhập chưa hoàn tất (có thể cần đổi mật khẩu hoặc MFA)");
                }
            }

            String idToken = authResult.idToken();
            TokenClaims claims = jwtValidator.validate(idToken)
                    .orElseThrow(() -> new UnauthorizedException("Token Cognito không hợp lệ"));

            UserEntity user = syncUser(claims);
            return AuthResponse.builder()
                    .token(idToken)
                    .user(toUserDto(user))
                    .build();
        } catch (NotAuthorizedException | UserNotFoundException e) {
            throw new UnauthorizedException("Sai email hoặc mật khẩu");
        }
    }

    @Override
    public UserDto getCurrentUser(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        TokenClaims claims = jwtValidator.validate(token)
                .orElseThrow(() -> new UnauthorizedException("Token không hợp lệ hoặc đã hết hạn"));
        UserEntity user = syncUser(claims);
        return toUserDto(user);
    }

    @Override
    public void logout(String token) {
        // Cognito stateless - client tự xóa token
    }

    private UserEntity syncUser(TokenClaims claims) {
        return userRepository.findByCognitoSub(claims.subject())
                .orElseGet(() -> userRepository.findByEmail(claims.email())
                        .map(existing -> {
                            existing.setCognitoSub(claims.subject());
                            return userRepository.save(existing);
                        })
                        .orElseGet(() -> userRepository.save(UserEntity.builder()
                                .id(claims.subject())
                                .cognitoSub(claims.subject())
                                .email(claims.email())
                                .password("")
                                .name(claims.email())
                                .role(com.edms.domain.enums.UserRole.valueOf(claims.role()))
                                .department(claims.department())
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build())));
    }

    private UserDto toUserDto(UserEntity user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .department(user.getDepartment())
                .departmentId(user.getDepartmentId())
                .avatar(user.getAvatar())
                .build();
    }
}
