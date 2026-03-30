package com.vutron.backend.controller;

import io.javalin.Javalin;

import javax.sql.DataSource;

public interface ControllerRegister {
    enum EndpointType {
        GET,
        POST,
        PUT,
        PATCH,
        DELETE,
        OPTIONS,
        HEAD
    }

    void register(EndpointType type, String url, Javalin app, DataSource dataSource);
}