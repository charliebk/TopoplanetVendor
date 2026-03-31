package com.vutron.backend.manager.coretypelog.DTO;

import com.vutron.backend.manager.coretypelog.Repository.CoreTypeLogRepository;

public record CreateCoreTypeLogResponseDto(
    long id,
    String code,
    String name,
    String description,
    String createdAt,
    String updatedAt
) {
    public static CreateCoreTypeLogResponseDto from(CoreTypeLogRepository.CoreTypeLogRecord record) {
        return new CreateCoreTypeLogResponseDto(
            record.id(),
            record.code(),
            record.name(),
            record.description(),
            record.createdAt(),
            record.updatedAt()
        );
    }
}