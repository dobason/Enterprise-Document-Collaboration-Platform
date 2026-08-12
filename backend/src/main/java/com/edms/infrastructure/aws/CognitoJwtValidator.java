package com.edms.infrastructure.aws;

import com.edms.infrastructure.security.TokenClaims;
import com.edms.infrastructure.security.TokenValidator;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@Profile({"mysql", "aws"})
public class CognitoJwtValidator implements TokenValidator {

    private static final Logger log = LoggerFactory.getLogger(CognitoJwtValidator.class);

    private final String userPoolId;
    private final String clientId;
    private final String issuer;
    private volatile ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

    public CognitoJwtValidator(
            @Value("${aws.cognito.user-pool-id:}") String userPoolId,
            @Value("${aws.cognito.client-id:}") String clientId,
            @Value("${aws.cognito.region:${aws.region:ap-southeast-1}}") String region) {
        this.userPoolId = userPoolId;
        this.clientId = clientId;
        this.issuer = "https://cognito-idp.%s.amazonaws.com/%s".formatted(region, userPoolId);
    }

    private ConfigurableJWTProcessor<SecurityContext> getProcessor() {
        if (jwtProcessor == null) {
            synchronized (this) {
                if (jwtProcessor == null) {
                    jwtProcessor = buildProcessor();
                }
            }
        }
        return jwtProcessor;
    }

    private ConfigurableJWTProcessor<SecurityContext> buildProcessor() {
        try {
            if (userPoolId == null || userPoolId.isBlank()) {
                throw new IllegalStateException("COGNITO_USER_POOL_ID chưa được cấu hình trong .env");
            }

            JWKSource<SecurityContext> keySource = new ImmutableJWKSet<>(
                    JWKSet.load(URI.create(issuer + "/.well-known/jwks.json").toURL()));
            JWSKeySelector<SecurityContext> keySelector =
                    new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keySource);

            ConfigurableJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
            processor.setJWSKeySelector(keySelector);

            JWTClaimsSet exactMatch = new JWTClaimsSet.Builder()
                    .issuer(issuer)
                    .audience(clientId)
                    .build();
            Set<String> requiredClaims = new HashSet<>(Set.of("sub", "exp", "iss", "aud"));
            processor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(exactMatch, requiredClaims));

            return processor;
        } catch (Exception e) {
            log.error("Không thể tải JWKS của Cognito User Pool (issuer={})", issuer, e);
            throw new IllegalStateException("Không thể khởi tạo Cognito JWT validator: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<TokenClaims> validate(String token) {
        try {
            JWTClaimsSet claims = getProcessor().process(token, null);
            String tokenUse = claims.getStringClaim("token_use");
            if (!"id".equals(tokenUse)) {
                return Optional.empty();
            }
            List<String> groups = claims.getStringListClaim("cognito:groups");
            String role = mapRole(groups);
            String department = mapDepartment(groups);
            return Optional.of(new TokenClaims(
                    claims.getSubject(),
                    claims.getStringClaim("email"),
                    role,
                    department));
        } catch (IllegalStateException e) {
            log.warn("Cognito chưa được cấu hình: {}", e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String mapRole(List<String> groups) {
        if (groups != null && groups.contains("ADMIN")) {
            return "ADMIN";
        }
        return "VIEWER";
    }

    private String mapDepartment(List<String> groups) {
        if (groups == null || groups.isEmpty()) {
            return null;
        }
        List<String> departments = List.of("HR", "SALES", "FINANCE", "LEGAL", "MARKETING", "IT_SUPPORT");
        return groups.stream()
                .filter(departments::contains)
                .findFirst()
                .orElse(null);
    }
}
