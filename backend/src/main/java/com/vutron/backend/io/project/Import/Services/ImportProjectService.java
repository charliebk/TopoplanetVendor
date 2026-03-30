package com.vutron.backend.io.project.Import.Services;

import com.vutron.backend.io.project.Import.DTO.ImportProjectRequestDto;
import com.vutron.backend.io.project.Import.Query.ImportProjectQueries;
import com.vutron.backend.io.project.Import.Repository.ImportProjectRepository;
import com.vutron.backend.manager.project.Repository.ProjectRepository;

import java.util.List;

public final class ImportProjectService {
    private final ImportProjectRepository repository;

    public ImportProjectService(ImportProjectRepository repository) {
        this.repository = repository;
    }

    public ProjectRepository.ProjectRecord importPayload(ImportProjectRequestDto payload) {
        if (payload == null || payload.project() == null) {
            throw new IllegalArgumentException(ImportProjectQueries.PROJECT_PAYLOAD_REQUIRED);
        }

        List<ImportProjectRepository.ImportProjectUserRecord> users = payload.users() == null
            ? List.of()
            : payload.users().stream()
                .map(user -> new ImportProjectRepository.ImportProjectUserRecord(
                    user.email(),
                    user.passwordHash(),
                    user.active()
                ))
                .toList();

        return repository.upsertProject(
            payload.project().code(),
            payload.project().name(),
            payload.project().description(),
            users
        );
    }
}

