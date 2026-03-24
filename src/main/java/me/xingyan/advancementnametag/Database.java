package me.xingyan.advancementnametag;

import org.bukkit.Bukkit;

import java.sql.*;
import java.util.Set;
import java.util.UUID;

public class Database {

    private final Connection connection;

    public Database(String path) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS Players (
                        UUID TEXT PRIMARY KEY,
                        Username TEXT NOT NULL,
                        Nametag TEXT,
                        Colored TEXT,
                        Icon TEXT
                    )
                    """);
            // Migrate: add Icon column when upgrading from an older schema
            try {
                statement.execute("ALTER TABLE Players ADD COLUMN Icon TEXT");
            } catch (SQLException e) {
                if (!e.getMessage().toLowerCase().contains("duplicate column name")) {
                    throw e;
                }
            }
        }
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public void addPlayer(String uuid) throws SQLException {
        // INSERT OR IGNORE avoids a separate SELECT round-trip
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT OR IGNORE INTO Players (UUID, Username) VALUES (?, ?)")) {
            statement.setString(1, uuid);
            statement.setString(2, Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName());
            statement.executeUpdate();
        }
    }

    public String getNametag(String uuid) throws SQLException {
        return queryColumn(uuid, "Nametag");
    }

    public String getColored(String uuid) throws SQLException {
        return queryColumn(uuid, "Colored");
    }

    public String getIcon(String uuid) throws SQLException {
        return queryColumn(uuid, "Icon");
    }

    public void setNametag(String uuid, String nametag, String colored, String icon) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE Players SET Nametag = ?, Colored = ?, Icon = ? WHERE UUID = ?")) {
            statement.setString(1, nametag);
            statement.setString(2, colored);
            statement.setString(3, icon);
            statement.setString(4, uuid);
            statement.executeUpdate();
        }
    }

    private static final Set<String> VALID_COLUMNS = Set.of("Nametag", "Colored", "Icon");

    private String queryColumn(String uuid, String column) throws SQLException {
        if (!VALID_COLUMNS.contains(column)) {
            throw new IllegalArgumentException("Invalid column name: " + column);
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT " + column + " FROM Players WHERE UUID = ?")) {
            statement.setString(1, uuid);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString(column);
                }
            }
        }
        return null;
    }
}
