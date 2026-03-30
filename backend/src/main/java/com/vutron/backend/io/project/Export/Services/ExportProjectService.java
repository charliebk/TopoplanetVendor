package com.vutron.backend.io.project.Export.Services;

import com.vutron.backend.io.project.Export.DTO.ExportProjectDataDto;
import com.vutron.backend.io.project.Export.DTO.ExportProjectResponseDto;
import com.vutron.backend.io.project.Export.DTO.ExportProjectUserDto;
import com.vutron.backend.io.project.Export.Repository.ExportProjectRepository;

import java.util.List;

public final class ExportProjectService {
    private final ExportProjectRepository repository;

    public ExportProjectService(ExportProjectRepository repository) {
        this.repository = repository;
    }

    public ExportProjectResponseDto exportByProjectCode(String projectCode) {
        ExportProjectRepository.ExportProjectRecord record = repository.findByCode(projectCode);

        if (record == null) {
            return null;
        }

        List<ExportProjectUserDto> users = record.users().stream()
            .map(user -> new ExportProjectUserDto(user.email(), user.passwordHash(), user.active()))
            .toList();

        return new ExportProjectResponseDto(
            new ExportProjectDataDto(record.code(), record.name(), record.description()),
            users
        );
    }
}

