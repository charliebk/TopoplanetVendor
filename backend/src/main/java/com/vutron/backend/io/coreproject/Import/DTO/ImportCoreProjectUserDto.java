package com.vutron.backend.io.coreproject.Import.DTO;

public record ImportCoreProjectUserDto(String email, String passwordHash, boolean active) {
}
