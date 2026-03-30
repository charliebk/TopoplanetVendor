package com.vutron.backend.manager.project.Repository;

import com.vutron.backend.manager.project.Query.ProjectQueries;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class ProjectRepository {
    private final DataSource dataSource;

    public ProjectRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<ProjectRecord> listActive() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(ProjectQueries.LIST_ACTIVE);
             ResultSet resultSet = statement.executeQuery()) {
            List<ProjectRecord> projects = new ArrayList<>();

            while (resultSet.next()) {
                projects.add(mapProject(resultSet));
            }

            return projects;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list projects", e);
        }
    }

    public ProjectRecord create(String code, String name, String description) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(ProjectQueries.INSERT_PROJECT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, code);
            statement.setString(2, name);
            statement.setString(3, description);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new IllegalStateException("Could not retrieve generated project id");
                }
                long id = keys.getLong(1);
                ProjectRecord project = findById(id);
                if (project == null) {
                    throw new IllegalStateException("Created project not found");
                }
                return project;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create project", e);
        }
    }

    public boolean softDelete(long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(ProjectQueries.SOFT_DELETE_PROJECT)) {
            statement.setLong(1, id);
            int affected = statement.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete project", e);
        }
    }

    public ProjectRecord updateById(long id, String code, String name, String description) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(ProjectQueries.UPDATE_BY_ID)) {
            statement.setString(1, code);
            statement.setString(2, name);
            statement.setString(3, description);
            statement.setLong(4, id);

            int affected = statement.executeUpdate();
            if (affected == 0) {
                return null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update project", e);
        }

        return findById(id);
    }

    public ProjectRecord findById(long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(ProjectQueries.FIND_BY_ID)) {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapProject(resultSet);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to query project by id", e);
        }
    }

    public ProjectRecord findByCode(String code) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(ProjectQueries.FIND_BY_CODE)) {
            statement.setString(1, code);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapProject(resultSet);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to query project by code", e);
        }
    }

    public ProjectRecord upsertByCode(String code, String name, String description) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(ProjectQueries.UPSERT_BY_CODE)) {
            statement.setString(1, code);
            statement.setString(2, name);
            statement.setString(3, description);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to upsert project", e);
        }

        ProjectRecord record = findByCode(code);
        if (record == null) {
            throw new IllegalStateException("Upserted project not found");
        }
        return record;
    }

    public List<ProjectUserRecord> listUsersByProject(long projectId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(ProjectQueries.LIST_PROJECT_USERS)) {
            statement.setLong(1, projectId);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<ProjectUserRecord> users = new ArrayList<>();

                while (resultSet.next()) {
                    users.add(new ProjectUserRecord(
                        resultSet.getLong("id"),
                        resultSet.getLong("project_id"),
                        resultSet.getString("email"),
                        resultSet.getString("password_hash"),
                        resultSet.getInt("is_active") == 1
                    ));
                }

                return users;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list project users", e);
        }
    }

    public void replaceProjectUsers(long projectId, List<ProjectUserRecord> users) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement deleteStatement = connection.prepareStatement(ProjectQueries.DELETE_PROJECT_USERS)) {
                    deleteStatement.setLong(1, projectId);
                    deleteStatement.executeUpdate();
                }

                try (PreparedStatement insertStatement = connection.prepareStatement(ProjectQueries.INSERT_PROJECT_USER)) {
                    for (ProjectUserRecord user : users) {
                        insertStatement.setLong(1, projectId);
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
            throw new IllegalStateException("Failed to replace project users", e);
        }
    }

    private ProjectRecord mapProject(ResultSet resultSet) throws SQLException {
        return new ProjectRecord(
            resultSet.getLong("id"),
            resultSet.getString("code"),
            resultSet.getString("name"),
            resultSet.getString("description"),
            resultSet.getInt("is_deleted") == 1,
            resultSet.getString("created_at"),
            resultSet.getString("updated_at")
        );
    }

    public record ProjectRecord(
        long id,
        String code,
        String name,
        String description,
        boolean deleted,
        String createdAt,
        String updatedAt
    ) {
    }

    public record ProjectUserRecord(
        long id,
        long projectId,
        String email,
        String passwordHash,
        boolean active
    ) {
    }
}
