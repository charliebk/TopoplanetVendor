package com.vutron.backend.manager.coreproject.DTO;

import com.vutron.backend.manager.coreproject.Repository.CoreProjectRepository;

public record CreateCoreProjectResponseDto(
    long id,
    String code,
    String name,
    String description,
    boolean deleted,
    String createdAt,
    String updatedAt
) {
    public static CreateCoreProjectResponseDto from(CoreProjectRepository.CoreProjectRecord record) {
        return new CreateCoreProjectResponseDto(
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
