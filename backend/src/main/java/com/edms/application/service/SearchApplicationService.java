package com.edms.application.service;

import com.edms.api.dto.DocumentDto;
import com.edms.api.dto.SearchResponse;
import com.edms.domain.enums.DocumentStatus;
import com.edms.infrastructure.persistence.entity.DocumentEntity;
import com.edms.infrastructure.persistence.repository.DocumentJpaRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchApplicationService {

    private final DocumentJpaRepository documentRepository;
    private final DocumentApplicationService documentApplicationService;

    public SearchApplicationService(DocumentJpaRepository documentRepository,
                                    DocumentApplicationService documentApplicationService) {
        this.documentRepository = documentRepository;
        this.documentApplicationService = documentApplicationService;
    }

    @Transactional(readOnly = true)
    public SearchResponse searchDocuments(String query, String type, String status, String currentUserId) {
        Specification<DocumentEntity> spec = (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isNull(root.get("deletedAt")));

            if (query != null && !query.isBlank()) {
                String pattern = "%" + query.toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
                Predicate contentMatch = cb.like(root.get("content"), pattern);
                predicates.add(cb.or(titleMatch, contentMatch));
            }

            if (type != null && !type.isBlank()) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            if (status != null && !status.isBlank()) {
                try {
                    DocumentStatus docStatus = DocumentStatus.valueOf(status.toUpperCase());
                    predicates.add(cb.equal(root.get("status"), docStatus));
                } catch (IllegalArgumentException ignored) {
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<DocumentEntity> results = documentRepository.findAll(spec);

        List<DocumentDto> dtos = results.stream()
                .map(documentApplicationService::mapToDto)
                .collect(Collectors.toList());

        return SearchResponse.builder()
                .items(dtos)
                .total(dtos.size())
                .build();
    }
}
