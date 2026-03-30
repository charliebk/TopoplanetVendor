package com.vutron.backend.io.project.Import.DTO;

import com.vutron.backend.manager.project.Repository.ProjectRepository;

public record ImportProjectResponseDto(
    long id,
    String code,
    String name,
    String description,
    boolean deleted,
    String createdAt,
    String updatedAt
) {
    public static ImportProjectResponseDto from(ProjectRepository.ProjectRecord record) {
        return new ImportProjectResponseDto(
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
