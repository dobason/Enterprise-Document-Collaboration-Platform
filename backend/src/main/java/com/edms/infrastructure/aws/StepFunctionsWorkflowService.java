package com.edms.infrastructure.aws;

import com.edms.api.exception.BadRequestException;
import com.edms.api.exception.ResourceNotFoundException;
import com.edms.application.ports.WorkflowService;
import com.edms.domain.enums.DocumentStatus;
import com.edms.infrastructure.persistence.entity.DocumentEntity;
import com.edms.infrastructure.persistence.repository.DocumentJpaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.SendTaskFailureRequest;
import software.amazon.awssdk.services.sfn.model.SendTaskSuccessRequest;
import software.amazon.awssdk.services.sfn.model.StartExecutionRequest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Profile("aws")
public class StepFunctionsWorkflowService implements WorkflowService {

    private static final Logger log = LoggerFactory.getLogger(StepFunctionsWorkflowService.class);

    private final SfnClient sfnClient;
    private final String stateMachineArn;
    private final DocumentJpaRepository documentRepository;
    private final ObjectMapper objectMapper;

    public StepFunctionsWorkflowService(SfnClient sfnClient,
                                        @Value("${aws.stepfunctions.state-machine-arn}") String stateMachineArn,
                                        DocumentJpaRepository documentRepository,
                                        ObjectMapper objectMapper) {
        this.sfnClient = sfnClient;
        this.stateMachineArn = stateMachineArn;
        this.documentRepository = documentRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void submitForApproval(String documentId, String submittedBy) {
        DocumentEntity doc = documentRepository.findByIdAndDeletedAtIsNull(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

        if (doc.getStatus() != DocumentStatus.DRAFT && doc.getStatus() != DocumentStatus.PENDING) {
            throw new BadRequestException("Only DRAFT or PENDING documents can be submitted");
        }

        doc.setStatus(DocumentStatus.PENDING);
        doc.setUpdatedAt(java.time.Instant.now());
        documentRepository.save(doc);

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("documentId", documentId);
        input.put("submittedBy", submittedBy != null ? submittedBy : "system");
        input.put("title", doc.getTitle());

        try {
            String execArn = sfnClient.startExecution(StartExecutionRequest.builder()
                    .stateMachineArn(stateMachineArn)
                    .name(UUID.randomUUID().toString())
                    .input(objectMapper.writeValueAsString(input))
                    .build()).executionArn();
            log.info("Started approval workflow execution={} for document {}", execArn, documentId);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize workflow input", e);
        }
    }

    @Override
    public void approveDocument(String documentId, String approvedBy) {
        DocumentEntity doc = documentRepository.findByIdAndDeletedAtIsNull(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

        if (doc.getStatus() != DocumentStatus.PENDING && doc.getStatus() != DocumentStatus.DRAFT) {
            throw new BadRequestException("Only PENDING documents can be approved");
        }
        if (doc.getTaskToken() == null || doc.getTaskToken().isBlank()) {
            throw new IllegalStateException("No active approval task for document " + documentId);
        }

        Map<String, Object> output = new LinkedHashMap<>();
        output.put("documentId", documentId);
        output.put("decision", "APPROVED");
        output.put("actedBy", approvedBy != null ? approvedBy : "system");

        try {
            sfnClient.sendTaskSuccess(SendTaskSuccessRequest.builder()
                    .taskToken(doc.getTaskToken())
                    .output(objectMapper.writeValueAsString(output))
                    .build());
            log.info("Sent task success (APPROVED) for document {}", documentId);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize task output", e);
        }
    }

    @Override
    public void rejectDocument(String documentId, String rejectedBy, String reason) {
        DocumentEntity doc = documentRepository.findByIdAndDeletedAtIsNull(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

        if (doc.getStatus() != DocumentStatus.PENDING && doc.getStatus() != DocumentStatus.DRAFT) {
            throw new BadRequestException("Only PENDING documents can be rejected");
        }
        if (doc.getTaskToken() == null || doc.getTaskToken().isBlank()) {
            throw new IllegalStateException("No active approval task for document " + documentId);
        }

        Map<String, Object> output = new LinkedHashMap<>();
        output.put("documentId", documentId);
        output.put("decision", "REJECTED");
        output.put("actedBy", rejectedBy != null ? rejectedBy : "system");
        output.put("reason", reason != null ? reason : "");

        try {
            sfnClient.sendTaskSuccess(SendTaskSuccessRequest.builder()
                    .taskToken(doc.getTaskToken())
                    .output(objectMapper.writeValueAsString(output))
                    .build());
            log.info("Sent task success (REJECTED) for document {}", documentId);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize task output", e);
        }
    }
}
