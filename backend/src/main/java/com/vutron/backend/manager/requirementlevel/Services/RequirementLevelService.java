package com.vutron.backend.manager.requirementlevel.Services;

import com.vutron.backend.manager.requirementlevel.DTO.CreateRequirementLevelRequestDto;
import com.vutron.backend.manager.requirementlevel.Repository.RequirementLevelRepository;

import java.util.List;
import java.util.Locale;

public final class RequirementLevelService {
    private final RequirementLevelRepository repository;

    public RequirementLevelService(RequirementLevelRepository repository) {
        this.repository = repository;
    }

    public List<RequirementLevelRepository.RequirementLevelRecord> listAll(Long coreProjectId) {
        if (coreProjectId == null) {
            return repository.listAll();
        }

        return repository.listByCoreProjectId(coreProjectId);
    }

    public RequirementLevelRepository.RequirementLevelRecord getById(long requirementLevelId) {
        return repository.findById(requirementLevelId);
    }

    public RequirementLevelRepository.RequirementLevelRecord create(CreateRequirementLevelRequestDto request) {
        if (request == null || request.coreProjectId() == null || isBlank(request.code()) || isBlank(request.name()) || request.factor() == null) {
            throw new IllegalArgumentException("Requirement level core project, code, name and factor are required");
        }

        if (request.factor() <= 0) {
            throw new IllegalArgumentException("Requirement level factor must be greater than zero");
        }

        return repository.create(
            request.coreProjectId(),
            normalizeCode(request.code()),
            request.name().trim(),
            nullableTrim(request.description()),
            request.factor()
        );
    }

    public RequirementLevelRepository.RequirementLevelRecord updateById(
        long id,
        Long coreProjectId,
        String code,
        String name,
        String description,
        Double factor
    ) {
        if (coreProjectId == null || isBlank(code) || isBlank(name) || factor == null) {
            throw new IllegalArgumentException("Requirement level core project, code, name and factor are required");
        }

        if (factor <= 0) {
            throw new IllegalArgumentException("Requirement level factor must be greater than zero");
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