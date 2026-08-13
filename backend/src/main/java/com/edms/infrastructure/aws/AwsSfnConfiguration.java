package com.edms.infrastructure.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sfn.SfnClient;

@Configuration
@Profile({"aws"})
public class AwsSfnConfiguration {

    private AwsCredentialsProvider credentialsProvider(String accessKey, String secretKey) {
        String sessionToken = System.getenv("AWS_SESSION_TOKEN");
        boolean hasSession = sessionToken != null && !sessionToken.isBlank();
        if (!hasSession && accessKey != null && !accessKey.isBlank()) {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
        }
        return DefaultCredentialsProvider.create();
    }

    @Bean
    public SfnClient sfnClient(
            @Value("${aws.region:ap-southeast-1}") String region,
            @Value("${aws.stepfunctions.access-key:}") String accessKey,
            @Value("${aws.stepfunctions.secret-key:}") String secretKey) {
        return SfnClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider(accessKey, secretKey))
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .build();
    }
}
