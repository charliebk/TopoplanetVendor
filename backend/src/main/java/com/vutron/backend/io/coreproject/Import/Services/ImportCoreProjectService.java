package com.vutron.backend.io.coreproject.Import.Services;

import com.vutron.backend.io.coreproject.Import.DTO.ImportCoreProjectRequestDto;
import com.vutron.backend.io.coreproject.Import.Query.ImportCoreProjectQueries;
import com.vutron.backend.io.coreproject.Import.Repository.ImportCoreProjectRepository;
import com.vutron.backend.manager.coreproject.Repository.CoreProjectRepository;

import java.util.List;

public final class ImportCoreProjectService {
    private final ImportCoreProjectRepository repository;

    public ImportCoreProjectService(ImportCoreProjectRepository repository) {
        this.repository = repository;
    }

    public CoreProjectRepository.CoreProjectRecord importPayload(ImportCoreProjectRequestDto payload) {
        if (payload == null || payload.coreProject() == null) {
            throw new IllegalArgumentException(ImportCoreProjectQueries.CORE_PROJECT_PAYLOAD_REQUIRED);
        }

        List<ImportCoreProjectRepository.ImportCoreProjectUserRecord> users = payload.users() == null
            ? List.of()
            : payload.users().stream()
                .map(user -> new ImportCoreProjectRepository.ImportCoreProjectUserRecord(
                    user.email(),
                    user.passwordHash(),
                    user.active()
                ))
                .toList();

        return repository.upsertCoreProject(
            payload.coreProject().code(),
            payload.coreProject().name(),
            payload.coreProject().description(),
            users
        );
    }
}

