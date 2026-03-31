package com.vutron.backend.io.coreproject.Export.DTO;

import java.util.List;

public record ExportCoreProjectResponseDto(ExportCoreProjectDataDto coreProject, List<ExportCoreProjectUserDto> users) {
}
