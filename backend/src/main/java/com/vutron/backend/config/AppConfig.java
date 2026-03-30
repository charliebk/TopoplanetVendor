package com.vutron.backend.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

public final class AppConfig {
    private final String host;
    private final int port;
    private final Path dbPath;

    private AppConfig(String host, int port, Path dbPath) {
        this.host = host;
        this.port = port;
        this.dbPath = dbPath;
    }

    public static AppConfig load() {
        Properties properties = new Properties();

        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not load application.properties", e);
        }

        String host = System.getenv().getOrDefault("APP_HOST", properties.getProperty("app.host", "127.0.0.1"));
        int port = Integer.parseInt(System.getenv().getOrDefault("APP_PORT", properties.getProperty("app.port", "7007")));
        String dbPathRaw = System.getenv().getOrDefault("APP_DB_PATH", properties.getProperty("app.db.path", "../db/app.sqlite"));

        return new AppConfig(host, port, Path.of(dbPathRaw).normalize());
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public Path dbPath() {
        return dbPath;
    }
}
