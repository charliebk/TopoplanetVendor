package com.vutron.backend.manager.coreproject.DTO;

import com.vutron.backend.manager.coreproject.Repository.CoreProjectRepository;

public record UpdateCoreProjectResponseDto(
    long id,
    String code,
    String name,
    String description,
    boolean deleted,
    String createdAt,
    String updatedAt
) {
    public static UpdateCoreProjectResponseDto from(CoreProjectRepository.CoreProjectRecord record) {
        return new UpdateCoreProjectResponseDto(
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