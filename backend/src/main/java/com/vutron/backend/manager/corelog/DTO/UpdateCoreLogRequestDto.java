package com.vutron.backend.manager.corelog.DTO;

public record UpdateCoreLogRequestDto(
    long coreProjectId,
    long coreTypeLogId,
    String title,
    String message,
    String comment,
    String happenedAtUtc
) {
}