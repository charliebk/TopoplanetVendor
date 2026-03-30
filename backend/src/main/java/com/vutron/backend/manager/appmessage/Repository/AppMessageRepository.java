package com.vutron.backend.manager.appmessage.Repository;

import com.vutron.backend.manager.appmessage.Query.AppMessageQueries;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class AppMessageRepository {
    private final DataSource dataSource;

    public AppMessageRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public AppMessageRecord findByKey(String key) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(AppMessageQueries.FIND_BY_KEY)) {
            statement.setString(1, key);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return new AppMessageRecord(
                    resultSet.getString("key"),
                    resultSet.getString("value")
                );
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to query app message", e);
        }
    }

    public record AppMessageRecord(String key, String value) {
    }
}
