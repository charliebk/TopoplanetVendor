package com.vutron.backend.manager.coreproject.Repository;

import com.vutron.backend.manager.coreproject.Query.CoreProjectQueries;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class CoreProjectRepository {
    private final DataSource dataSource;

    public CoreProjectRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<CoreProjectRecord> listActive() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CoreProjectQueries.LIST_ACTIVE);
             ResultSet resultSet = statement.executeQuery()) {
            List<CoreProjectRecord> coreProjects = new ArrayList<>();

            while (resultSet.next()) {
                coreProjects.add(mapCoreProject(resultSet));
            }

            return coreProjects;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list core projects", e);
        }
    }

    public CoreProjectRecord create(String code, String name, String description) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CoreProjectQueries.INSERT_CORE_PROJECT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, code);
            statement.setString(2, name);
            statement.setString(3, description);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new IllegalStateException("Could not retrieve generated core project id");
                }
                long id = keys.getLong(1);
                CoreProjectRecord coreProject = findById(id);
                if (coreProject == null) {
                    throw new IllegalStateException("Created core project not found");
                }
                return coreProject;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create core project", e);
        }
    }

    public boolean softDelete(long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CoreProjectQueries.SOFT_DELETE_CORE_PROJECT)) {
            statement.setLong(1, id);
            int affected = statement.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete core project", e);
        }
    }

    public CoreProjectRecord updateById(long id, String code, String name, String description) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CoreProjectQueries.UPDATE_BY_ID)) {
            statement.setString(1, code);
            statement.setString(2, name);
            statement.setString(3, description);
            statement.setLong(4, id);

            int affected = statement.executeUpdate();
            if (affected == 0) {
                return null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update core project", e);
        }

        return findById(id);
    }

    public CoreProjectRecord findById(long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CoreProjectQueries.FIND_BY_ID)) {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapCoreProject(resultSet);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to query core project by id", e);
        }
    }

    public CoreProjectRecord findByCode(String code) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CoreProjectQueries.FIND_BY_CODE)) {
            statement.setString(1, code);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapCoreProject(resultSet);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to query core project by code", e);
        }
    }

    public CoreProjectRecord upsertByCode(String code, String name, String description) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CoreProjectQueries.UPSERT_BY_CODE)) {
            statement.setString(1, code);
            statement.setString(2, name);
            statement.setString(3, description);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to upsert core project", e);
        }

        CoreProjectRecord record = findByCode(code);
        if (record == null) {
            throw new IllegalStateException("Upserted core project not found");
        }
        return record;
    }

    public List<CoreProjectUserRecord> listUsersByCoreProject(long coreProjectId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CoreProjectQueries.LIST_CORE_PROJECT_USERS)) {
            statement.setLong(1, coreProjectId);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<CoreProjectUserRecord> users = new ArrayList<>();

                while (resultSet.next()) {
                    users.add(new CoreProjectUserRecord(
                        resultSet.getLong("id"),
                        resultSet.getLong("core_project_id"),
                        resultSet.getString("email"),
                        resultSet.getString("password_hash"),
                        resultSet.getInt("is_active") == 1
                    ));
                }

                return users;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list core project users", e);
        }
    }

    public void replaceCoreProjectUsers(long coreProjectId, List<CoreProjectUserRecord> users) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement deleteStatement = connection.prepareStatement(CoreProjectQueries.DELETE_CORE_PROJECT_USERS)) {
                    deleteStatement.setLong(1, coreProjectId);
                    deleteStatement.executeUpdate();
                }

                try (PreparedStatement insertStatement = connection.prepareStatement(CoreProjectQueries.INSERT_CORE_PROJECT_USER)) {
                    for (CoreProjectUserRecord user : users) {
                        insertStatement.setLong(1, coreProjectId);
                        insertStatement.setString(2, user.email());
                        insertStatement.setString(3, user.passwordHash());
                        insertStatement.setInt(4, user.active() ? 1 : 0);
                        insertStatement.addBatch();
                    }
                    insertStatement.executeBatch();
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to replace core project users", e);
        }
    }

    private CoreProjectRecord mapCoreProject(ResultSet resultSet) throws SQLException {
        return new CoreProjectRecord(
            resultSet.getLong("id"),
            resultSet.getString("code"),
            resultSet.getString("name"),
            resultSet.getString("description"),
            resultSet.getInt("is_deleted") == 1,
            resultSet.getString("created_at"),
            resultSet.getString("updated_at")
        );
    }

    public record CoreProjectRecord(
        long id,
        String code,
        String name,
        String description,
        boolean deleted,
        String createdAt,
        String updatedAt
    ) {
    }

    public record CoreProjectUserRecord(
        long id,
        long coreProjectId,
        String email,
        String passwordHash,
        boolean active
    ) {
    }
}
