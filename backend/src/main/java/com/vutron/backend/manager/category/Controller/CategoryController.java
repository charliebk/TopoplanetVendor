package com.vutron.backend.manager.category.Controller;

import com.vutron.backend.controller.CrudCapabilitiesController;
import com.vutron.backend.manager.category.DTO.CreateCategoryRequestDto;
import com.vutron.backend.manager.category.DTO.CreateCategoryResponseDto;
import com.vutron.backend.manager.category.DTO.GetCategoryByIdResponseDto;
import com.vutron.backend.manager.category.DTO.ListCategoryResponseDto;
import com.vutron.backend.manager.category.DTO.UpdateCategoryRequestDto;
import com.vutron.backend.manager.category.DTO.UpdateCategoryResponseDto;
import com.vutron.backend.manager.category.Repository.CategoryRepository;
import com.vutron.backend.manager.category.Services.CategoryService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

public final class CategoryController extends CrudCapabilitiesController {

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
        CategoryService service = buildService(dataSource);
        app.get(url, ctx -> listCategories(service, ctx));
    }

    @Override
    protected void registerGetById(String url, Javalin app, DataSource dataSource) {
        CategoryService service = buildService(dataSource);
        app.get(url, ctx -> getCategoryById(service, ctx));
    }

    @Override
    protected void registerCreate(String url, Javalin app, DataSource dataSource) {
        CategoryService service = buildService(dataSource);
        app.post(url, ctx -> createCategory(service, ctx));
    }

    @Override
    protected void registerUpdate(String url, Javalin app, DataSource dataSource) {
        CategoryService service = buildService(dataSource);
        app.put(url, ctx -> updateCategory(service, ctx));
    }

    @Override
    protected void registerDelete(String url, Javalin app, DataSource dataSource) {
        CategoryService service = buildService(dataSource);
        app.delete(url, ctx -> deleteCategory(service, ctx));
    }

    private static CategoryService buildService(DataSource dataSource) {
        return new CategoryService(new CategoryRepository(dataSource));
    }

    private static void listCategories(CategoryService service, Context ctx) {
        Long coreProjectId = parseOptionalLong(ctx.queryParam("coreProjectId"), ctx, "Invalid core project id");
        if (ctx.status().getCode() == 400) {
            return;
        }

        List<CategoryRepository.CategoryRecord> records = service.listAll(coreProjectId);
        List<ListCategoryResponseDto> response = records.stream().map(ListCategoryResponseDto::from).toList();
        ctx.status(200).json(response);
    }

    private static void getCategoryById(CategoryService service, Context ctx) {
        Long id = parseCategoryId(ctx);
        if (id == null) {
            return;
        }

        CategoryRepository.CategoryRecord record = service.getById(id);
        if (record == null) {
            ctx.status(404).json(Map.of("error", "Category not found"));
            return;
        }

        ctx.status(200).json(GetCategoryByIdResponseDto.from(record));
    }

    private static void createCategory(CategoryService service, Context ctx) {
        try {
            CreateCategoryRequestDto request = ctx.bodyAsClass(CreateCategoryRequestDto.class);
            CategoryRepository.CategoryRecord created = service.create(request);
            ctx.status(201).json(CreateCategoryResponseDto.from(created));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    private static void updateCategory(CategoryService service, Context ctx) {
        Long id = parseCategoryId(ctx);
        if (id == null) {
            return;
        }

        try {
            UpdateCategoryRequestDto request = ctx.bodyAsClass(UpdateCategoryRequestDto.class);
            CategoryRepository.CategoryRecord updated = service.updateById(
                id,
                request.coreProjectId(),
                request.code(),
                request.name(),
                request.description(),
                request.factor()
            );

            if (updated == null) {
                ctx.status(404).json(Map.of("error", "Category not found"));
                return;
            }

            ctx.status(200).json(UpdateCategoryResponseDto.from(updated));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    private static void deleteCategory(CategoryService service, Context ctx) {
        Long id = parseCategoryId(ctx);
        if (id == null) {
            return;
        }

        boolean deleted = service.deleteById(id);
        if (!deleted) {
            ctx.status(404).json(Map.of("error", "Category not found"));
            return;
        }

        ctx.status(204);
    }

    private static Long parseCategoryId(Context ctx) {
        try {
            return Long.parseLong(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("error", "Invalid category id"));
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