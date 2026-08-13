package com.edms;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.SingleValueHeaders;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class StreamLambdaHandler implements RequestStreamHandler {
    private static SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        try {
            // Khởi tạo Spring Boot context một lần duy nhất lúc Cold Start
            handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(EdmsApplication.class);
        } catch (ContainerInitializationException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not initialize Spring Boot application", e);
        }
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        byte[] body = inputStream.readAllBytes();
        String raw = new String(body, StandardCharsets.UTF_8);

        // Event từ AWS Step Functions (lambda:invoke) có marker "edmsInternal"
        if (isStepFunctionsEvent(raw)) {
            AwsProxyRequest internal = new AwsProxyRequest();
            internal.setHttpMethod("POST");
            internal.setPath("/internal/workflow");
            internal.setBody(raw);
            SingleValueHeaders headers = new SingleValueHeaders();
            headers.put("Content-Type", "application/json");
            internal.setHeaders(headers);
            AwsProxyResponse resp = handler.proxy(internal, context);
            outputStream.write(resp.getBody() != null ? resp.getBody().getBytes(StandardCharsets.UTF_8)
                    : new byte[0]);
            return;
        }

        handler.proxyStream(new ByteArrayInputStream(body), outputStream, context);
    }

    private boolean isStepFunctionsEvent(String raw) {
        if (raw == null || raw.isBlank()) {
            return false;
        }
        try {
            JsonNode node = OBJECT_MAPPER.readTree(raw);
            return node.hasNonNull("edmsInternal");
        } catch (Exception e) {
            return false;
        }
    }
}
