package com.vutron.backend.manager.project.DTO;

import com.vutron.backend.manager.project.Repository.ProjectRepository;

public record GetProjectByIdResponseDto(
    long id,
    String code,
    String name,
    String description,
    boolean deleted,
    String createdAt,
    String updatedAt
) {
    public static GetProjectByIdResponseDto from(ProjectRepository.ProjectRecord record) {
        return new GetProjectByIdResponseDto(
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