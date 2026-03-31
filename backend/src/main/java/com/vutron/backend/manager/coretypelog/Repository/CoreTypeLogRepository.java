package com.vutron.backend.manager.coretypelog.Repository;

import com.vutron.backend.manager.coretypelog.Query.CoreTypeLogQueries;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class CoreTypeLogRepository {
    private final DataSource dataSource;

    public CoreTypeLogRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<CoreTypeLogRecord> listAll() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CoreTypeLogQueries.LIST_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            List<CoreTypeLogRecord> records = new ArrayList<>();

            while (resultSet.next()) {
                records.add(mapCoreTypeLog(resultSet));
            }

            return records;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list core type logs", e);
        }
    }

    public CoreTypeLogRecord create(String code, String name, String description) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 CoreTypeLogQueries.INSERT_CORE_TYPE_LOG,
                 Statement.RETURN_GENERATED_KEYS
             )) {
            statement.setString(1, code);
            statement.setString(2, name);
            statement.setString(3, description);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new IllegalStateException("Could not retrieve generated core type log id");
                }
                long id = keys.getLong(1);
                CoreTypeLogRecord created = findById(id);
                if (created == null) {
                    throw new IllegalStateException("Created core type log not found");
                }
                return created;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create core type log", e);
        }
    }

    public CoreTypeLogRecord updateById(long id, String code, String name, String description) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CoreTypeLogQueries.UPDATE_BY_ID)) {
            statement.setString(1, code);
            statement.setString(2, name);
            statement.setString(3, description);
            statement.setLong(4, id);

            int affected = statement.executeUpdate();
            if (affected == 0) {
                return null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update core type log", e);
        }

        return findById(id);
    }

    public CoreTypeLogRecord findById(long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CoreTypeLogQueries.FIND_BY_ID)) {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapCoreTypeLog(resultSet);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to query core type log by id", e);
        }
    }

    public CoreTypeLogRecord findByCode(String code) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CoreTypeLogQueries.FIND_BY_CODE)) {
            statement.setString(1, code);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapCoreTypeLog(resultSet);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to query core type log by code", e);
        }
    }

    public boolean deleteById(long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CoreTypeLogQueries.DELETE_BY_ID)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete core type log", e);
        }
    }

    private CoreTypeLogRecord mapCoreTypeLog(ResultSet resultSet) throws SQLException {
        return new CoreTypeLogRecord(
            resultSet.getLong("id"),
            resultSet.getString("code"),
            resultSet.getString("name"),
            resultSet.getString("description"),
            resultSet.getString("created_at"),
            resultSet.getString("updated_at")
        );
    }

    public record CoreTypeLogRecord(
        long id,
        String code,
        String name,
        String description,
        String createdAt,
        String updatedAt
    ) {
    }
}