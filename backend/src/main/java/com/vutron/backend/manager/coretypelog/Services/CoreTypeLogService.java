package com.vutron.backend.manager.coretypelog.Services;

import com.vutron.backend.manager.coretypelog.DTO.CreateCoreTypeLogRequestDto;
import com.vutron.backend.manager.coretypelog.Repository.CoreTypeLogRepository;

import java.util.List;
import java.util.Locale;

public final class CoreTypeLogService {
    private final CoreTypeLogRepository repository;

    public CoreTypeLogService(CoreTypeLogRepository repository) {
        this.repository = repository;
    }

    public List<CoreTypeLogRepository.CoreTypeLogRecord> listAll() {
        return repository.listAll();
    }

    public CoreTypeLogRepository.CoreTypeLogRecord getById(long coreTypeLogId) {
        return repository.findById(coreTypeLogId);
    }

    public CoreTypeLogRepository.CoreTypeLogRecord create(CreateCoreTypeLogRequestDto request) {
        if (request == null || isBlank(request.code()) || isBlank(request.name())) {
            throw new IllegalArgumentException("Core type log code and name are required");
        }

        return repository.create(
            normalizeCode(request.code()),
            request.name().trim(),
            nullableTrim(request.description())
        );
    }

    public CoreTypeLogRepository.CoreTypeLogRecord updateById(long id, String code, String name, String description) {
        if (isBlank(code) || isBlank(name)) {
            throw new IllegalArgumentException("Core type log code and name are required");
        }

        return repository.updateById(
            id,
            normalizeCode(code),
            name.trim(),
            nullableTrim(description)
        );
    }

    public boolean deleteById(long id) {
        return repository.deleteById(id);
    }

    private static String normalizeCode(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
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