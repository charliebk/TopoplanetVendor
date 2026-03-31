package com.vutron.backend.db;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseSchemaManager {
    private static final String V001_FILENAME = "V001__initial_schema.sql";

    private DatabaseSchemaManager() {
    }

    public static void ensureSchema(DataSource dataSource, Path dbPath) {
        applyV001Schema(dataSource, dbPath);
    }

    private static void applyV001Schema(DataSource dataSource, Path dbPath) {
        Path scriptPath = resolveV001Path(dbPath);
        String script;

        try {
            script = Files.readString(scriptPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read schema script: " + scriptPath, e);
        }

        List<String> statements = splitSqlStatements(script);

        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            for (String sql : statements) {
                statement.execute(sql);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to apply schema script: " + scriptPath, e);
        }
    }

    private static Path resolveV001Path(Path dbPath) {
        Path dbDir = dbPath.toAbsolutePath().normalize().getParent();
        if (dbDir == null) {
            throw new IllegalStateException("Could not resolve DB directory from path: " + dbPath);
        }

        Path scriptPath = dbDir.resolve("schema").resolve(V001_FILENAME).normalize();
        if (!Files.exists(scriptPath)) {
            throw new IllegalStateException("Schema script not found: " + scriptPath);
        }
        return scriptPath;
    }

    private static List<String> splitSqlStatements(String script) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;

        for (int i = 0; i < script.length(); i++) {
            char c = script.charAt(i);
            char next = i + 1 < script.length() ? script.charAt(i + 1) : '\0';

            if (!inSingleQuote && !inDoubleQuote && c == '-' && next == '-') {
                while (i < script.length() && script.charAt(i) != '\n') {
                    i++;
                }
                continue;
            }

            if (!inSingleQuote && !inDoubleQuote && c == '/' && next == '*') {
                i += 2;
                while (i < script.length() - 1) {
                    if (script.charAt(i) == '*' && script.charAt(i + 1) == '/') {
                        i++;
                        break;
                    }
                    i++;
                }
                continue;
            }

            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
            } else if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
            }

            if (c == ';' && !inSingleQuote && !inDoubleQuote) {
                String sql = current.toString().trim();
                if (!sql.isEmpty()) {
                    statements.add(sql);
                }
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        String remaining = current.toString().trim();
        if (!remaining.isEmpty()) {
            statements.add(remaining);
        }

        return statements;
    }
}
