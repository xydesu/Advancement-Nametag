package me.xingyan.advancementnametag;

import me.xingyan.advancementnametag.commands.TagsCommand;
import me.xingyan.advancementnametag.gui.NametagGui;
import me.xingyan.advancementnametag.listeners.PlayerJoinListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class AdvancementNametag extends JavaPlugin {

    private static AdvancementNametag instance;

    private Database database;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            database = new Database(getDataFolder().getAbsolutePath() + "/players.db");
        } catch (SQLException e) {
            getLogger().severe("Failed to connect to database: " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        TagsCommand commandExecutor = new TagsCommand();
        getCommand("tags").setExecutor(commandExecutor);
        getCommand("tags").setTabCompleter(commandExecutor);

        getServer().getPluginManager().registerEvents(new NametagGui(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new Expansion().register();
        }
    }

    @Override
    public void onDisable() {
        if (database != null) {
            try {
                database.closeConnection();
            } catch (SQLException e) {
                getLogger().severe("Failed to close database connection: " + e.getMessage());
            }
        }
    }

    public static AdvancementNametag getInstance() {
        return instance;
    }

    public Database getDatabase() {
        return database;
    }
}
