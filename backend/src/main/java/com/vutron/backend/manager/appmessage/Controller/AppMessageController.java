package com.vutron.backend.manager.appmessage.Controller;

import com.vutron.backend.controller.CrudCapabilitiesController;
import com.vutron.backend.manager.appmessage.DTO.GetHelloWorldResponseDto;
import com.vutron.backend.manager.appmessage.Repository.AppMessageRepository;
import com.vutron.backend.manager.appmessage.Services.AppMessageService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import javax.sql.DataSource;
import java.util.Map;

public final class AppMessageController extends CrudCapabilitiesController {

    @Override
    protected boolean supportsList() {
        return true;
    }

    @Override
    protected void registerList(String url, Javalin app, DataSource dataSource) {
        AppMessageService service = new AppMessageService(new AppMessageRepository(dataSource));
        app.get(url, ctx -> getHelloWorld(service, ctx));
    }

    private static void getHelloWorld(AppMessageService service, Context ctx) {
        GetHelloWorldResponseDto message = service.findHelloWorld();

        if (message == null) {
            ctx.status(404).json(Map.of(
                "error", "App message not found",
                "key", "HELLO_WORLD"
            ));
            return;
        }

        ctx.status(200).json(message);
    }
}
