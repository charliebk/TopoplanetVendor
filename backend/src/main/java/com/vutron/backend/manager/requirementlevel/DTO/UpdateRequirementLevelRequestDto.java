package com.vutron.backend.manager.requirementlevel.DTO;

public record UpdateRequirementLevelRequestDto(Long coreProjectId, String code, String name, String description, Double factor) {
}