package com.vutron.backend.io.coreproject.Import.Repository;

import com.vutron.backend.manager.coreproject.DTO.CreateCoreProjectRequestDto;
import com.vutron.backend.manager.coreproject.Repository.CoreProjectRepository;
import com.vutron.backend.manager.coreproject.Services.CoreProjectService;

import java.util.List;

public final class ImportCoreProjectRepository {
    private final CoreProjectService coreProjectService;

    public ImportCoreProjectRepository(CoreProjectService coreProjectService) {
        this.coreProjectService = coreProjectService;
    }

    public CoreProjectRepository.CoreProjectRecord upsertCoreProject(
        String code,
        String name,
        String description,
        List<ImportCoreProjectUserRecord> users
    ) {
        CoreProjectRepository.CoreProjectRecord coreProject = coreProjectService.upsertByCode(
            new CreateCoreProjectRequestDto(code, name, description)
        );

        List<CoreProjectRepository.CoreProjectUserRecord> mappedUsers = users == null
            ? List.of()
            : users.stream().map(user -> new CoreProjectRepository.CoreProjectUserRecord(
                0,
                coreProject.id(),
                user.email(),
                user.passwordHash(),
                user.active()
            )).toList();

        coreProjectService.replaceUsers(coreProject.id(), mappedUsers);
        return coreProject;
    }

    public record ImportCoreProjectUserRecord(String email, String passwordHash, boolean active) {
    }
}

