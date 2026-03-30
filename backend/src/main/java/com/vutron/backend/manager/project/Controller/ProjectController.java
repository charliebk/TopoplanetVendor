package com.vutron.backend.manager.project.Controller;

import com.vutron.backend.controller.CrudCapabilitiesController;
import com.vutron.backend.manager.project.DTO.CreateProjectRequestDto;
import com.vutron.backend.manager.project.DTO.CreateProjectResponseDto;
import com.vutron.backend.manager.project.DTO.GetProjectByIdResponseDto;
import com.vutron.backend.manager.project.DTO.ListProjectResponseDto;
import com.vutron.backend.manager.project.DTO.UpdateProjectRequestDto;
import com.vutron.backend.manager.project.DTO.UpdateProjectResponseDto;
import com.vutron.backend.manager.project.Repository.ProjectRepository;
import com.vutron.backend.manager.project.Services.ProjectService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

public final class ProjectController extends CrudCapabilitiesController {

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
        ProjectService service = buildService(dataSource);
        app.get(url, ctx -> listProjects(service, ctx));
    }

    @Override
    protected void registerGetById(String url, Javalin app, DataSource dataSource) {
        ProjectService service = buildService(dataSource);
        app.get(url, ctx -> getProjectById(service, ctx));
    }

    @Override
    protected void registerCreate(String url, Javalin app, DataSource dataSource) {
        ProjectService service = buildService(dataSource);
        app.post(url, ctx -> createProject(service, ctx));
    }

    @Override
    protected void registerUpdate(String url, Javalin app, DataSource dataSource) {
        ProjectService service = buildService(dataSource);
        app.put(url, ctx -> updateProject(service, ctx));
    }

    @Override
    protected void registerDelete(String url, Javalin app, DataSource dataSource) {
        ProjectService service = buildService(dataSource);
        app.delete(url, ctx -> deleteProject(service, ctx));
    }

    private static ProjectService buildService(DataSource dataSource) {
        return new ProjectService(new ProjectRepository(dataSource));
    }

    private static void getProjectById(ProjectService service, Context ctx) {
        Long projectId = parseProjectId(ctx);
        if (projectId == null) {
            return;
        }

        ProjectRepository.ProjectRecord project = service.getById(projectId);
        if (project == null || project.deleted()) {
            ctx.status(404).json(Map.of("error", "Project not found"));
            return;
        }

        ctx.status(200).json(GetProjectByIdResponseDto.from(project));
    }

    private static void updateProject(ProjectService service, Context ctx) {
        Long projectId = parseProjectId(ctx);
        if (projectId == null) {
            return;
        }

        try {
            UpdateProjectRequestDto request = ctx.bodyAsClass(UpdateProjectRequestDto.class);
            ProjectRepository.ProjectRecord updated = service.updateById(
                projectId,
                request.code(),
                request.name(),
                request.description()
            );

            if (updated == null) {
                ctx.status(404).json(Map.of("error", "Project not found"));
                return;
            }

            ctx.status(200).json(UpdateProjectResponseDto.from(updated));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    private static void listProjects(ProjectService service, Context ctx) {
        List<ProjectRepository.ProjectRecord> projects = service.listActive();
        List<ListProjectResponseDto> response = projects.stream().map(ListProjectResponseDto::from).toList();
        ctx.status(200).json(response);
    }

    private static void createProject(ProjectService service, Context ctx) {
        try {
            CreateProjectRequestDto request = ctx.bodyAsClass(CreateProjectRequestDto.class);
            ProjectRepository.ProjectRecord created = service.create(request);
            ctx.status(201).json(CreateProjectResponseDto.from(created));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    private static void deleteProject(ProjectService service, Context ctx) {
        Long projectId = parseProjectId(ctx);
        if (projectId == null) {
            return;
        }

        boolean deleted = service.softDelete(projectId);

        if (!deleted) {
            ctx.status(404).json(Map.of("error", "Project not found"));
            return;
        }

        ctx.status(204);
    }

    private static Long parseProjectId(Context ctx) {
        try {
            return Long.parseLong(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("error", "Invalid project id"));
            return null;
        }
    }
}
