package com.vutron.backend.manager.coreproject.Services;

import com.vutron.backend.manager.coreproject.DTO.CreateCoreProjectRequestDto;
import com.vutron.backend.manager.coreproject.Repository.CoreProjectRepository;

import java.util.List;

public final class CoreProjectService {
    private final CoreProjectRepository repository;

    public CoreProjectService(CoreProjectRepository repository) {
        this.repository = repository;
    }

    public List<CoreProjectRepository.CoreProjectRecord> listActive() {
        return repository.listActive();
    }

    public CoreProjectRepository.CoreProjectRecord getById(long coreProjectId) {
        return repository.findById(coreProjectId);
    }

    public CoreProjectRepository.CoreProjectRecord create(CreateCoreProjectRequestDto request) {
        if (request == null || isBlank(request.code()) || isBlank(request.name())) {
            throw new IllegalArgumentException("Core project code and name are required");
        }

        return repository.create(
            request.code().trim(),
            request.name().trim(),
            nullableTrim(request.description())
        );
    }

    public boolean softDelete(long coreProjectId) {
        return repository.softDelete(coreProjectId);
    }

    public CoreProjectRepository.CoreProjectRecord updateById(long coreProjectId, String code, String name, String description) {
        if (isBlank(code) || isBlank(name)) {
            throw new IllegalArgumentException("Core project code and name are required");
        }

        return repository.updateById(
            coreProjectId,
            code.trim(),
            name.trim(),
            nullableTrim(description)
        );
    }

    public CoreProjectRepository.CoreProjectRecord upsertByCode(CreateCoreProjectRequestDto request) {
        if (request == null || isBlank(request.code()) || isBlank(request.name())) {
            throw new IllegalArgumentException("Core project code and name are required");
        }

        return repository.upsertByCode(
            request.code().trim(),
            request.name().trim(),
            nullableTrim(request.description())
        );
    }

    public List<CoreProjectRepository.CoreProjectUserRecord> listUsers(long coreProjectId) {
        return repository.listUsersByCoreProject(coreProjectId);
    }

    public void replaceUsers(long coreProjectId, List<CoreProjectRepository.CoreProjectUserRecord> users) {
        repository.replaceCoreProjectUsers(coreProjectId, users);
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
