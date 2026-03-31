package com.vutron.backend.manager.requirementlevel.DTO;

import com.vutron.backend.manager.requirementlevel.Repository.RequirementLevelRepository;

public record UpdateRequirementLevelResponseDto(
    long id,
    long coreProjectId,
    String code,
    String name,
    String description,
    double factor,
    String createdAt,
    String updatedAt
) {
    public static UpdateRequirementLevelResponseDto from(RequirementLevelRepository.RequirementLevelRecord record) {
        return new UpdateRequirementLevelResponseDto(
            record.id(),
            record.coreProjectId(),
            record.code(),
            record.name(),
            record.description(),
            record.factor(),
            record.createdAt(),
            record.updatedAt()
        );
    }
}