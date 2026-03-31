package com.vutron.backend.manager.category.Services;

import com.vutron.backend.manager.category.DTO.CreateCategoryRequestDto;
import com.vutron.backend.manager.category.Repository.CategoryRepository;

import java.util.List;
import java.util.Locale;

public final class CategoryService {
    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    public List<CategoryRepository.CategoryRecord> listAll(Long coreProjectId) {
        if (coreProjectId == null) {
            return repository.listAll();
        }

        return repository.listByCoreProjectId(coreProjectId);
    }

    public CategoryRepository.CategoryRecord getById(long categoryId) {
        return repository.findById(categoryId);
    }

    public CategoryRepository.CategoryRecord create(CreateCategoryRequestDto request) {
        if (request == null || request.coreProjectId() == null || isBlank(request.code()) || isBlank(request.name()) || request.factor() == null) {
            throw new IllegalArgumentException("Category core project, code, name and factor are required");
        }

        if (request.factor() <= 0) {
            throw new IllegalArgumentException("Category factor must be greater than zero");
        }

        return repository.create(
            request.coreProjectId(),
            normalizeCode(request.code()),
            request.name().trim(),
            nullableTrim(request.description()),
            request.factor()
        );
    }

    public CategoryRepository.CategoryRecord updateById(
        long id,
        Long coreProjectId,
        String code,
        String name,
        String description,
        Double factor
    ) {
        if (coreProjectId == null || isBlank(code) || isBlank(name) || factor == null) {
            throw new IllegalArgumentException("Category core project, code, name and factor are required");
        }

        if (factor <= 0) {
            throw new IllegalArgumentException("Category factor must be greater than zero");
        }

        return repository.updateById(
            id,
            coreProjectId,
            normalizeCode(code),
            name.trim(),
            nullableTrim(description),
            factor
        );
    }

    public boolean deleteById(long id) {
        return repository.deleteById(id);
    }

    private static String normalizeCode(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static String nullableTrim(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}