package com.vutron.backend.manager.category.DTO;

import com.vutron.backend.manager.category.Repository.CategoryRepository;

public record CreateCategoryResponseDto(
    long id,
    long coreProjectId,
    String code,
    String name,
    String description,
    double factor,
    String createdAt,
    String updatedAt
) {
    public static CreateCategoryResponseDto from(CategoryRepository.CategoryRecord record) {
        return new CreateCategoryResponseDto(
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