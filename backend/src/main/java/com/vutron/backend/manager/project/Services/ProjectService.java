package com.vutron.backend.manager.project.Services;

import com.vutron.backend.manager.project.DTO.CreateProjectRequestDto;
import com.vutron.backend.manager.project.Repository.ProjectRepository;

import java.util.List;

public final class ProjectService {
    private final ProjectRepository repository;

    public ProjectService(ProjectRepository repository) {
        this.repository = repository;
    }

    public List<ProjectRepository.ProjectRecord> listActive() {
        return repository.listActive();
    }

    public ProjectRepository.ProjectRecord getById(long projectId) {
        return repository.findById(projectId);
    }

    public ProjectRepository.ProjectRecord create(CreateProjectRequestDto request) {
        if (request == null || isBlank(request.code()) || isBlank(request.name())) {
            throw new IllegalArgumentException("Project code and name are required");
        }

        return repository.create(
            request.code().trim(),
            request.name().trim(),
            nullableTrim(request.description())
        );
    }

    public boolean softDelete(long projectId) {
        return repository.softDelete(projectId);
    }

    public ProjectRepository.ProjectRecord updateById(long projectId, String code, String name, String description) {
        if (isBlank(code) || isBlank(name)) {
            throw new IllegalArgumentException("Project code and name are required");
        }

        return repository.updateById(
            projectId,
            code.trim(),
            name.trim(),
            nullableTrim(description)
        );
    }

    public ProjectRepository.ProjectRecord upsertByCode(CreateProjectRequestDto request) {
        if (request == null || isBlank(request.code()) || isBlank(request.name())) {
            throw new IllegalArgumentException("Project code and name are required");
        }

        return repository.upsertByCode(
            request.code().trim(),
            request.name().trim(),
            nullableTrim(request.description())
        );
    }

    public List<ProjectRepository.ProjectUserRecord> listUsers(long projectId) {
        return repository.listUsersByProject(projectId);
    }

    public void replaceUsers(long projectId, List<ProjectRepository.ProjectUserRecord> users) {
        repository.replaceProjectUsers(projectId, users);
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
