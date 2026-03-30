package com.vutron.backend.controller;

import io.javalin.Javalin;

import javax.sql.DataSource;

public abstract class CrudCapabilitiesController implements ControllerRegister {
    @Override
    public final void register(EndpointType type, String url, Javalin app, DataSource dataSource) {
        if (isCollectionRoute(url) && type == EndpointType.GET && supportsList()) {
            registerList(url, app, dataSource);
            return;
        }

        if (isItemRoute(url) && type == EndpointType.GET && supportsGetById()) {
            registerGetById(url, app, dataSource);
            return;
        }

        if (isCollectionRoute(url) && type == EndpointType.POST && supportsCreate()) {
            registerCreate(url, app, dataSource);
            return;
        }

        if (isItemRoute(url)
            && (type == EndpointType.PUT || type == EndpointType.PATCH)
            && supportsUpdate()) {
            registerUpdate(url, app, dataSource);
            return;
        }

        if (isItemRoute(url) && type == EndpointType.DELETE && supportsDelete()) {
            registerDelete(url, app, dataSource);
            return;
        }

        throw new IllegalArgumentException(
            "Unsupported endpoint for " + getClass().getSimpleName() + ": " + type + " " + url
        );
    }

    protected boolean supportsList() {
        return false;
    }

    protected boolean supportsGetById() {
        return false;
    }

    protected boolean supportsCreate() {
        return false;
    }

    protected boolean supportsUpdate() {
        return false;
    }

    protected boolean supportsDelete() {
        return false;
    }

    protected void registerList(String url, Javalin app, DataSource dataSource) {
        throw unsupported("LIST");
    }

    protected void registerGetById(String url, Javalin app, DataSource dataSource) {
        throw unsupported("GET_BY_ID");
    }

    protected void registerCreate(String url, Javalin app, DataSource dataSource) {
        throw unsupported("CREATE");
    }

    protected void registerUpdate(String url, Javalin app, DataSource dataSource) {
        throw unsupported("UPDATE");
    }

    protected void registerDelete(String url, Javalin app, DataSource dataSource) {
        throw unsupported("DELETE");
    }

    private UnsupportedOperationException unsupported(String capability) {
        return new UnsupportedOperationException(
            getClass().getSimpleName() + " does not support capability: " + capability
        );
    }

    private static boolean isItemRoute(String url) {
        return url != null && url.contains("{") && url.contains("}");
    }

    private static boolean isCollectionRoute(String url) {
        return !isItemRoute(url);
    }
}