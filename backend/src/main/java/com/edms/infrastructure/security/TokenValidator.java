package com.edms.infrastructure.security;

import java.util.Optional;

public interface TokenValidator {
    Optional<TokenClaims> validate(String token);
}
