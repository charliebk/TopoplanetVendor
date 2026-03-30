package com.vutron.backend.io.project.Import.Repository;

import com.vutron.backend.manager.project.DTO.CreateProjectRequestDto;
import com.vutron.backend.manager.project.Repository.ProjectRepository;
import com.vutron.backend.manager.project.Services.ProjectService;

import java.util.List;

public final class ImportProjectRepository {
    private final ProjectService projectService;

    public ImportProjectRepository(ProjectService projectService) {
        this.projectService = projectService;
    }

    public ProjectRepository.ProjectRecord upsertProject(
        String code,
        String name,
        String description,
        List<ImportProjectUserRecord> users
    ) {
        ProjectRepository.ProjectRecord project = projectService.upsertByCode(
            new CreateProjectRequestDto(code, name, description)
        );

        List<ProjectRepository.ProjectUserRecord> mappedUsers = users == null
            ? List.of()
            : users.stream().map(user -> new ProjectRepository.ProjectUserRecord(
                0,
                project.id(),
                user.email(),
                user.passwordHash(),
                user.active()
            )).toList();

        projectService.replaceUsers(project.id(), mappedUsers);
        return project;
    }

    public record ImportProjectUserRecord(String email, String passwordHash, boolean active) {
    }
}

