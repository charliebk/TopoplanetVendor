package com.vutron.backend.io.coreproject.Import.DTO;

import com.vutron.backend.manager.coreproject.Repository.CoreProjectRepository;

public record ImportCoreProjectResponseDto(
    long id,
    String code,
    String name,
    String description,
    boolean deleted,
    String createdAt,
    String updatedAt
) {
    public static ImportCoreProjectResponseDto from(CoreProjectRepository.CoreProjectRecord record) {
        return new ImportCoreProjectResponseDto(
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
