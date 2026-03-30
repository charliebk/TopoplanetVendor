package com.vutron.backend.manager.health.Controller;

import com.vutron.backend.config.Database;
import com.vutron.backend.controller.CrudCapabilitiesController;
import io.javalin.Javalin;
import io.javalin.http.Context;

import javax.sql.DataSource;
import java.util.Map;

public final class HealthController extends CrudCapabilitiesController {

    @Override
    protected boolean supportsList() {
        return true;
    }

    @Override
    protected void registerList(String url, Javalin app, DataSource dataSource) {
        app.get(url, ctx -> getHealthStatus(dataSource, ctx));
    }

    private static void getHealthStatus(DataSource dataSource, Context ctx) {
        boolean dbHealthy = Database.isHealthy(dataSource);
        int statusCode = dbHealthy ? 200 : 503;

        ctx.status(statusCode).json(Map.of(
            "status", dbHealthy ? "ok" : "degraded",
            "service", "vutron-backend",
            "database", dbHealthy ? "ok" : "down"
        ));
    }
}
