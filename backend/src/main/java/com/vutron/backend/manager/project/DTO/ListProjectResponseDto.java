package com.vutron.backend.manager.project.DTO;

import com.vutron.backend.manager.project.Repository.ProjectRepository;

public record ListProjectResponseDto(
    long id,
    String code,
    String name,
    String description,
    boolean deleted,
    String createdAt,
    String updatedAt
) {
    public static ListProjectResponseDto from(ProjectRepository.ProjectRecord record) {
        return new ListProjectResponseDto(
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
