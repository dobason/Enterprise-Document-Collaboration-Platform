package com.edms.api.controller;

import com.edms.api.dto.CreateFolderRequest;
import com.edms.api.dto.FolderDto;
import com.edms.api.exception.ResourceNotFoundException;
import com.edms.application.service.FolderApplicationService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mysql")
class FolderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FolderApplicationService folderService;

    @Test
    @WithMockUser(username = "u1", roles = "USER")
    @DisplayName("GET /folders - Success")
    void getAllFolders_Success() throws Exception {
        FolderDto folder = FolderDto.builder()
                .id("f1")
                .name("Contracts")
                .department("Engineering")
                .ownerId("u1")
                .createdAt(Instant.now())
                .build();

        when(folderService.getAllFolders()).thenReturn(List.of(folder));

        mockMvc.perform(get("/folders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value("f1"))
                .andExpect(jsonPath("$.items[0].name").value("Contracts"));
    }

    @Test
    @WithMockUser(username = "u1", roles = "USER")
    @DisplayName("GET /folders/{id} - Not Found")
    void getFolderById_NotFound() throws Exception {
        when(folderService.getFolderById("non-existent"))
                .thenThrow(new ResourceNotFoundException("Folder not found: non-existent"));

        mockMvc.perform(get("/folders/non-existent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Folder not found: non-existent"));
    }

    @Test
    @WithMockUser(username = "u1", roles = "USER")
    @DisplayName("POST /folders - Success")
    void createFolder_Success() throws Exception {
        CreateFolderRequest request = new CreateFolderRequest("New Folder", "Engineering", "u1");
        FolderDto created = FolderDto.builder()
                .id("f99")
                .name("New Folder")
                .department("Engineering")
                .ownerId("u1")
                .createdAt(Instant.now())
                .build();

        when(folderService.createFolder(any(CreateFolderRequest.class), eq("u1"))).thenReturn(created);

        mockMvc.perform(post("/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("f99"))
                .andExpect(jsonPath("$.name").value("New Folder"));
    }
}
