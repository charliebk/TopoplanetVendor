package com.vutron.backend.manager.requirementlevel.DTO;

public record CreateRequirementLevelRequestDto(Long coreProjectId, String code, String name, String description, Double factor) {
}