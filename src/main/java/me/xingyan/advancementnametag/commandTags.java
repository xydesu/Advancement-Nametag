package me.xingyan.advancementnametag;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class commandTags implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        FileConfiguration config = AdvancementNametag.plugin.getConfig();

        //check if sender is player
        if(!(commandSender instanceof Player)) {
            commandSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(config.getString("message.not-player")));
            return true;
        }
        //open gui
        guiNametag gui = new guiNametag();
        gui.openInventory((Player) commandSender);

        return true;
    }
}
