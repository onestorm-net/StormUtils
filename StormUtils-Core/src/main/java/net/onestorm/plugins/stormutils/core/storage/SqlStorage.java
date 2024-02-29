package net.onestorm.plugins.stormutils.core.storage;

import com.zaxxer.hikari.HikariDataSource;
import net.onestorm.plugins.stormutils.api.storage.Storage;
import net.onestorm.plugins.stormutils.api.storage.UserData;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class SqlStorage implements Storage {

    private static final int NUMBER_OF_THREADS = 10;
    private final ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    protected HikariDataSource hikari;
    protected Logger logger;


    protected void createTables() {
        String query = "CREATE TABLE IF NOT EXISTS storm_util_users (" +
                "`uuid` CHAR(36) NOT NULL, " +
                "`last_known_username` VARCHAR(16), " +
                "`first_join_time` TIMESTAMP, " +
                "`last_join_time` TIMESTAMP, " +
                "`last_logout_time` TIMESTAMP, " +
                "PRIMARY KEY (`uuid`));";

        try (Connection connection = hikari.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            logger.log(Level.WARNING, "SQLException in createTables", e);
        }
    }

    @Override
    public CompletableFuture<Optional<UserData>> getUserData(UUID uuid) {
        CompletableFuture<Optional<UserData>> future = new CompletableFuture<>();

        String query = "SELECT uuid, last_known_username, first_join_time, last_join_time, last_logout_time FROM storm_util_users WHERE uuid = ?;";

        executor.submit(() -> {
            try (Connection connection = hikari.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setString(1, uuid.toString());

                ResultSet resultSet = preparedStatement.executeQuery();

                if (!resultSet.next()) {
                    future.complete(Optional.empty());
                    return; // submit
                }

                UserData data = createUserData(resultSet);

                future.complete(Optional.of(data));

            } catch (SQLException e) {
                future.completeExceptionally(e);
                logger.log(Level.WARNING, "SQLException in getUserData(UUID)", e);
            }
        });

        return future;
    }

    @Override
    public CompletableFuture<Optional<UserData>> getUserData(String username) {
        if (username == null) {
            return CompletableFuture.failedFuture(new NullPointerException("Username cannot be null"));
        }

        CompletableFuture<Optional<UserData>> future = new CompletableFuture<>();

        String query = "SELECT uuid, last_known_username, first_join_time, last_join_time, last_logout_time FROM storm_util_users WHERE LOWER(last_known_username) = ?;";

        executor.submit(() -> {
            try (Connection connection = hikari.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setString(1, username.toLowerCase(Locale.ENGLISH));

                ResultSet resultSet = preparedStatement.executeQuery();

                if (!resultSet.next()) {
                    future.complete(Optional.empty());
                    System.out.println("done get empty");
                    return; // submit
                }

                UserData data = createUserData(resultSet);

                future.complete(Optional.of(data));
                System.out.println("done get present");

            } catch (IllegalArgumentException e) {
                future.completeExceptionally(e);
                logger.log(Level.WARNING, "IllegalArgumentException in getUserData(String): Invalid UUID syntax", e);
            } catch (SQLException e) {
                future.completeExceptionally(e);
                logger.log(Level.WARNING, "SQLException in getUserData(String)", e);
            }
        });

        return future;
    }

    private UserData createUserData(ResultSet resultSet) throws SQLException {
        UUID uuid = UUID.fromString(resultSet.getString("uuid"));
        String lastKnownUsername = resultSet.getString("last_known_username");
        Timestamp firstJoinTimestamp = resultSet.getTimestamp("first_join_time");
        Timestamp lastJoinTimestamp = resultSet.getTimestamp("last_join_time");
        Timestamp lastLogoutTimestamp = resultSet.getTimestamp("last_logout_time");

        long firstJoinTime = 0L;
        long lastJoinTime = 0L;
        long lastLogoutTime = 0L;

        if (firstJoinTimestamp != null) {
            firstJoinTime = firstJoinTimestamp.getTime();
        }
        if (lastJoinTimestamp != null) {
            lastJoinTime = lastJoinTimestamp.getTime();
        }
        if (lastLogoutTimestamp != null) {
            lastLogoutTime = lastLogoutTimestamp.getTime();
        }

        return new UserDataImpl(uuid, lastKnownUsername, firstJoinTime, lastJoinTime, lastLogoutTime);
    }

    @Override
    public CompletableFuture<Void> setUserData(UUID uuid, UserData data) {

        CompletableFuture<Void> future = new CompletableFuture<>();

        String query = "INSERT INTO storm_util_users " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE last_known_username = ?, first_join_time = ?, last_join_time = ?, last_logout_time = ?;";

        executor.submit(() -> {
            System.out.println("submit");
            try (Connection connection = hikari.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                System.out.println("try");

                Timestamp firstJoinTime = null;
                Timestamp lastJoinTime = null;
                Timestamp lastLogoutTime = null;

                if (data.getFirstJoinTime() >= 1000L) {
                    firstJoinTime = new Timestamp(data.getFirstJoinTime());
                }
                if (data.getLastJoinTime() >= 1000L) {
                    lastJoinTime = new Timestamp(data.getLastJoinTime());
                }
                if (data.getLastLogoutTime() >= 1000L) {
                    lastLogoutTime = new Timestamp(data.getLastLogoutTime());
                }

                preparedStatement.setString(1, uuid.toString());

                preparedStatement.setString(2, data.getLastKnownUsername());
                preparedStatement.setTimestamp(3, firstJoinTime);
                preparedStatement.setTimestamp(4, lastJoinTime);
                preparedStatement.setTimestamp(5, lastLogoutTime);

                preparedStatement.setString(6, data.getLastKnownUsername());
                preparedStatement.setTimestamp(7, firstJoinTime);
                preparedStatement.setTimestamp(8, lastJoinTime);
                preparedStatement.setTimestamp(9, lastLogoutTime);

                preparedStatement.setQueryTimeout(2);

                int modification = preparedStatement.executeUpdate();
                System.out.println("modifications: " + modification);

                future.complete(null);
                System.out.println("done set");

            } catch (SQLException e) {
                future.completeExceptionally(e);
                logger.log(Level.WARNING, "SQLException in setUserData(UUID, UserData)", e);
            } catch (Exception e) {
                future.completeExceptionally(e);
                logger.log(Level.WARNING, "Uncaught Exception in setUserData(UUID, UserData)", e);
            }
        });
        return future;
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}
