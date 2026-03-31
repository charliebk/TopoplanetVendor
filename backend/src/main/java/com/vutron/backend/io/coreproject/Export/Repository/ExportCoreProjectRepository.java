package com.vutron.backend.io.coreproject.Export.Repository;

import com.vutron.backend.manager.coreproject.Repository.CoreProjectRepository;
import com.vutron.backend.manager.coreproject.Services.CoreProjectService;

import java.util.List;

public final class ExportCoreProjectRepository {
    private final CoreProjectService coreProjectService;

    public ExportCoreProjectRepository(CoreProjectService coreProjectService) {
        this.coreProjectService = coreProjectService;
    }

    public ExportCoreProjectRecord findByCode(String code) {
        List<CoreProjectRepository.CoreProjectRecord> coreProjects = coreProjectService.listActive();
        CoreProjectRepository.CoreProjectRecord coreProject = coreProjects.stream()
            .filter(value -> value.code().equals(code))
            .findFirst()
            .orElse(null);

        if (coreProject == null) {
            return null;
        }

        List<CoreProjectRepository.CoreProjectUserRecord> users = coreProjectService.listUsers(coreProject.id());

        List<ExportCoreProjectUserRecord> mappedUsers = users.stream()
            .map(user -> new ExportCoreProjectUserRecord(user.email(), user.passwordHash(), user.active()))
            .toList();

        return new ExportCoreProjectRecord(
            coreProject.code(),
            coreProject.name(),
            coreProject.description(),
            mappedUsers
        );
    }

    public record ExportCoreProjectRecord(
        String code,
        String name,
        String description,
        List<ExportCoreProjectUserRecord> users
    ) {
    }

    public record ExportCoreProjectUserRecord(String email, String passwordHash, boolean active) {
    }
}

