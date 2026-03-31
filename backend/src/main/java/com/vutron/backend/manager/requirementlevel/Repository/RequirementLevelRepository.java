package com.vutron.backend.manager.requirementlevel.Repository;

import com.vutron.backend.manager.requirementlevel.Query.RequirementLevelQueries;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class RequirementLevelRepository {
    private final DataSource dataSource;

    public RequirementLevelRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<RequirementLevelRecord> listAll() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(RequirementLevelQueries.LIST_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            List<RequirementLevelRecord> records = new ArrayList<>();

            while (resultSet.next()) {
                records.add(mapRequirementLevel(resultSet));
            }

            return records;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list requirement levels", e);
        }
    }

    public List<RequirementLevelRecord> listByCoreProjectId(long coreProjectId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(RequirementLevelQueries.LIST_BY_CORE_PROJECT_ID)) {
            statement.setLong(1, coreProjectId);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<RequirementLevelRecord> records = new ArrayList<>();

                while (resultSet.next()) {
                    records.add(mapRequirementLevel(resultSet));
                }

                return records;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list requirement levels by core project", e);
        }
    }

    public RequirementLevelRecord create(long coreProjectId, String code, String name, String description, double factor) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 RequirementLevelQueries.INSERT_REQUIREMENT_LEVEL,
                 Statement.RETURN_GENERATED_KEYS
             )) {
            statement.setLong(1, coreProjectId);
            statement.setString(2, code);
            statement.setString(3, name);
            statement.setString(4, description);
            statement.setDouble(5, factor);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new IllegalStateException("Could not retrieve generated requirement level id");
                }
                long id = keys.getLong(1);
                RequirementLevelRecord created = findById(id);
                if (created == null) {
                    throw new IllegalStateException("Created requirement level not found");
                }
                return created;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create requirement level", e);
        }
    }

    public RequirementLevelRecord updateById(long id, long coreProjectId, String code, String name, String description, double factor) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(RequirementLevelQueries.UPDATE_BY_ID)) {
            statement.setLong(1, coreProjectId);
            statement.setString(2, code);
            statement.setString(3, name);
            statement.setString(4, description);
            statement.setDouble(5, factor);
            statement.setLong(6, id);

            int affected = statement.executeUpdate();
            if (affected == 0) {
                return null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update requirement level", e);
        }

        return findById(id);
    }

    public RequirementLevelRecord findById(long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(RequirementLevelQueries.FIND_BY_ID)) {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapRequirementLevel(resultSet);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to query requirement level by id", e);
        }
    }

    public boolean deleteById(long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(RequirementLevelQueries.DELETE_BY_ID)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete requirement level", e);
        }
    }

    private RequirementLevelRecord mapRequirementLevel(ResultSet resultSet) throws SQLException {
        return new RequirementLevelRecord(
            resultSet.getLong("id"),
            resultSet.getLong("core_project_id"),
            resultSet.getString("code"),
            resultSet.getString("name"),
            resultSet.getString("description"),
            resultSet.getDouble("factor"),
            resultSet.getString("created_at"),
            resultSet.getString("updated_at")
        );
    }

    public record RequirementLevelRecord(
        long id,
        long coreProjectId,
        String code,
        String name,
        String description,
        double factor,
        String createdAt,
        String updatedAt
    ) {
    }
}