package com.vutron.backend.manager.coreproject.Query;

public final class CoreProjectQueries {
    private CoreProjectQueries() {
    }

    public static final String TABLE_NAME = "coreProject";

    public static final String LIST_ACTIVE = """
        SELECT id, code, name, description, is_deleted, created_at, updated_at
        FROM coreProject
        WHERE is_deleted = 0
        ORDER BY id DESC
        """;

    public static final String INSERT_CORE_PROJECT = """
        INSERT INTO coreProject (code, name, description, is_deleted)
        VALUES (?, ?, ?, 0)
        """;

    public static final String SOFT_DELETE_CORE_PROJECT = """
        UPDATE coreProject
        SET is_deleted = 1,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = ?
        """;

    public static final String UPDATE_BY_ID = """
        UPDATE coreProject
        SET code = ?,
            name = ?,
            description = ?,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = ?
          AND is_deleted = 0
        """;

    public static final String FIND_BY_ID = """
        SELECT id, code, name, description, is_deleted, created_at, updated_at
        FROM coreProject
        WHERE id = ?
        LIMIT 1
        """;

    public static final String FIND_BY_CODE = """
        SELECT id, code, name, description, is_deleted, created_at, updated_at
        FROM coreProject
        WHERE code = ?
        LIMIT 1
        """;

    public static final String UPSERT_BY_CODE = """
        INSERT INTO coreProject (code, name, description, is_deleted)
        VALUES (?, ?, ?, 0)
        ON CONFLICT(code)
        DO UPDATE SET
            name = excluded.name,
            description = excluded.description,
            is_deleted = 0,
            updated_at = CURRENT_TIMESTAMP
        """;

    public static final String LIST_CORE_PROJECT_USERS = """
        SELECT id, core_project_id, email, password_hash, is_super_admin, is_active
        FROM users
        WHERE core_project_id = ?
          AND is_super_admin = 0
        ORDER BY id ASC
        """;

    public static final String DELETE_CORE_PROJECT_USERS = """
        DELETE FROM users
        WHERE core_project_id = ?
          AND is_super_admin = 0
        """;

    public static final String INSERT_CORE_PROJECT_USER = """
        INSERT INTO users (core_project_id, email, password_hash, is_super_admin, is_active)
        VALUES (?, ?, ?, 0, ?)
        """;
}
