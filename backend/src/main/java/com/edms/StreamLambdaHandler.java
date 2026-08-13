package com.edms;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
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
            String internalEvent = "{" +
                "\"resource\":\"/internal/workflow\"," +
                "\"path\":\"/internal/workflow\"," +
                "\"httpMethod\":\"POST\"," +
                "\"headers\":{\"Content-Type\":\"application/json\"}," +
                "\"multiValueHeaders\":{\"Content-Type\":[\"application/json\"]}," +
                "\"queryStringParameters\":null," +
                "\"multiValueQueryStringParameters\":null," +
                "\"pathParameters\":null," +
                "\"stageVariables\":null," +
                "\"requestContext\":{" +
                    "\"protocol\":\"HTTP/1.1\"," +
                    "\"httpMethod\":\"POST\"," +
                    "\"path\":\"/internal/workflow\"," +
                    "\"stage\":\"Prod\"," +
                    "\"requestId\":\"workflow-callback\"," +
                    "\"resourcePath\":\"/internal/workflow\"," +
                    "\"identity\":{\"sourceIp\":\"0.0.0.0\"}" +
                "}," +
                "\"body\":" + OBJECT_MAPPER.writeValueAsString(raw) + "," +
                "\"isBase64Encoded\":false" +
            "}";
            handler.proxyStream(new ByteArrayInputStream(internalEvent.getBytes(StandardCharsets.UTF_8)),
                    outputStream, context);
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
