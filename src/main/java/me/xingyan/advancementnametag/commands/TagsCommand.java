package me.xingyan.advancementnametag.commands;

import me.xingyan.advancementnametag.AdvancementNametag;
import me.xingyan.advancementnametag.Database;
import me.xingyan.advancementnametag.gui.NametagGui;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TagsCommand implements CommandExecutor, TabCompleter {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        FileConfiguration config = AdvancementNametag.getInstance().getConfig();

        // /tags reload
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("advancementnametag.admin")) {
                sender.sendMessage(LEGACY.deserialize(Objects.requireNonNull(config.getString("message.no-permission"))));
                return true;
            }
            AdvancementNametag.getInstance().reloadConfig();
            sender.sendMessage(LEGACY.deserialize(Objects.requireNonNull(
                    AdvancementNametag.getInstance().getConfig().getString("message.reload"))));
            return true;
        }

        // /tags view <player>
        if (args.length == 2 && args[0].equalsIgnoreCase("view")) {
            if (!sender.hasPermission("advancementnametag.admin")) {
                sender.sendMessage(LEGACY.deserialize(Objects.requireNonNull(config.getString("message.no-permission"))));
                return true;
            }
            String targetName = args[1];
            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                sender.sendMessage(LEGACY.deserialize(Objects.requireNonNull(config.getString("message.player-not-found"))
                        .replace("%player%", targetName)));
                return true;
            }
            try {
                Database db = AdvancementNametag.getInstance().getDatabase();
                if (db == null) {
                    sender.sendMessage(LEGACY.deserialize(Objects.requireNonNull(config.getString("message.database-error"))));
                    return true;
                }
                String nametag = db.getNametag(target.getUniqueId().toString());
                if (nametag == null || nametag.isEmpty()) {
                    sender.sendMessage(LEGACY.deserialize(Objects.requireNonNull(config.getString("message.view-no-tag"))
                            .replace("%player%", target.getName())));
                } else {
                    sender.sendMessage(LEGACY.deserialize(Objects.requireNonNull(config.getString("message.view-tag"))
                            .replace("%player%", target.getName())
                            .replace("%tag%", nametag)));
                }
            } catch (SQLException e) {
                AdvancementNametag.getInstance().getLogger().severe(
                        "Database error looking up nametag for " + target.getName() + ": " + e.getMessage());
                sender.sendMessage(LEGACY.deserialize(Objects.requireNonNull(config.getString("message.database-error"))));
            }
            return true;
        }

        // /tags (open GUI)
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(LEGACY.deserialize(Objects.requireNonNull(config.getString("message.not-player"))));
                return true;
            }
            if (!player.hasPermission("advancementnametag.use")) {
                player.sendMessage(LEGACY.deserialize(Objects.requireNonNull(config.getString("message.no-permission"))));
                return true;
            }
            new NametagGui().openInventory(player);
            return true;
        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1 && sender.hasPermission("advancementnametag.admin")) {
            String partial = args[0].toLowerCase();
            if ("reload".startsWith(partial)) completions.add("reload");
            if ("view".startsWith(partial)) completions.add("view");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("view") && sender.hasPermission("advancementnametag.admin")) {
            String partial = args[1].toLowerCase();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(partial)) {
                    completions.add(p.getName());
                }
            }
        }

        return completions;
    }
}
