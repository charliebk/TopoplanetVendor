package com.vutron.backend.io.coreproject.Import.DTO;

import java.util.List;

public record ImportCoreProjectRequestDto(ImportCoreProjectDataDto coreProject, List<ImportCoreProjectUserDto> users) {
}
