package com.vutron.backend.manager.category.DTO;

import com.vutron.backend.manager.category.Repository.CategoryRepository;

public record GetCategoryByIdResponseDto(
    long id,
    long coreProjectId,
    String code,
    String name,
    String description,
    double factor,
    String createdAt,
    String updatedAt
) {
    public static GetCategoryByIdResponseDto from(CategoryRepository.CategoryRecord record) {
        return new GetCategoryByIdResponseDto(
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