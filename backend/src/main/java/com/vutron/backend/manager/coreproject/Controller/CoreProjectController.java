package com.vutron.backend.manager.coreproject.Controller;

import com.vutron.backend.controller.CrudCapabilitiesController;
import com.vutron.backend.manager.coreproject.DTO.CreateCoreProjectRequestDto;
import com.vutron.backend.manager.coreproject.DTO.CreateCoreProjectResponseDto;
import com.vutron.backend.manager.coreproject.DTO.GetCoreProjectByIdResponseDto;
import com.vutron.backend.manager.coreproject.DTO.ListCoreProjectResponseDto;
import com.vutron.backend.manager.coreproject.DTO.UpdateCoreProjectRequestDto;
import com.vutron.backend.manager.coreproject.DTO.UpdateCoreProjectResponseDto;
import com.vutron.backend.manager.coreproject.Repository.CoreProjectRepository;
import com.vutron.backend.manager.coreproject.Services.CoreProjectService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

public final class CoreProjectController extends CrudCapabilitiesController {

    @Override
    protected boolean supportsList() {
        return true;
    }

    @Override
    protected boolean supportsGetById() {
        return true;
    }

    @Override
    protected boolean supportsCreate() {
        return true;
    }

    @Override
    protected boolean supportsUpdate() {
        return true;
    }

    @Override
    protected boolean supportsDelete() {
        return true;
    }

    @Override
    protected void registerList(String url, Javalin app, DataSource dataSource) {
        CoreProjectService service = buildService(dataSource);
        app.get(url, ctx -> listCoreProjects(service, ctx));
    }

    @Override
    protected void registerGetById(String url, Javalin app, DataSource dataSource) {
        CoreProjectService service = buildService(dataSource);
        app.get(url, ctx -> getCoreProjectById(service, ctx));
    }

    @Override
    protected void registerCreate(String url, Javalin app, DataSource dataSource) {
        CoreProjectService service = buildService(dataSource);
        app.post(url, ctx -> createCoreProject(service, ctx));
    }

    @Override
    protected void registerUpdate(String url, Javalin app, DataSource dataSource) {
        CoreProjectService service = buildService(dataSource);
        app.put(url, ctx -> updateCoreProject(service, ctx));
    }

    @Override
    protected void registerDelete(String url, Javalin app, DataSource dataSource) {
        CoreProjectService service = buildService(dataSource);
        app.delete(url, ctx -> deleteCoreProject(service, ctx));
    }

    private static CoreProjectService buildService(DataSource dataSource) {
        return new CoreProjectService(new CoreProjectRepository(dataSource));
    }

    private static void getCoreProjectById(CoreProjectService service, Context ctx) {
        Long coreProjectId = parseCoreProjectId(ctx);
        if (coreProjectId == null) {
            return;
        }

        CoreProjectRepository.CoreProjectRecord coreProject = service.getById(coreProjectId);
        if (coreProject == null || coreProject.deleted()) {
            ctx.status(404).json(Map.of("error", "Core project not found"));
            return;
        }

        ctx.status(200).json(GetCoreProjectByIdResponseDto.from(coreProject));
    }

    private static void updateCoreProject(CoreProjectService service, Context ctx) {
        Long coreProjectId = parseCoreProjectId(ctx);
        if (coreProjectId == null) {
            return;
        }

        try {
            UpdateCoreProjectRequestDto request = ctx.bodyAsClass(UpdateCoreProjectRequestDto.class);
            CoreProjectRepository.CoreProjectRecord updated = service.updateById(
                coreProjectId,
                request.code(),
                request.name(),
                request.description()
            );

            if (updated == null) {
                ctx.status(404).json(Map.of("error", "Core project not found"));
                return;
            }

            ctx.status(200).json(UpdateCoreProjectResponseDto.from(updated));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    private static void listCoreProjects(CoreProjectService service, Context ctx) {
        List<CoreProjectRepository.CoreProjectRecord> coreProjects = service.listActive();
        List<ListCoreProjectResponseDto> response = coreProjects.stream().map(ListCoreProjectResponseDto::from).toList();
        ctx.status(200).json(response);
    }

    private static void createCoreProject(CoreProjectService service, Context ctx) {
        try {
            CreateCoreProjectRequestDto request = ctx.bodyAsClass(CreateCoreProjectRequestDto.class);
            CoreProjectRepository.CoreProjectRecord created = service.create(request);
            ctx.status(201).json(CreateCoreProjectResponseDto.from(created));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    private static void deleteCoreProject(CoreProjectService service, Context ctx) {
        Long coreProjectId = parseCoreProjectId(ctx);
        if (coreProjectId == null) {
            return;
        }

        boolean deleted = service.softDelete(coreProjectId);

        if (!deleted) {
            ctx.status(404).json(Map.of("error", "Core project not found"));
            return;
        }

        ctx.status(204);
    }

    private static Long parseCoreProjectId(Context ctx) {
        try {
            return Long.parseLong(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("error", "Invalid core project id"));
            return null;
        }
    }
}
