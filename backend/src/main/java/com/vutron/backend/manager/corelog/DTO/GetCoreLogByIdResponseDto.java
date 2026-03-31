package com.vutron.backend.manager.corelog.DTO;

import com.vutron.backend.manager.corelog.Repository.CoreLogRepository;

public record GetCoreLogByIdResponseDto(
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
    public static GetCoreLogByIdResponseDto from(CoreLogRepository.CoreLogRecord record) {
        return new GetCoreLogByIdResponseDto(
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