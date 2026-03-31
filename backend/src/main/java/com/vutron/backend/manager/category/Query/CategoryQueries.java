package com.vutron.backend.manager.category.Query;

public final class CategoryQueries {
    private CategoryQueries() {
    }

    public static final String LIST_ALL = """
        SELECT id, core_project_id, code, name, description, factor, created_at, updated_at
        FROM category
        ORDER BY core_project_id ASC, factor DESC, name ASC, id ASC
        """;

    public static final String LIST_BY_CORE_PROJECT_ID = """
        SELECT id, core_project_id, code, name, description, factor, created_at, updated_at
        FROM category
        WHERE core_project_id = ?
        ORDER BY factor DESC, name ASC, id ASC
        """;

    public static final String INSERT_CATEGORY = """
        INSERT INTO category (core_project_id, code, name, description, factor)
        VALUES (?, ?, ?, ?, ?)
        """;

    public static final String UPDATE_BY_ID = """
        UPDATE category
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
        FROM category
        WHERE id = ?
        LIMIT 1
        """;

    public static final String DELETE_BY_ID = """
        DELETE FROM category
        WHERE id = ?
        """;
}