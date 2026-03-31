package com.vutron.backend.io.coreproject.Export.Services;

import com.vutron.backend.io.coreproject.Export.DTO.ExportCoreProjectDataDto;
import com.vutron.backend.io.coreproject.Export.DTO.ExportCoreProjectResponseDto;
import com.vutron.backend.io.coreproject.Export.DTO.ExportCoreProjectUserDto;
import com.vutron.backend.io.coreproject.Export.Repository.ExportCoreProjectRepository;

import java.util.List;

public final class ExportCoreProjectService {
    private final ExportCoreProjectRepository repository;

    public ExportCoreProjectService(ExportCoreProjectRepository repository) {
        this.repository = repository;
    }

    public ExportCoreProjectResponseDto exportByCoreProjectCode(String coreProjectCode) {
        ExportCoreProjectRepository.ExportCoreProjectRecord record = repository.findByCode(coreProjectCode);

        if (record == null) {
            return null;
        }

        List<ExportCoreProjectUserDto> users = record.users().stream()
            .map(user -> new ExportCoreProjectUserDto(user.email(), user.passwordHash(), user.active()))
            .toList();

        return new ExportCoreProjectResponseDto(
            new ExportCoreProjectDataDto(record.code(), record.name(), record.description()),
            users
        );
    }
}

