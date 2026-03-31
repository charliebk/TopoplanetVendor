package com.vutron.backend.manager.project.Query;

public final class ProjectQueries {
    private ProjectQueries() {
    }

    public static final String TABLE_NAME = "project";

    public static final String LIST_ACTIVE = """
        SELECT id, code, name, description, is_deleted, created_at, updated_at
        FROM project
        WHERE is_deleted = 0
        ORDER BY id DESC
        """;

    public static final String INSERT_PROJECT = """
        INSERT INTO project (code, name, description, is_deleted)
        VALUES (?, ?, ?, 0)
        """;

    public static final String SOFT_DELETE_PROJECT = """
        UPDATE project
        SET is_deleted = 1,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = ?
        """;

    public static final String UPDATE_BY_ID = """
        UPDATE project
        SET code = ?,
            name = ?,
            description = ?,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = ?
          AND is_deleted = 0
        """;

    public static final String FIND_BY_ID = """
        SELECT id, code, name, description, is_deleted, created_at, updated_at
        FROM project
        WHERE id = ?
        LIMIT 1
        """;

    public static final String FIND_BY_CODE = """
        SELECT id, code, name, description, is_deleted, created_at, updated_at
        FROM project
        WHERE code = ?
        LIMIT 1
        """;

    public static final String UPSERT_BY_CODE = """
        INSERT INTO project (code, name, description, is_deleted)
        VALUES (?, ?, ?, 0)
        ON CONFLICT(code)
        DO UPDATE SET
            name = excluded.name,
            description = excluded.description,
            is_deleted = 0,
            updated_at = CURRENT_TIMESTAMP
        """;

    public static final String LIST_PROJECT_USERS = """
        SELECT id, project_id, email, password_hash, is_super_admin, is_active
        FROM users
        WHERE project_id = ?
          AND is_super_admin = 0
        ORDER BY id ASC
        """;

    public static final String DELETE_PROJECT_USERS = """
        DELETE FROM users
        WHERE project_id = ?
          AND is_super_admin = 0
        """;

    public static final String INSERT_PROJECT_USER = """
        INSERT INTO users (project_id, email, password_hash, is_super_admin, is_active)
        VALUES (?, ?, ?, 0, ?)
        """;
}
