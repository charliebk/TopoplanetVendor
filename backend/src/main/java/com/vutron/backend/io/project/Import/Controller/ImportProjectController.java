package com.vutron.backend.io.project.Import.Controller;

import com.vutron.backend.controller.CrudCapabilitiesController;
import com.vutron.backend.io.project.Import.DTO.ImportProjectRequestDto;
import com.vutron.backend.io.project.Import.DTO.ImportProjectResponseDto;
import com.vutron.backend.io.project.Import.Repository.ImportProjectRepository;
import com.vutron.backend.io.project.Import.Services.ImportProjectService;
import com.vutron.backend.manager.project.Repository.ProjectRepository;
import com.vutron.backend.manager.project.Services.ProjectService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import javax.sql.DataSource;
import java.util.Map;

public final class ImportProjectController extends CrudCapabilitiesController {

    @Override
    protected boolean supportsCreate() {
        return true;
    }

    @Override
    protected void registerCreate(String url, Javalin app, DataSource dataSource) {
        ProjectService projectService = new ProjectService(new ProjectRepository(dataSource));
        ImportProjectService service = new ImportProjectService(new ImportProjectRepository(projectService));
        app.post(url, ctx -> importProject(service, ctx));
    }

    private static void importProject(ImportProjectService service, Context ctx) {
        try {
            ImportProjectRequestDto payload = ctx.bodyAsClass(ImportProjectRequestDto.class);
            ProjectRepository.ProjectRecord project = service.importPayload(payload);
            ctx.status(200).json(ImportProjectResponseDto.from(project));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }
}

