package com.vutron.backend.io.project.Import.DTO;

public record ImportProjectUserDto(String email, String passwordHash, boolean active) {
}
