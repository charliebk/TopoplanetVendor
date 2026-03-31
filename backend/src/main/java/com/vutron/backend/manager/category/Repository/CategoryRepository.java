package com.vutron.backend.manager.category.Repository;

import com.vutron.backend.manager.category.Query.CategoryQueries;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class CategoryRepository {
    private final DataSource dataSource;

    public CategoryRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<CategoryRecord> listAll() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CategoryQueries.LIST_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            List<CategoryRecord> records = new ArrayList<>();

            while (resultSet.next()) {
                records.add(mapCategory(resultSet));
            }

            return records;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list categories", e);
        }
    }

    public List<CategoryRecord> listByCoreProjectId(long coreProjectId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CategoryQueries.LIST_BY_CORE_PROJECT_ID)) {
            statement.setLong(1, coreProjectId);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<CategoryRecord> records = new ArrayList<>();

                while (resultSet.next()) {
                    records.add(mapCategory(resultSet));
                }

                return records;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list categories by core project", e);
        }
    }

    public CategoryRecord create(long coreProjectId, String code, String name, String description, double factor) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 CategoryQueries.INSERT_CATEGORY,
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
                    throw new IllegalStateException("Could not retrieve generated category id");
                }
                long id = keys.getLong(1);
                CategoryRecord created = findById(id);
                if (created == null) {
                    throw new IllegalStateException("Created category not found");
                }
                return created;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create category", e);
        }
    }

    public CategoryRecord updateById(long id, long coreProjectId, String code, String name, String description, double factor) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CategoryQueries.UPDATE_BY_ID)) {
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
            throw new IllegalStateException("Failed to update category", e);
        }

        return findById(id);
    }

    public CategoryRecord findById(long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CategoryQueries.FIND_BY_ID)) {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapCategory(resultSet);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to query category by id", e);
        }
    }

    public boolean deleteById(long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CategoryQueries.DELETE_BY_ID)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete category", e);
        }
    }

    private CategoryRecord mapCategory(ResultSet resultSet) throws SQLException {
        return new CategoryRecord(
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

    public record CategoryRecord(
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