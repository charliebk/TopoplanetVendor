package com.vutron.backend.io.project.Import.DTO;

import java.util.List;

public record ImportProjectRequestDto(ImportProjectDataDto project, List<ImportProjectUserDto> users) {
}
