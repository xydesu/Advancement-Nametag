package me.xingyan.advancementnametag;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class Expansion extends PlaceholderExpansion {

    private final Database database = AdvancementNametag.getInstance().getDatabase();

    @Override
    public @NotNull String getIdentifier() {
        return "advancementnametag";
    }

    @Override
    public @NotNull String getAuthor() {
        return "xydesu";
    }

    @Override
    public @NotNull String getVersion() {
        return AdvancementNametag.getInstance().getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        String uuid = player.getUniqueId().toString();
        try {
            return switch (params.toLowerCase()) {
                case "tag" -> {
                    String tag = database.getNametag(uuid);
                    yield tag != null ? tag : "";
                }
                case "colored" -> {
                    String colored = database.getColored(uuid);
                    yield colored != null ? colored : "";
                }
                case "icon" -> {
                    String icon = database.getIcon(uuid);
                    yield icon != null ? icon : "";
                }
                case "hastag" -> database.getNametag(uuid) != null ? "true" : "false";
                default -> null;
            };
        } catch (SQLException e) {
            AdvancementNametag.getInstance().getLogger().warning(
                    "Database error handling placeholder '" + params + "' for " + player.getName() + ": " + e.getMessage());
            return null;
        }
    }
}
