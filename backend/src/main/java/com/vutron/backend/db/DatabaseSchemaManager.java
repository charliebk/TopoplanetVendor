package com.vutron.backend.db;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseSchemaManager {
    private static final String SCHEMA_FILENAME = "schema.sql";
    private static final String TEST_DATA_FILENAME = "test-data.sql";

    private DatabaseSchemaManager() {
    }

    public static void ensureSchema(DataSource dataSource, Path dbPath) {
        applyV001Schema(dataSource, dbPath);
    }

    public static void recreateSchema(DataSource dataSource, Path dbPath) {
        dropAllObjects(dataSource);
        applyScript(dataSource, resolveSqlPath(dbPath, SCHEMA_FILENAME));
    }

    public static void recreateSchemaAndData(DataSource dataSource, Path dbPath) {
        dropAllObjects(dataSource);
        applyScript(dataSource, resolveSqlPath(dbPath, SCHEMA_FILENAME));
        applyScript(dataSource, resolveSqlPath(dbPath, TEST_DATA_FILENAME));
    }

    private static void applyV001Schema(DataSource dataSource, Path dbPath) {
        applyScript(dataSource, resolveSqlPath(dbPath, SCHEMA_FILENAME));
    }

    private static void applyScript(DataSource dataSource, Path scriptPath) {
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

    private static void dropAllObjects(DataSource dataSource) {
        List<SchemaObject> schemaObjects = readSchemaObjects(dataSource);

        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = OFF");

            for (SchemaObject schemaObject : schemaObjects) {
                statement.execute("DROP " + schemaObject.typeKeyword() + " IF EXISTS \"" + schemaObject.name() + "\"");
            }

            statement.execute("DELETE FROM sqlite_sequence");
            statement.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to drop existing schema objects", e);
        }
    }

    private static List<SchemaObject> readSchemaObjects(DataSource dataSource) {
        String sql = """
            SELECT type, name
            FROM sqlite_master
            WHERE name NOT LIKE 'sqlite_%'
              AND type IN ('table', 'view', 'trigger')
            """;

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            List<SchemaObject> schemaObjects = new ArrayList<>();

            while (resultSet.next()) {
                schemaObjects.add(new SchemaObject(
                    resultSet.getString("type"),
                    resultSet.getString("name")
                ));
            }

            schemaObjects.sort(Comparator.comparingInt(SchemaObject::dropOrder).reversed());
            return schemaObjects;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to inspect existing schema objects", e);
        }
    }

    private static Path resolveSqlPath(Path dbPath, String filename) {
        Path dbDir = dbPath.toAbsolutePath().normalize().getParent();
        if (dbDir == null) {
            throw new IllegalStateException("Could not resolve DB directory from path: " + dbPath);
        }

        Path scriptPath = dbDir.resolve("sql").resolve(filename).normalize();
        if (!Files.exists(scriptPath)) {
            throw new IllegalStateException("SQL script not found: " + scriptPath);
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

    private record SchemaObject(String type, String name) {
        private String typeKeyword() {
            return switch (type) {
                case "table" -> "TABLE";
                case "view" -> "VIEW";
                case "trigger" -> "TRIGGER";
                default -> throw new IllegalStateException("Unsupported schema object type: " + type);
            };
        }

        private int dropOrder() {
            return switch (type) {
                case "trigger" -> 3;
                case "view" -> 2;
                case "table" -> 1;
                default -> 0;
            };
        }
    }
}
