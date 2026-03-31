package com.vutron.backend.manager.requirementlevel.DTO;

import com.vutron.backend.manager.requirementlevel.Repository.RequirementLevelRepository;

public record CreateRequirementLevelResponseDto(
    long id,
    long coreProjectId,
    String code,
    String name,
    String description,
    double factor,
    String createdAt,
    String updatedAt
) {
    public static CreateRequirementLevelResponseDto from(RequirementLevelRepository.RequirementLevelRecord record) {
        return new CreateRequirementLevelResponseDto(
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