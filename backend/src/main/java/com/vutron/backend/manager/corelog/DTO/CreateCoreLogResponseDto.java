package com.vutron.backend.manager.corelog.DTO;

import com.vutron.backend.manager.corelog.Repository.CoreLogRepository;

public record CreateCoreLogResponseDto(
    long id,
    long coreProjectId,
    long coreTypeLogId,
    String coreTypeLogCode,
    String coreTypeLogName,
    String title,
    String message,
    String comment,
    String happenedAtUtc,
    String createdAt
) {
    public static CreateCoreLogResponseDto from(CoreLogRepository.CoreLogRecord record) {
        return new CreateCoreLogResponseDto(
            record.id(),
            record.coreProjectId(),
            record.coreTypeLogId(),
            record.coreTypeLogCode(),
            record.coreTypeLogName(),
            record.title(),
            record.message(),
            record.comment(),
            record.happenedAtUtc(),
            record.createdAt()
        );
    }
}