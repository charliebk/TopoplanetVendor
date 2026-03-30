package com.vutron.backend.io.project.Export.Controller;

import com.vutron.backend.controller.CrudCapabilitiesController;
import com.vutron.backend.io.project.Export.DTO.ExportProjectRequestDto;
import com.vutron.backend.io.project.Export.DTO.ExportProjectResponseDto;
import com.vutron.backend.io.project.Export.Query.ExportProjectQueries;
import com.vutron.backend.io.project.Export.Repository.ExportProjectRepository;
import com.vutron.backend.io.project.Export.Services.ExportProjectService;
import com.vutron.backend.manager.project.Repository.ProjectRepository;
import com.vutron.backend.manager.project.Services.ProjectService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import javax.sql.DataSource;
import java.util.Map;

public final class ExportProjectController extends CrudCapabilitiesController {

    @Override
    protected boolean supportsCreate() {
        return true;
    }

    @Override
    protected void registerCreate(String url, Javalin app, DataSource dataSource) {
        ProjectService projectService = new ProjectService(new ProjectRepository(dataSource));
        ExportProjectService service = new ExportProjectService(new ExportProjectRepository(projectService));
        app.post(url, ctx -> exportProject(service, ctx));
    }

    private static void exportProject(ExportProjectService service, Context ctx) {
        ExportProjectRequestDto request = ctx.bodyAsClass(ExportProjectRequestDto.class);

        if (request == null || request.projectCode() == null || request.projectCode().trim().isEmpty()) {
            ctx.status(400).json(Map.of("error", ExportProjectQueries.PROJECT_CODE_REQUIRED));
            return;
        }

        ExportProjectResponseDto payload = service.exportByProjectCode(request.projectCode().trim());

        if (payload == null) {
            ctx.status(404).json(Map.of("error", ExportProjectQueries.PROJECT_NOT_FOUND));
            return;
        }

        ctx.status(200).json(payload);
    }
}

