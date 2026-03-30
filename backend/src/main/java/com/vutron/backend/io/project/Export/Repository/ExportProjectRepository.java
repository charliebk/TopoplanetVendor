package com.vutron.backend.io.project.Export.Repository;

import com.vutron.backend.manager.project.Repository.ProjectRepository;
import com.vutron.backend.manager.project.Services.ProjectService;

import java.util.List;

public final class ExportProjectRepository {
    private final ProjectService projectService;

    public ExportProjectRepository(ProjectService projectService) {
        this.projectService = projectService;
    }

    public ExportProjectRecord findByCode(String code) {
        List<ProjectRepository.ProjectRecord> projects = projectService.listActive();
        ProjectRepository.ProjectRecord project = projects.stream()
            .filter(value -> value.code().equals(code))
            .findFirst()
            .orElse(null);

        if (project == null) {
            return null;
        }

        List<ProjectRepository.ProjectUserRecord> users = projectService.listUsers(project.id());

        List<ExportProjectUserRecord> mappedUsers = users.stream()
            .map(user -> new ExportProjectUserRecord(user.email(), user.passwordHash(), user.active()))
            .toList();

        return new ExportProjectRecord(
            project.code(),
            project.name(),
            project.description(),
            mappedUsers
        );
    }

    public record ExportProjectRecord(
        String code,
        String name,
        String description,
        List<ExportProjectUserRecord> users
    ) {
    }

    public record ExportProjectUserRecord(String email, String passwordHash, boolean active) {
    }
}

