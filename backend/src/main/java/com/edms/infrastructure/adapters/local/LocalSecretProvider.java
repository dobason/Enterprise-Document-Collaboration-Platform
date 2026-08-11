package com.edms.infrastructure.adapters.local;

import com.edms.application.ports.SecretProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
@Profile({"local", "mysql", "aws"})
public class LocalSecretProvider implements SecretProvider {

    private final Environment environment;

    public LocalSecretProvider(Environment environment) {
        this.environment = environment;
    }

    @Override
    public String getSecret(String secretName) {
        return environment.getProperty(secretName, "mock_secret_val_" + secretName);
    }
}
