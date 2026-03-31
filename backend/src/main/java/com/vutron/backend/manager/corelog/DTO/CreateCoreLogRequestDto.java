package com.vutron.backend.manager.corelog.DTO;

public record CreateCoreLogRequestDto(
    long coreProjectId,
    long coreTypeLogId,
    String title,
    String message,
    String comment,
    String happenedAtUtc
) {
}