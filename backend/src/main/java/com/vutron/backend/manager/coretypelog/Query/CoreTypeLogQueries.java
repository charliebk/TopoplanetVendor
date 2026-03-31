package com.vutron.backend.manager.coretypelog.Query;

public final class CoreTypeLogQueries {
    private CoreTypeLogQueries() {
    }

    public static final String LIST_ALL = """
        SELECT id, code, name, description, created_at, updated_at
        FROM coreTypeLog
        ORDER BY name ASC, id ASC
        """;

    public static final String INSERT_CORE_TYPE_LOG = """
        INSERT INTO coreTypeLog (code, name, description)
        VALUES (?, ?, ?)
        """;

    public static final String UPDATE_BY_ID = """
        UPDATE coreTypeLog
        SET code = ?,
            name = ?,
            description = ?,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = ?
        """;

    public static final String FIND_BY_ID = """
        SELECT id, code, name, description, created_at, updated_at
        FROM coreTypeLog
        WHERE id = ?
        LIMIT 1
        """;

    public static final String FIND_BY_CODE = """
        SELECT id, code, name, description, created_at, updated_at
        FROM coreTypeLog
        WHERE code = ?
        LIMIT 1
        """;

    public static final String DELETE_BY_ID = """
        DELETE FROM coreTypeLog
        WHERE id = ?
        """;
}