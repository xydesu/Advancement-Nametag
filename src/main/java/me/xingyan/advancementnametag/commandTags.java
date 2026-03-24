package me.xingyan.advancementnametag;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

public class commandTags implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        FileConfiguration config = AdvancementNametag.plugin.getConfig();

        // /tags reload
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!commandSender.hasPermission("advancementnametag.admin")) {
                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        Objects.requireNonNull(config.getString("message.no-permission"))));
                return true;
            }
            AdvancementNametag.plugin.reloadConfig();
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    Objects.requireNonNull(AdvancementNametag.plugin.getConfig().getString("message.reload"))));
            return true;
        }

        // /tags view <player>
        if (args.length == 2 && args[0].equalsIgnoreCase("view")) {
            if (!commandSender.hasPermission("advancementnametag.admin")) {
                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        Objects.requireNonNull(config.getString("message.no-permission"))));
                return true;
            }
            String targetName = args[1];
            Player target = Bukkit.getPlayerExact(targetName);
            if (target == null) {
                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        Objects.requireNonNull(config.getString("message.player-not-found"))
                                .replace("%player%", targetName)));
                return true;
            }
            try {
                String nametag = AdvancementNametag.plugin.getDatabase().getNametag(target.getUniqueId().toString());
                if (nametag == null || nametag.isEmpty()) {
                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            Objects.requireNonNull(config.getString("message.view-no-tag"))
                                    .replace("%player%", target.getName())));
                } else {
                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            Objects.requireNonNull(config.getString("message.view-tag"))
                                    .replace("%player%", target.getName())
                                    .replace("%tag%", nametag)));
                }
            } catch (SQLException e) {
                AdvancementNametag.plugin.getLogger().severe("Database error looking up nametag for " + target.getName() + ": " + e.getMessage());
                commandSender.sendMessage(ChatColor.RED + "Failed to retrieve nametag for " + target.getName() + ". Check database connection and server logs.");
            }
            return true;
        }

        // /tags (open GUI)
        if (args.length == 0) {
            if (!(commandSender instanceof Player player)) {
                commandSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                        Objects.requireNonNull(config.getString("message.not-player"))));
                return true;
            }
            if (!player.hasPermission("advancementnametag.use")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        Objects.requireNonNull(config.getString("message.no-permission"))));
                return true;
            }
            guiNametag gui = new guiNametag();
            gui.openInventory(player);
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
