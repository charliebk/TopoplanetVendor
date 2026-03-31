package com.vutron.backend.manager.corelog.Query;

public final class CoreLogQueries {
    private CoreLogQueries() {
    }

    public static final String BASE_SELECT = """
        SELECT cl.id,
               cl.core_project_id,
               cl.core_type_log_id,
               ctl.code AS core_type_log_code,
               ctl.name AS core_type_log_name,
               cl.title,
               cl.message,
               cl.comment,
               cl.happened_at_utc,
               cl.created_at
        FROM coreLog cl
        INNER JOIN coreTypeLog ctl ON ctl.id = cl.core_type_log_id
        """;

    public static final String LIST_ALL = BASE_SELECT + """
        ORDER BY cl.happened_at_utc DESC, cl.id DESC
        """;

    public static final String LIST_BY_CORE_PROJECT_ID = BASE_SELECT + """
        WHERE cl.core_project_id = ?
        ORDER BY cl.happened_at_utc DESC, cl.id DESC
        """;

    public static final String FIND_BY_ID = BASE_SELECT + """
        WHERE cl.id = ?
        LIMIT 1
        """;

    public static final String INSERT_CORE_LOG = """
        INSERT INTO coreLog (
            core_project_id,
            core_type_log_id,
            title,
            message,
            comment,
            happened_at_utc
        )
        VALUES (?, ?, ?, ?, ?, COALESCE(?, STRFTIME('%Y-%m-%dT%H:%M:%fZ', 'now')))
        """;

    public static final String UPDATE_BY_ID = """
        UPDATE coreLog
        SET core_project_id = ?,
            core_type_log_id = ?,
            title = ?,
            message = ?,
            comment = ?,
            happened_at_utc = COALESCE(?, happened_at_utc)
        WHERE id = ?
        """;

    public static final String DELETE_BY_ID = """
        DELETE FROM coreLog
        WHERE id = ?
        """;

    public static final String EXISTS_CORE_PROJECT = """
        SELECT 1
        FROM coreProject
        WHERE id = ? AND is_deleted = 0
        LIMIT 1
        """;

    public static final String EXISTS_CORE_TYPE_LOG = """
        SELECT 1
        FROM coreTypeLog
        WHERE id = ?
        LIMIT 1
        """;
}