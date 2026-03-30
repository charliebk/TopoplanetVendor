package com.vutron.backend.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

public final class Database {
    private Database() {
    }

    public static DataSource createDataSource(Path dbFilePath) {
        try {
            Path parent = dbFilePath.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not create DB directory", e);
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:sqlite:" + dbFilePath.toAbsolutePath());
        hikariConfig.setMaximumPoolSize(5);
        hikariConfig.setPoolName("vutron-sqlite-pool");

        return new HikariDataSource(hikariConfig);
    }

    public static boolean isHealthy(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(1);
        } catch (SQLException e) {
            return false;
        }
    }
}
