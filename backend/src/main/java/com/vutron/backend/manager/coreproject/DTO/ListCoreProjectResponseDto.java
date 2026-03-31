package com.vutron.backend.manager.coreproject.DTO;

import com.vutron.backend.manager.coreproject.Repository.CoreProjectRepository;

public record ListCoreProjectResponseDto(
    long id,
    String code,
    String name,
    String description,
    boolean deleted,
    String createdAt,
    String updatedAt
) {
    public static ListCoreProjectResponseDto from(CoreProjectRepository.CoreProjectRecord record) {
        return new ListCoreProjectResponseDto(
            record.id(),
            record.code(),
            record.name(),
            record.description(),
            record.deleted(),
            record.createdAt(),
            record.updatedAt()
        );
    }
}
