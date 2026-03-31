package com.vutron.backend;

import com.vutron.backend.config.AppConfig;
import com.vutron.backend.config.Database;
import com.vutron.backend.controller.ControllerRegister;
import com.vutron.backend.db.DatabaseSchemaManager;
import com.vutron.backend.io.coreproject.Export.Controller.ExportCoreProjectController;
import com.vutron.backend.io.coreproject.Import.Controller.ImportCoreProjectController;
import com.vutron.backend.manager.category.Controller.CategoryController;
import com.vutron.backend.manager.corelog.Controller.CoreLogController;
import com.vutron.backend.manager.coreproject.Controller.CoreProjectController;
import com.vutron.backend.manager.coretypelog.Controller.CoreTypeLogController;
import com.vutron.backend.manager.requirementlevel.Controller.RequirementLevelController;
import io.javalin.Javalin;

import javax.sql.DataSource;

public final class App {
    private static final String RECREATE_SCHEMA_ARG = "--recreate-schema";
    private static final String RECREATE_SCHEMA_AND_DATA_ARG = "--recreate-schema-and-data";

    private App() {
    }

    public static void main(String[] args) {
        AppConfig config = AppConfig.load();
        DataSource dataSource = Database.createDataSource(config.dbPath());

        if (shouldRecreateSchemaAndData(args)) {
            DatabaseSchemaManager.recreateSchemaAndData(dataSource, config.dbPath());
            return;
        }

        if (shouldRecreateSchema(args)) {
            DatabaseSchemaManager.recreateSchema(dataSource, config.dbPath());
            return;
        }

        DatabaseSchemaManager.ensureSchema(dataSource, config.dbPath());

        Javalin app = Javalin.create(javalinConfig ->
            javalinConfig.bundledPlugins.enableCors(cors ->
                cors.addRule(rule -> {
                    rule.anyHost();
                })
            )
        );

        app.get("/health", ctx -> {
            boolean healthy = Database.isHealthy(dataSource);
            ctx.status(healthy ? 200 : 503).json(java.util.Map.of("healthy", healthy));
        });

        // core project
        new CoreProjectController().register(
            ControllerRegister.EndpointType.GET,
            "/api/core-projects",
            app,
            dataSource
        );
        new CoreProjectController().register(
            ControllerRegister.EndpointType.GET,
            "/api/core-project/{id}",
            app,
            dataSource
        );
        new CoreProjectController().register(
            ControllerRegister.EndpointType.POST,
            "/api/core-project",
            app,
            dataSource
        );
        new CoreProjectController().register(
            ControllerRegister.EndpointType.PUT,
            "/api/core-project/{id}",
            app,
            dataSource
        );
        new CoreProjectController().register(
            ControllerRegister.EndpointType.DELETE,
            "/api/core-project/{id}",
            app,
            dataSource
        );
        new ExportCoreProjectController().register(
            ControllerRegister.EndpointType.POST,
            "/api/core-project/export",
            app,
            dataSource
        );
        new ImportCoreProjectController().register(
            ControllerRegister.EndpointType.POST,
            "/api/core-project/import",
            app,
            dataSource
        );

        // core type log
        new CoreTypeLogController().register(
            ControllerRegister.EndpointType.GET,
            "/api/core-type-logs",
            app,
            dataSource
        );
        new CoreTypeLogController().register(
            ControllerRegister.EndpointType.GET,
            "/api/core-type-log/{id}",
            app,
            dataSource
        );
        new CoreTypeLogController().register(
            ControllerRegister.EndpointType.POST,
            "/api/core-type-log",
            app,
            dataSource
        );
        new CoreTypeLogController().register(
            ControllerRegister.EndpointType.PUT,
            "/api/core-type-log/{id}",
            app,
            dataSource
        );
        new CoreTypeLogController().register(
            ControllerRegister.EndpointType.DELETE,
            "/api/core-type-log/{id}",
            app,
            dataSource
        );

        // core log
        new CoreLogController().register(
            ControllerRegister.EndpointType.GET,
            "/api/core-logs",
            app,
            dataSource
        );
        new CoreLogController().register(
            ControllerRegister.EndpointType.GET,
            "/api/core-log/{id}",
            app,
            dataSource
        );
        new CoreLogController().register(
            ControllerRegister.EndpointType.POST,
            "/api/core-log",
            app,
            dataSource
        );
        new CoreLogController().register(
            ControllerRegister.EndpointType.PUT,
            "/api/core-log/{id}",
            app,
            dataSource
        );
        new CoreLogController().register(
            ControllerRegister.EndpointType.DELETE,
            "/api/core-log/{id}",
            app,
            dataSource
        );

        // category
        new CategoryController().register(
            ControllerRegister.EndpointType.GET,
            "/api/categories",
            app,
            dataSource
        );
        new CategoryController().register(
            ControllerRegister.EndpointType.GET,
            "/api/category/{id}",
            app,
            dataSource
        );
        new CategoryController().register(
            ControllerRegister.EndpointType.POST,
            "/api/category",
            app,
            dataSource
        );
        new CategoryController().register(
            ControllerRegister.EndpointType.PUT,
            "/api/category/{id}",
            app,
            dataSource
        );
        new CategoryController().register(
            ControllerRegister.EndpointType.DELETE,
            "/api/category/{id}",
            app,
            dataSource
        );

        // requirement level
        new RequirementLevelController().register(
            ControllerRegister.EndpointType.GET,
            "/api/requirement-levels",
            app,
            dataSource
        );
        new RequirementLevelController().register(
            ControllerRegister.EndpointType.GET,
            "/api/requirement-level/{id}",
            app,
            dataSource
        );
        new RequirementLevelController().register(
            ControllerRegister.EndpointType.POST,
            "/api/requirement-level",
            app,
            dataSource
        );
        new RequirementLevelController().register(
            ControllerRegister.EndpointType.PUT,
            "/api/requirement-level/{id}",
            app,
            dataSource
        );
        new RequirementLevelController().register(
            ControllerRegister.EndpointType.DELETE,
            "/api/requirement-level/{id}",
            app,
            dataSource
        );

        app.start(config.host(), config.port());
    }

    private static boolean shouldRecreateSchema(String[] args) {
        return hasArg(args, RECREATE_SCHEMA_ARG);
    }

    private static boolean shouldRecreateSchemaAndData(String[] args) {
        return hasArg(args, RECREATE_SCHEMA_AND_DATA_ARG);
    }

    private static boolean hasArg(String[] args, String expectedArg) {
        if (args == null) {
            return false;
        }

        for (String arg : args) {
            if (expectedArg.equals(arg)) {
                return true;
            }
        }

        return false;
    }
}

