package com.vutron.backend.manager.corelog.Services;

import com.vutron.backend.manager.corelog.DTO.CreateCoreLogRequestDto;
import com.vutron.backend.manager.corelog.Repository.CoreLogRepository;

import java.util.List;

public final class CoreLogService {
    private final CoreLogRepository repository;

    public CoreLogService(CoreLogRepository repository) {
        this.repository = repository;
    }

    public List<CoreLogRepository.CoreLogRecord> list(Long coreProjectId) {
        if (coreProjectId == null) {
            return repository.listAll();
        }

        validatePositiveId(coreProjectId, "Core project id must be greater than zero");
        return repository.listByCoreProjectId(coreProjectId);
    }

    public CoreLogRepository.CoreLogRecord getById(long id) {
        validatePositiveId(id, "Core log id must be greater than zero");
        return repository.findById(id);
    }

    public CoreLogRepository.CoreLogRecord create(CreateCoreLogRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Core log payload is required");
        }

        return createOrUpdate(
            request.coreProjectId(),
            request.coreTypeLogId(),
            request.title(),
            request.message(),
            request.comment(),
            request.happenedAtUtc(),
            null
        );
    }

    public CoreLogRepository.CoreLogRecord updateById(
        long id,
        long coreProjectId,
        long coreTypeLogId,
        String title,
        String message,
        String comment,
        String happenedAtUtc
    ) {
        validatePositiveId(id, "Core log id must be greater than zero");

        return createOrUpdate(
            coreProjectId,
            coreTypeLogId,
            title,
            message,
            comment,
            happenedAtUtc,
            id
        );
    }

    public boolean deleteById(long id) {
        validatePositiveId(id, "Core log id must be greater than zero");
        return repository.deleteById(id);
    }

    private CoreLogRepository.CoreLogRecord createOrUpdate(
        long coreProjectId,
        long coreTypeLogId,
        String title,
        String message,
        String comment,
        String happenedAtUtc,
        Long id
    ) {
        validatePositiveId(coreProjectId, "Core project id must be greater than zero");
        validatePositiveId(coreTypeLogId, "Core type log id must be greater than zero");
        validateRequired(title, "Core log title is required");
        validateRequired(message, "Core log message is required");

        if (!repository.existsCoreProject(coreProjectId)) {
            throw new IllegalArgumentException("Core project not found");
        }

        if (!repository.existsCoreTypeLog(coreTypeLogId)) {
            throw new IllegalArgumentException("Core type log not found");
        }

        String normalizedTitle = title.trim();
        String normalizedMessage = message.trim();
        String normalizedComment = nullableTrim(comment);
        String normalizedHappenedAtUtc = nullableTrim(happenedAtUtc);

        if (id == null) {
            return repository.create(
                coreProjectId,
                coreTypeLogId,
                normalizedTitle,
                normalizedMessage,
                normalizedComment,
                normalizedHappenedAtUtc
            );
        }

        return repository.updateById(
            id,
            coreProjectId,
            coreTypeLogId,
            normalizedTitle,
            normalizedMessage,
            normalizedComment,
            normalizedHappenedAtUtc
        );
    }

    private static void validateRequired(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void validatePositiveId(long value, String message) {
        if (value <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private static String nullableTrim(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}