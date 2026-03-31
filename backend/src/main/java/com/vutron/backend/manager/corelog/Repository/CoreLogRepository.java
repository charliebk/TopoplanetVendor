package com.vutron.backend.manager.corelog.Repository;

import com.vutron.backend.manager.corelog.Query.CoreLogQueries;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class CoreLogRepository {
    private final DataSource dataSource;

    public CoreLogRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<CoreLogRecord> listAll() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CoreLogQueries.LIST_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            List<CoreLogRecord> records = new ArrayList<>();

            while (resultSet.next()) {
                records.add(mapCoreLog(resultSet));
            }

            return records;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list core logs", e);
        }
    }

    public List<CoreLogRecord> listByCoreProjectId(long coreProjectId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CoreLogQueries.LIST_BY_CORE_PROJECT_ID)) {
            statement.setLong(1, coreProjectId);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<CoreLogRecord> records = new ArrayList<>();

                while (resultSet.next()) {
                    records.add(mapCoreLog(resultSet));
                }

                return records;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list core logs by core project", e);
        }
    }

    public CoreLogRecord findById(long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CoreLogQueries.FIND_BY_ID)) {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapCoreLog(resultSet);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to query core log by id", e);
        }
    }

    public CoreLogRecord create(
        long coreProjectId,
        long coreTypeLogId,
        String title,
        String message,
        String comment,
        String happenedAtUtc
    ) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 CoreLogQueries.INSERT_CORE_LOG,
                 Statement.RETURN_GENERATED_KEYS
             )) {
            statement.setLong(1, coreProjectId);
            statement.setLong(2, coreTypeLogId);
            statement.setString(3, title);
            statement.setString(4, message);
            statement.setString(5, comment);
            statement.setString(6, happenedAtUtc);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new IllegalStateException("Could not retrieve generated core log id");
                }

                long id = keys.getLong(1);
                CoreLogRecord created = findById(id);
                if (created == null) {
                    throw new IllegalStateException("Created core log not found");
                }
                return created;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create core log", e);
        }
    }

    public CoreLogRecord updateById(
        long id,
        long coreProjectId,
        long coreTypeLogId,
        String title,
        String message,
        String comment,
        String happenedAtUtc
    ) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CoreLogQueries.UPDATE_BY_ID)) {
            statement.setLong(1, coreProjectId);
            statement.setLong(2, coreTypeLogId);
            statement.setString(3, title);
            statement.setString(4, message);
            statement.setString(5, comment);
            statement.setString(6, happenedAtUtc);
            statement.setLong(7, id);

            int affected = statement.executeUpdate();
            if (affected == 0) {
                return null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update core log", e);
        }

        return findById(id);
    }

    public boolean deleteById(long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CoreLogQueries.DELETE_BY_ID)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete core log", e);
        }
    }

    public boolean existsCoreProject(long coreProjectId) {
        return exists(CoreLogQueries.EXISTS_CORE_PROJECT, coreProjectId);
    }

    public boolean existsCoreTypeLog(long coreTypeLogId) {
        return exists(CoreLogQueries.EXISTS_CORE_TYPE_LOG, coreTypeLogId);
    }

    private boolean exists(String query, long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to validate core log relation", e);
        }
    }

    private CoreLogRecord mapCoreLog(ResultSet resultSet) throws SQLException {
        return new CoreLogRecord(
            resultSet.getLong("id"),
            resultSet.getLong("core_project_id"),
            resultSet.getLong("core_type_log_id"),
            resultSet.getString("core_type_log_code"),
            resultSet.getString("core_type_log_name"),
            resultSet.getString("title"),
            resultSet.getString("message"),
            resultSet.getString("comment"),
            resultSet.getString("happened_at_utc"),
            resultSet.getString("created_at")
        );
    }

    public record CoreLogRecord(
        long id,
        long coreProjectId,
        long coreTypeLogId,
        String coreTypeLogCode,
        String coreTypeLogName,
        String title,
        String message,
        String comment,
        String happenedAtUtc,
        String createdAt
    ) {
    }
}