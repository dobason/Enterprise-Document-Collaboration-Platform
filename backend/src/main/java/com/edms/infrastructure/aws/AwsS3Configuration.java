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
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@Profile({"mysql","aws"})
public class AwsS3Configuration {

    // Ưu tiên DefaultCredentialsProvider (IAM Role / Lambda runtime) để có session token.
    // Chỉ dùng static credentials khi thật sự cấu hình access-key ở môi trường local
    // và KHÔNG có AWS_SESSION_TOKEN (Lambda luôn có biến này).
    private AwsCredentialsProvider credentialsProvider(String accessKey, String secretKey) {
        String sessionToken = System.getenv("AWS_SESSION_TOKEN");
        boolean hasSession = sessionToken != null && !sessionToken.isBlank();
        if (!hasSession && accessKey != null && !accessKey.isBlank()) {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
        }
        return DefaultCredentialsProvider.create();
    }

    @Bean
    public AwsCredentialsProvider s3CredentialsProvider(
            @Value("${aws.s3.access-key:}") String accessKey,
            @Value("${aws.s3.secret-key:}") String secretKey) {
        return credentialsProvider(accessKey, secretKey);
    }

    @Bean
    public S3Client s3Client(
            @Value("${aws.region:ap-southeast-1}") String region,
            AwsCredentialsProvider credentialsProvider) {

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(
            @Value("${aws.region:ap-southeast-1}") String region,
            AwsCredentialsProvider credentialsProvider) {

        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();
    }
}
