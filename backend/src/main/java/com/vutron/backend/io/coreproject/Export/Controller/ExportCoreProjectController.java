package com.vutron.backend.io.coreproject.Export.Controller;

import com.vutron.backend.controller.CrudCapabilitiesController;
import com.vutron.backend.io.coreproject.Export.DTO.ExportCoreProjectRequestDto;
import com.vutron.backend.io.coreproject.Export.DTO.ExportCoreProjectResponseDto;
import com.vutron.backend.io.coreproject.Export.Query.ExportCoreProjectQueries;
import com.vutron.backend.io.coreproject.Export.Repository.ExportCoreProjectRepository;
import com.vutron.backend.io.coreproject.Export.Services.ExportCoreProjectService;
import com.vutron.backend.manager.coreproject.Repository.CoreProjectRepository;
import com.vutron.backend.manager.coreproject.Services.CoreProjectService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import javax.sql.DataSource;
import java.util.Map;

public final class ExportCoreProjectController extends CrudCapabilitiesController {

    @Override
    protected boolean supportsCreate() {
        return true;
    }

    @Override
    protected void registerCreate(String url, Javalin app, DataSource dataSource) {
        CoreProjectService coreProjectService = new CoreProjectService(new CoreProjectRepository(dataSource));
        ExportCoreProjectService service = new ExportCoreProjectService(new ExportCoreProjectRepository(coreProjectService));
        app.post(url, ctx -> exportCoreProject(service, ctx));
    }

    private static void exportCoreProject(ExportCoreProjectService service, Context ctx) {
        ExportCoreProjectRequestDto request = ctx.bodyAsClass(ExportCoreProjectRequestDto.class);

        if (request == null || request.coreProjectCode() == null || request.coreProjectCode().trim().isEmpty()) {
            ctx.status(400).json(Map.of("error", ExportCoreProjectQueries.CORE_PROJECT_CODE_REQUIRED));
            return;
        }

        ExportCoreProjectResponseDto payload = service.exportByCoreProjectCode(request.coreProjectCode().trim());

        if (payload == null) {
            ctx.status(404).json(Map.of("error", ExportCoreProjectQueries.CORE_PROJECT_NOT_FOUND));
            return;
        }

        ctx.status(200).json(payload);
    }
}

