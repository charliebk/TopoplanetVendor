package com.vutron.backend.manager.requirementlevel.Query;

public final class RequirementLevelQueries {
    private RequirementLevelQueries() {
    }

    public static final String LIST_ALL = """
        SELECT id, core_project_id, code, name, description, factor, created_at, updated_at
        FROM requirementLevel
        ORDER BY core_project_id ASC, factor DESC, name ASC, id ASC
        """;

    public static final String LIST_BY_CORE_PROJECT_ID = """
        SELECT id, core_project_id, code, name, description, factor, created_at, updated_at
        FROM requirementLevel
        WHERE core_project_id = ?
        ORDER BY factor DESC, name ASC, id ASC
        """;

    public static final String INSERT_REQUIREMENT_LEVEL = """
        INSERT INTO requirementLevel (core_project_id, code, name, description, factor)
        VALUES (?, ?, ?, ?, ?)
        """;

    public static final String UPDATE_BY_ID = """
        UPDATE requirementLevel
        SET core_project_id = ?,
            code = ?,
            name = ?,
            description = ?,
            factor = ?,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = ?
        """;

    public static final String FIND_BY_ID = """
        SELECT id, core_project_id, code, name, description, factor, created_at, updated_at
        FROM requirementLevel
        WHERE id = ?
        LIMIT 1
        """;

    public static final String DELETE_BY_ID = """
        DELETE FROM requirementLevel
        WHERE id = ?
        """;
}