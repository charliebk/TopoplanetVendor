package com.vutron.backend;

import com.vutron.backend.config.AppConfig;
import com.vutron.backend.config.Database;
import com.vutron.backend.controller.ControllerRegister;
import com.vutron.backend.db.DatabaseSchemaManager;
import com.vutron.backend.io.coreproject.Export.Controller.ExportCoreProjectController;
import com.vutron.backend.io.coreproject.Import.Controller.ImportCoreProjectController;
import com.vutron.backend.manager.coreproject.Controller.CoreProjectController;
import io.javalin.Javalin;

import javax.sql.DataSource;

public final class App {
    private App() {
    }

    public static void main(String[] args) {
        AppConfig config = AppConfig.load();
        DataSource dataSource = Database.createDataSource(config.dbPath());

        DatabaseSchemaManager.ensureSchema(dataSource, config.dbPath());

        Javalin app = Javalin.create(javalinConfig ->
            javalinConfig.bundledPlugins.enableCors(cors ->
                cors.addRule(rule -> {
                    rule.anyHost();
                })
            )
        );

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

        app.start(config.host(), config.port());
    }
}

