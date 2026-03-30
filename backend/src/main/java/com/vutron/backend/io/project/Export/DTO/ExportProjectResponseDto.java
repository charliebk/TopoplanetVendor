package com.vutron.backend.io.project.Export.DTO;

import java.util.List;

public record ExportProjectResponseDto(ExportProjectDataDto project, List<ExportProjectUserDto> users) {
}
