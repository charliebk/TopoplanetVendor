package com.vutron.backend.manager.category.DTO;

public record CreateCategoryRequestDto(Long coreProjectId, String code, String name, String description, Double factor) {
}