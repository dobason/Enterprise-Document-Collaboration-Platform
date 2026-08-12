package com.edms.api.controller;

import com.edms.api.dto.CreateDocumentRequest;
import com.edms.api.dto.DocumentDto;
import com.edms.api.dto.PageResponse;
import com.edms.api.exception.ResourceNotFoundException;
import com.edms.application.service.DocumentApplicationService;
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

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mysql")
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DocumentApplicationService documentService;

    @Test
    @WithMockUser(username = "u1", roles = "USER")
    @DisplayName("GET /documents - Success")
    void getDocuments_Success() throws Exception {
        DocumentDto doc = DocumentDto.builder()
                .id("d1")
                .title("Q1 Engineering Report")
                .type("Report")
                .status("APPROVED")
                .ownerId("u1")
                .createdAt(Instant.now())
                .build();

        PageResponse<DocumentDto> pageResponse = PageResponse.<DocumentDto>builder()
                .items(List.of(doc))
                .total(1)
                .page(1)
                .limit(10)
                .totalPages(1)
                .build();

        when(documentService.getDocuments(anyInt(), anyInt(), any(), any(), any(), eq("u1"), anyBoolean()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value("d1"))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    @WithMockUser(username = "u1", roles = "USER")
    @DisplayName("GET /documents/{id} - Success")
    void getDocumentById_Success() throws Exception {
        DocumentDto doc = DocumentDto.builder()
                .id("d1")
                .title("Q1 Engineering Report")
                .type("Report")
                .status("APPROVED")
                .ownerId("u1")
                .createdAt(Instant.now())
                .build();

        when(documentService.getDocumentById(eq("d1"), eq("u1"), anyBoolean())).thenReturn(doc);

        mockMvc.perform(get("/documents/d1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("d1"))
                .andExpect(jsonPath("$.title").value("Q1 Engineering Report"));
    }

    @Test
    @WithMockUser(username = "u1", roles = "USER")
    @DisplayName("GET /documents/{id} - Not Found")
    void getDocumentById_NotFound() throws Exception {
        when(documentService.getDocumentById(eq("d999"), eq("u1"), anyBoolean()))
                .thenThrow(new ResourceNotFoundException("Document not found: d999"));

        mockMvc.perform(get("/documents/d999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Document not found: d999"));
    }

    @Test
    @WithMockUser(username = "u1", roles = "USER")
    @DisplayName("POST /documents - Success")
    void createDocument_Success() throws Exception {
        CreateDocumentRequest request = CreateDocumentRequest.builder()
                .title("Test Title")
                .type("Report")
                .folderId("f1")
                .content("Sample content")
                .build();
        DocumentDto doc = DocumentDto.builder()
                .id("d99")
                .title("Test Title")
                .type("Report")
                .status("DRAFT")
                .ownerId("u1")
                .createdAt(Instant.now())
                .build();

        when(documentService.createDocument(any(CreateDocumentRequest.class), eq("u1"), anyBoolean())).thenReturn(doc);

        mockMvc.perform(post("/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("d99"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    @WithMockUser(username = "u1", roles = "USER")
    @DisplayName("DELETE /documents/{id} - Soft Delete Success")
    void deleteDocument_Success() throws Exception {
        mockMvc.perform(delete("/documents/d1"))
                .andExpect(status().isNoContent());
    }
}
