package com.edms.application.ports;

public interface SecretProvider {
    String getSecret(String secretName);
}
