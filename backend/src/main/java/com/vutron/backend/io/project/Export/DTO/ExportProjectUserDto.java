package com.vutron.backend.io.project.Export.DTO;

public record ExportProjectUserDto(String email, String passwordHash, boolean active) {
}
