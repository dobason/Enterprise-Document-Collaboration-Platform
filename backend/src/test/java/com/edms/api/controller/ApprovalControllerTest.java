package com.edms.api.controller;

import com.edms.api.dto.ApprovalActionResponse;
import com.edms.api.dto.ApprovalSubmitResponse;
import com.edms.application.service.ApprovalApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class ApprovalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApprovalApplicationService approvalService;

    @Test
    @WithMockUser(username = "u1", roles = "OWNER")
    @DisplayName("POST /approval/submit - Success")
    void submitForApproval_Success() throws Exception {
        ApprovalSubmitResponse response = ApprovalSubmitResponse.builder()
                .id("d1")
                .status("PENDING")
                .message("Submitted for approval")
                .build();

        when(approvalService.submitForApproval(eq("d1"), eq("u1"))).thenReturn(response);

        mockMvc.perform(post("/approval/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("documentId", "d1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.message").value("Submitted for approval"));
    }

    @Test
    @WithMockUser(username = "u3", roles = "MANAGER")
    @DisplayName("POST /approval/approve - Success")
    void approveDocument_Success() throws Exception {
        ApprovalActionResponse response = ApprovalActionResponse.builder()
                .id("d1")
                .status("APPROVED")
                .build();

        when(approvalService.approveDocument(eq("d1"), eq("u3"))).thenReturn(response);

        mockMvc.perform(post("/approval/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("documentId", "d1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
}
