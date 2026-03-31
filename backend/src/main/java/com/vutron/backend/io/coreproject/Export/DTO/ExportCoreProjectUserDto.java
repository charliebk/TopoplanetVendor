package com.vutron.backend.io.coreproject.Export.DTO;

public record ExportCoreProjectUserDto(String email, String passwordHash, boolean active) {
}
