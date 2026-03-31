package com.vutron.backend.manager.corelog.Controller;

import com.vutron.backend.controller.CrudCapabilitiesController;
import com.vutron.backend.manager.corelog.DTO.CreateCoreLogRequestDto;
import com.vutron.backend.manager.corelog.DTO.CreateCoreLogResponseDto;
import com.vutron.backend.manager.corelog.DTO.GetCoreLogByIdResponseDto;
import com.vutron.backend.manager.corelog.DTO.ListCoreLogResponseDto;
import com.vutron.backend.manager.corelog.DTO.UpdateCoreLogRequestDto;
import com.vutron.backend.manager.corelog.DTO.UpdateCoreLogResponseDto;
import com.vutron.backend.manager.corelog.Repository.CoreLogRepository;
import com.vutron.backend.manager.corelog.Services.CoreLogService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

public final class CoreLogController extends CrudCapabilitiesController {

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
        CoreLogService service = buildService(dataSource);
        app.get(url, ctx -> listCoreLogs(service, ctx));
    }

    @Override
    protected void registerGetById(String url, Javalin app, DataSource dataSource) {
        CoreLogService service = buildService(dataSource);
        app.get(url, ctx -> getCoreLogById(service, ctx));
    }

    @Override
    protected void registerCreate(String url, Javalin app, DataSource dataSource) {
        CoreLogService service = buildService(dataSource);
        app.post(url, ctx -> createCoreLog(service, ctx));
    }

    @Override
    protected void registerUpdate(String url, Javalin app, DataSource dataSource) {
        CoreLogService service = buildService(dataSource);
        app.put(url, ctx -> updateCoreLog(service, ctx));
    }

    @Override
    protected void registerDelete(String url, Javalin app, DataSource dataSource) {
        CoreLogService service = buildService(dataSource);
        app.delete(url, ctx -> deleteCoreLog(service, ctx));
    }

    private static CoreLogService buildService(DataSource dataSource) {
        return new CoreLogService(new CoreLogRepository(dataSource));
    }

    private static void listCoreLogs(CoreLogService service, Context ctx) {
        try {
            Long coreProjectId = parseOptionalCoreProjectId(ctx);
            List<CoreLogRepository.CoreLogRecord> records = service.list(coreProjectId);
            List<ListCoreLogResponseDto> response = records.stream().map(ListCoreLogResponseDto::from).toList();
            ctx.status(200).json(response);
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    private static void getCoreLogById(CoreLogService service, Context ctx) {
        Long id = parseCoreLogId(ctx);
        if (id == null) {
            return;
        }

        try {
            CoreLogRepository.CoreLogRecord record = service.getById(id);
            if (record == null) {
                ctx.status(404).json(Map.of("error", "Core log not found"));
                return;
            }

            ctx.status(200).json(GetCoreLogByIdResponseDto.from(record));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    private static void createCoreLog(CoreLogService service, Context ctx) {
        try {
            CreateCoreLogRequestDto request = ctx.bodyAsClass(CreateCoreLogRequestDto.class);
            CoreLogRepository.CoreLogRecord created = service.create(request);
            ctx.status(201).json(CreateCoreLogResponseDto.from(created));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    private static void updateCoreLog(CoreLogService service, Context ctx) {
        Long id = parseCoreLogId(ctx);
        if (id == null) {
            return;
        }

        try {
            UpdateCoreLogRequestDto request = ctx.bodyAsClass(UpdateCoreLogRequestDto.class);
            CoreLogRepository.CoreLogRecord updated = service.updateById(
                id,
                request.coreProjectId(),
                request.coreTypeLogId(),
                request.title(),
                request.message(),
                request.comment(),
                request.happenedAtUtc()
            );

            if (updated == null) {
                ctx.status(404).json(Map.of("error", "Core log not found"));
                return;
            }

            ctx.status(200).json(UpdateCoreLogResponseDto.from(updated));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    private static void deleteCoreLog(CoreLogService service, Context ctx) {
        Long id = parseCoreLogId(ctx);
        if (id == null) {
            return;
        }

        try {
            boolean deleted = service.deleteById(id);
            if (!deleted) {
                ctx.status(404).json(Map.of("error", "Core log not found"));
                return;
            }

            ctx.status(204);
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    private static Long parseCoreLogId(Context ctx) {
        try {
            return Long.parseLong(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("error", "Invalid core log id"));
            return null;
        }
    }

    private static Long parseOptionalCoreProjectId(Context ctx) {
        String value = ctx.queryParam("coreProjectId");
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid core project id");
        }
    }
}