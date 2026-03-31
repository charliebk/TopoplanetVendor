package com.vutron.backend.io.coreproject.Import.Controller;

import com.vutron.backend.controller.CrudCapabilitiesController;
import com.vutron.backend.io.coreproject.Import.DTO.ImportCoreProjectRequestDto;
import com.vutron.backend.io.coreproject.Import.DTO.ImportCoreProjectResponseDto;
import com.vutron.backend.io.coreproject.Import.Repository.ImportCoreProjectRepository;
import com.vutron.backend.io.coreproject.Import.Services.ImportCoreProjectService;
import com.vutron.backend.manager.coreproject.Repository.CoreProjectRepository;
import com.vutron.backend.manager.coreproject.Services.CoreProjectService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import javax.sql.DataSource;
import java.util.Map;

public final class ImportCoreProjectController extends CrudCapabilitiesController {

    @Override
    protected boolean supportsCreate() {
        return true;
    }

    @Override
    protected void registerCreate(String url, Javalin app, DataSource dataSource) {
        CoreProjectService coreProjectService = new CoreProjectService(new CoreProjectRepository(dataSource));
        ImportCoreProjectService service = new ImportCoreProjectService(new ImportCoreProjectRepository(coreProjectService));
        app.post(url, ctx -> importCoreProject(service, ctx));
    }

    private static void importCoreProject(ImportCoreProjectService service, Context ctx) {
        try {
            ImportCoreProjectRequestDto payload = ctx.bodyAsClass(ImportCoreProjectRequestDto.class);
            CoreProjectRepository.CoreProjectRecord coreProject = service.importPayload(payload);
            ctx.status(200).json(ImportCoreProjectResponseDto.from(coreProject));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }
}

