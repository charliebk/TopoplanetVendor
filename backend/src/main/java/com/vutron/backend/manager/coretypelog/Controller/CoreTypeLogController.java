package com.vutron.backend.manager.coretypelog.Controller;

import com.vutron.backend.controller.CrudCapabilitiesController;
import com.vutron.backend.manager.coretypelog.DTO.CreateCoreTypeLogRequestDto;
import com.vutron.backend.manager.coretypelog.DTO.CreateCoreTypeLogResponseDto;
import com.vutron.backend.manager.coretypelog.DTO.GetCoreTypeLogByIdResponseDto;
import com.vutron.backend.manager.coretypelog.DTO.ListCoreTypeLogResponseDto;
import com.vutron.backend.manager.coretypelog.DTO.UpdateCoreTypeLogRequestDto;
import com.vutron.backend.manager.coretypelog.DTO.UpdateCoreTypeLogResponseDto;
import com.vutron.backend.manager.coretypelog.Repository.CoreTypeLogRepository;
import com.vutron.backend.manager.coretypelog.Services.CoreTypeLogService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

public final class CoreTypeLogController extends CrudCapabilitiesController {

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
        CoreTypeLogService service = buildService(dataSource);
        app.get(url, ctx -> listCoreTypeLogs(service, ctx));
    }

    @Override
    protected void registerGetById(String url, Javalin app, DataSource dataSource) {
        CoreTypeLogService service = buildService(dataSource);
        app.get(url, ctx -> getCoreTypeLogById(service, ctx));
    }

    @Override
    protected void registerCreate(String url, Javalin app, DataSource dataSource) {
        CoreTypeLogService service = buildService(dataSource);
        app.post(url, ctx -> createCoreTypeLog(service, ctx));
    }

    @Override
    protected void registerUpdate(String url, Javalin app, DataSource dataSource) {
        CoreTypeLogService service = buildService(dataSource);
        app.put(url, ctx -> updateCoreTypeLog(service, ctx));
    }

    @Override
    protected void registerDelete(String url, Javalin app, DataSource dataSource) {
        CoreTypeLogService service = buildService(dataSource);
        app.delete(url, ctx -> deleteCoreTypeLog(service, ctx));
    }

    private static CoreTypeLogService buildService(DataSource dataSource) {
        return new CoreTypeLogService(new CoreTypeLogRepository(dataSource));
    }

    private static void listCoreTypeLogs(CoreTypeLogService service, Context ctx) {
        List<CoreTypeLogRepository.CoreTypeLogRecord> records = service.listAll();
        List<ListCoreTypeLogResponseDto> response = records.stream().map(ListCoreTypeLogResponseDto::from).toList();
        ctx.status(200).json(response);
    }

    private static void getCoreTypeLogById(CoreTypeLogService service, Context ctx) {
        Long id = parseCoreTypeLogId(ctx);
        if (id == null) {
            return;
        }

        CoreTypeLogRepository.CoreTypeLogRecord record = service.getById(id);
        if (record == null) {
            ctx.status(404).json(Map.of("error", "Core type log not found"));
            return;
        }

        ctx.status(200).json(GetCoreTypeLogByIdResponseDto.from(record));
    }

    private static void createCoreTypeLog(CoreTypeLogService service, Context ctx) {
        try {
            CreateCoreTypeLogRequestDto request = ctx.bodyAsClass(CreateCoreTypeLogRequestDto.class);
            CoreTypeLogRepository.CoreTypeLogRecord created = service.create(request);
            ctx.status(201).json(CreateCoreTypeLogResponseDto.from(created));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    private static void updateCoreTypeLog(CoreTypeLogService service, Context ctx) {
        Long id = parseCoreTypeLogId(ctx);
        if (id == null) {
            return;
        }

        try {
            UpdateCoreTypeLogRequestDto request = ctx.bodyAsClass(UpdateCoreTypeLogRequestDto.class);
            CoreTypeLogRepository.CoreTypeLogRecord updated = service.updateById(
                id,
                request.code(),
                request.name(),
                request.description()
            );

            if (updated == null) {
                ctx.status(404).json(Map.of("error", "Core type log not found"));
                return;
            }

            ctx.status(200).json(UpdateCoreTypeLogResponseDto.from(updated));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    private static void deleteCoreTypeLog(CoreTypeLogService service, Context ctx) {
        Long id = parseCoreTypeLogId(ctx);
        if (id == null) {
            return;
        }

        boolean deleted = service.deleteById(id);
        if (!deleted) {
            ctx.status(404).json(Map.of("error", "Core type log not found"));
            return;
        }

        ctx.status(204);
    }

    private static Long parseCoreTypeLogId(Context ctx) {
        try {
            return Long.parseLong(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("error", "Invalid core type log id"));
            return null;
        }
    }
}