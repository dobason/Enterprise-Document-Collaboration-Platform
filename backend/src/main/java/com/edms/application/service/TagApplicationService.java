package com.edms.application.service;

import com.edms.api.dto.AddTagRequest;
import com.edms.api.dto.DocTagDto;
import com.edms.api.dto.DocTagListResponse;
import com.edms.api.dto.TagDto;
import com.edms.api.dto.TagListResponse;
import com.edms.api.exception.ResourceNotFoundException;
import com.edms.infrastructure.persistence.entity.DocumentTagEntity;
import com.edms.infrastructure.persistence.entity.TagEntity;
import com.edms.infrastructure.persistence.repository.DocumentJpaRepository;
import com.edms.infrastructure.persistence.repository.DocumentTagJpaRepository;
import com.edms.infrastructure.persistence.repository.TagJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TagApplicationService {

    private final DocumentJpaRepository documentRepository;
    private final TagJpaRepository tagRepository;
    private final DocumentTagJpaRepository documentTagRepository;

    public TagApplicationService(DocumentJpaRepository documentRepository,
                                 TagJpaRepository tagRepository,
                                 DocumentTagJpaRepository documentTagRepository) {
        this.documentRepository = documentRepository;
        this.tagRepository = tagRepository;
        this.documentTagRepository = documentTagRepository;
    }

    @Transactional(readOnly = true)
    public DocTagListResponse getDocumentTags(String documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new ResourceNotFoundException("Document not found: " + documentId);
        }

        List<DocumentTagEntity> docTags = documentTagRepository.findByDocumentId(documentId);
        List<DocTagDto> items = new ArrayList<>();

        for (DocumentTagEntity dt : docTags) {
            tagRepository.findById(dt.getTagId()).ifPresent(t -> {
                items.add(DocTagDto.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .docTagId(dt.getId())
                        .build());
            });
        }

        return DocTagListResponse.builder().items(items).build();
    }

    @Transactional
    public DocTagDto addTagToDocument(String documentId, AddTagRequest request) {
        if (!documentRepository.existsById(documentId)) {
            throw new ResourceNotFoundException("Document not found: " + documentId);
        }

        // Find or create TagEntity
        TagEntity tag = tagRepository.findByName(request.getName())
                .orElseGet(() -> tagRepository.save(TagEntity.builder()
                        .id(UUID.randomUUID().toString())
                        .name(request.getName())
                        .build()));

        // Check if link exists
        Optional<DocumentTagEntity> existingLink = documentTagRepository.findByDocumentIdAndTagId(documentId, tag.getId());
        DocumentTagEntity docTag;
        if (existingLink.isPresent()) {
            docTag = existingLink.get();
        } else {
            docTag = DocumentTagEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .documentId(documentId)
                    .tagId(tag.getId())
                    .createdAt(Instant.now())
                    .build();
            docTag = documentTagRepository.save(docTag);
        }

        return DocTagDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .docTagId(docTag.getId())
                .build();
    }

    @Transactional
    public void removeTagFromDocument(String documentId, String docTagId) {
        if (!documentRepository.existsById(documentId)) {
            throw new ResourceNotFoundException("Document not found: " + documentId);
        }
        if (!documentTagRepository.existsById(docTagId)) {
            throw new ResourceNotFoundException("Document tag association not found: " + docTagId);
        }
        documentTagRepository.deleteById(docTagId);
    }

    @Transactional(readOnly = true)
    public TagListResponse getAllTags() {
        List<TagDto> items = tagRepository.findAll().stream()
                .map(t -> TagDto.builder().id(t.getId()).name(t.getName()).build())
                .collect(Collectors.toList());
        return TagListResponse.builder().items(items).build();
    }
}
