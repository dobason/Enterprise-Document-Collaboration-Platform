package com.edms.infrastructure.security;

public record TokenClaims(String subject, String email, String role, String department) {
}
