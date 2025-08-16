package org.mad4me.roleSync;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SQLite {
    private Connection connection;
    private final JavaPlugin plugin;

    public SQLite(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public Connection getConnection() {
        return connection;
    }

    public void createDatabase() {
        try {
            File dbFile = new File(plugin.getDataFolder(), "database.db");
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdir();

            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getLinkedPlayers() {
        String query = "SELECT uuid FROM players";
        List<String> linkedPlayers = new ArrayList<>();

        if (getConnection() == null) {
            return Collections.emptyList();
        }

        try (PreparedStatement pstmt = this.getConnection().prepareStatement(query)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) linkedPlayers.add(rs.getString("uuid"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

        return linkedPlayers;
    }

    public boolean isPlayerLinked(String uuid) {
        String query = "SELECT * FROM players WHERE uuid = ? ";

        try (PreparedStatement pstmt = this.getConnection().prepareStatement(query)) {
            pstmt.setString(1, uuid);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public long getDiscordId(String uuid) {
        String query = "SELECT discord_id FROM players WHERE uuid = ?";

        try (PreparedStatement pstmt = this.getConnection().prepareStatement(query)) {
            pstmt.setString(1, uuid);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.getLong("discord_id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    // привте нернн

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createTable() {
        String query = "CREATE TABLE IF NOT EXISTS players (id INTEGER PRIMARY KEY, discord_id TEXT NOT NULL, uuid TEXT NOT NULL);";
        try (PreparedStatement pstmt = this.getConnection().prepareStatement(query)) {
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertPlayer(String discord_id, String uuid) {
        String query = "INSERT INTO players (discord_id, uuid) VALUES (?, ?);";
        try (PreparedStatement pstmt = this.getConnection().prepareStatement(query)) {
            pstmt.setString(1, discord_id);
            pstmt.setString(2, uuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
