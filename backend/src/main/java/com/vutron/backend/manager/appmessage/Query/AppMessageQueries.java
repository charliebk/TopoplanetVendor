package com.vutron.backend.manager.appmessage.Query;

public final class AppMessageQueries {
    private AppMessageQueries() {
    }

    public static final String FIND_BY_KEY = """
        SELECT key, value
        FROM app_messages
        WHERE key = ?
        LIMIT 1
        """;
}
