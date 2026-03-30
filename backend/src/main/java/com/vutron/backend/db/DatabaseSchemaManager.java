package com.vutron.backend.db;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseSchemaManager {
    private static final String SUPER_ADMIN_EMAIL = "topoplanet@topoplanet.net";
    private static final String SUPER_ADMIN_PASSWORD_HASH = "c700be3353d72efd8348a35cb1c8eb70c112f634f8e75eb5dcdbf77f64b3b46f";

    private static final String CREATE_PROJECTS_TABLE = """
        CREATE TABLE IF NOT EXISTS projects (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            code TEXT NOT NULL UNIQUE,
            name TEXT NOT NULL,
            description TEXT,
            is_deleted INTEGER NOT NULL DEFAULT 0,
            created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
        );
        """;

    private static final String CREATE_USERS_TABLE = """
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            project_id INTEGER,
            email TEXT NOT NULL,
            password_hash TEXT NOT NULL,
            is_super_admin INTEGER NOT NULL DEFAULT 0,
            is_active INTEGER NOT NULL DEFAULT 1,
            created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
            UNIQUE (project_id, email),
            UNIQUE (email, is_super_admin)
        );
        """;

    private static final String CREATE_APP_MESSAGES_TABLE = """
        CREATE TABLE IF NOT EXISTS app_messages (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            key TEXT NOT NULL UNIQUE,
            value TEXT NOT NULL,
            created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
        );
        """;

    private static final String CREATE_PROJECTS_ACTIVE_INDEX = """
        CREATE INDEX IF NOT EXISTS idx_projects_active
        ON projects(is_deleted);
        """;

    private static final String CREATE_USERS_PROJECT_INDEX = """
        CREATE INDEX IF NOT EXISTS idx_users_project_id
        ON users(project_id);
        """;

    private static final String CREATE_USERS_EMAIL_INDEX = """
        CREATE INDEX IF NOT EXISTS idx_users_email
        ON users(email);
        """;

    private static final String SEED_HELLO_WORLD = """
        INSERT INTO app_messages (key, value)
        VALUES ('HELLO_WORLD', 'Hola Mundo desde SQLite')
        ON CONFLICT(key) DO UPDATE SET value = excluded.value;
        """;

    private DatabaseSchemaManager() {
    }

    public static void ensureSchema(DataSource dataSource) {
        String seedSuperAdmin = """
            INSERT INTO users (project_id, email, password_hash, is_super_admin, is_active)
            VALUES (NULL, '%s', '%s', 1, 1)
            ON CONFLICT(email, is_super_admin)
            DO UPDATE SET password_hash = excluded.password_hash, is_active = 1;
            """.formatted(SUPER_ADMIN_EMAIL, SUPER_ADMIN_PASSWORD_HASH);

        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_PROJECTS_TABLE);
            statement.executeUpdate(CREATE_USERS_TABLE);
            statement.executeUpdate(CREATE_APP_MESSAGES_TABLE);

            statement.executeUpdate(CREATE_PROJECTS_ACTIVE_INDEX);
            statement.executeUpdate(CREATE_USERS_PROJECT_INDEX);
            statement.executeUpdate(CREATE_USERS_EMAIL_INDEX);

            statement.executeUpdate(SEED_HELLO_WORLD);
            statement.executeUpdate(seedSuperAdmin);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to ensure schema", e);
        }
    }
}
