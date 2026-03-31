package com.vutron.backend.manager.requirementlevel.Controller;

import com.vutron.backend.controller.CrudCapabilitiesController;
import com.vutron.backend.manager.requirementlevel.DTO.CreateRequirementLevelRequestDto;
import com.vutron.backend.manager.requirementlevel.DTO.CreateRequirementLevelResponseDto;
import com.vutron.backend.manager.requirementlevel.DTO.GetRequirementLevelByIdResponseDto;
import com.vutron.backend.manager.requirementlevel.DTO.ListRequirementLevelResponseDto;
import com.vutron.backend.manager.requirementlevel.DTO.UpdateRequirementLevelRequestDto;
import com.vutron.backend.manager.requirementlevel.DTO.UpdateRequirementLevelResponseDto;
import com.vutron.backend.manager.requirementlevel.Repository.RequirementLevelRepository;
import com.vutron.backend.manager.requirementlevel.Services.RequirementLevelService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

public final class RequirementLevelController extends CrudCapabilitiesController {

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
        RequirementLevelService service = buildService(dataSource);
        app.get(url, ctx -> listRequirementLevels(service, ctx));
    }

    @Override
    protected void registerGetById(String url, Javalin app, DataSource dataSource) {
        RequirementLevelService service = buildService(dataSource);
        app.get(url, ctx -> getRequirementLevelById(service, ctx));
    }

    @Override
    protected void registerCreate(String url, Javalin app, DataSource dataSource) {
        RequirementLevelService service = buildService(dataSource);
        app.post(url, ctx -> createRequirementLevel(service, ctx));
    }

    @Override
    protected void registerUpdate(String url, Javalin app, DataSource dataSource) {
        RequirementLevelService service = buildService(dataSource);
        app.put(url, ctx -> updateRequirementLevel(service, ctx));
    }

    @Override
    protected void registerDelete(String url, Javalin app, DataSource dataSource) {
        RequirementLevelService service = buildService(dataSource);
        app.delete(url, ctx -> deleteRequirementLevel(service, ctx));
    }

    private static RequirementLevelService buildService(DataSource dataSource) {
        return new RequirementLevelService(new RequirementLevelRepository(dataSource));
    }

    private static void listRequirementLevels(RequirementLevelService service, Context ctx) {
        Long coreProjectId = parseOptionalLong(ctx.queryParam("coreProjectId"), ctx, "Invalid core project id");
        if (ctx.status().getCode() == 400) {
            return;
        }

        List<RequirementLevelRepository.RequirementLevelRecord> records = service.listAll(coreProjectId);
        List<ListRequirementLevelResponseDto> response = records.stream().map(ListRequirementLevelResponseDto::from).toList();
        ctx.status(200).json(response);
    }

    private static void getRequirementLevelById(RequirementLevelService service, Context ctx) {
        Long id = parseRequirementLevelId(ctx);
        if (id == null) {
            return;
        }

        RequirementLevelRepository.RequirementLevelRecord record = service.getById(id);
        if (record == null) {
            ctx.status(404).json(Map.of("error", "Requirement level not found"));
            return;
        }

        ctx.status(200).json(GetRequirementLevelByIdResponseDto.from(record));
    }

    private static void createRequirementLevel(RequirementLevelService service, Context ctx) {
        try {
            CreateRequirementLevelRequestDto request = ctx.bodyAsClass(CreateRequirementLevelRequestDto.class);
            RequirementLevelRepository.RequirementLevelRecord created = service.create(request);
            ctx.status(201).json(CreateRequirementLevelResponseDto.from(created));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    private static void updateRequirementLevel(RequirementLevelService service, Context ctx) {
        Long id = parseRequirementLevelId(ctx);
        if (id == null) {
            return;
        }

        try {
            UpdateRequirementLevelRequestDto request = ctx.bodyAsClass(UpdateRequirementLevelRequestDto.class);
            RequirementLevelRepository.RequirementLevelRecord updated = service.updateById(
                id,
                request.coreProjectId(),
                request.code(),
                request.name(),
                request.description(),
                request.factor()
            );

            if (updated == null) {
                ctx.status(404).json(Map.of("error", "Requirement level not found"));
                return;
            }

            ctx.status(200).json(UpdateRequirementLevelResponseDto.from(updated));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    private static void deleteRequirementLevel(RequirementLevelService service, Context ctx) {
        Long id = parseRequirementLevelId(ctx);
        if (id == null) {
            return;
        }

        boolean deleted = service.deleteById(id);
        if (!deleted) {
            ctx.status(404).json(Map.of("error", "Requirement level not found"));
            return;
        }

        ctx.status(204);
    }

    private static Long parseRequirementLevelId(Context ctx) {
        try {
            return Long.parseLong(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("error", "Invalid requirement level id"));
            return null;
        }
    }

    private static Long parseOptionalLong(String value, Context ctx, String message) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("error", message));
            return null;
        }
    }
}