package com.vutron.backend.manager.category.DTO;

public record UpdateCategoryRequestDto(Long coreProjectId, String code, String name, String description, Double factor) {
}