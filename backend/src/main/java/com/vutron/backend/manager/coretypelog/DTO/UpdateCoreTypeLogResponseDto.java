package com.vutron.backend.manager.coretypelog.DTO;

import com.vutron.backend.manager.coretypelog.Repository.CoreTypeLogRepository;

public record UpdateCoreTypeLogResponseDto(
    long id,
    String code,
    String name,
    String description,
    String createdAt,
    String updatedAt
) {
    public static UpdateCoreTypeLogResponseDto from(CoreTypeLogRepository.CoreTypeLogRecord record) {
        return new UpdateCoreTypeLogResponseDto(
            record.id(),
            record.code(),
            record.name(),
            record.description(),
            record.createdAt(),
            record.updatedAt()
        );
    }
}