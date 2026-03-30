package com.vutron.backend;

import com.vutron.backend.config.AppConfig;
import com.vutron.backend.config.Database;
import com.vutron.backend.controller.ControllerRegister;
import com.vutron.backend.db.DatabaseSchemaManager;
import com.vutron.backend.io.project.Export.Controller.ExportProjectController;
import com.vutron.backend.io.project.Import.Controller.ImportProjectController;
import com.vutron.backend.manager.appmessage.Controller.AppMessageController;
import com.vutron.backend.manager.health.Controller.HealthController;
import com.vutron.backend.manager.project.Controller.ProjectController;
import io.javalin.Javalin;

import javax.sql.DataSource;

public final class App {
    private App() {
    }

    public static void main(String[] args) {
        AppConfig config = AppConfig.load();
        DataSource dataSource = Database.createDataSource(config.dbPath());

        DatabaseSchemaManager.ensureSchema(dataSource);

        Javalin app = Javalin.create(javalinConfig ->
            javalinConfig.bundledPlugins.enableCors(cors ->
                cors.addRule(rule -> {
                    rule.anyHost();
                })
            )
        );

        // health
        new HealthController().register(
            ControllerRegister.EndpointType.GET,
            "/health",
            app,
            dataSource
        );

        // appmessage
        new AppMessageController().register(
            ControllerRegister.EndpointType.GET,
            "/api/app-message/hello-world",
            app,
            dataSource
        );

        // project
        new ProjectController().register(
            ControllerRegister.EndpointType.GET,
            "/api/projects",
            app,
            dataSource
        );
        new ProjectController().register(
            ControllerRegister.EndpointType.GET,
            "/api/project/{id}",
            app,
            dataSource
        );
        new ProjectController().register(
            ControllerRegister.EndpointType.POST,
            "/api/project",
            app,
            dataSource
        );
        new ProjectController().register(
            ControllerRegister.EndpointType.PUT,
            "/api/project/{id}",
            app,
            dataSource
        );
        new ProjectController().register(
            ControllerRegister.EndpointType.DELETE,
            "/api/project/{id}",
            app,
            dataSource
        );

        // io
        new ExportProjectController().register(
            ControllerRegister.EndpointType.POST,
            "/api/project/export",
            app,
            dataSource
        );
        new ImportProjectController().register(
            ControllerRegister.EndpointType.POST,
            "/api/project/import",
            app,
            dataSource
        );

        app.start(config.host(), config.port());
    }
}

