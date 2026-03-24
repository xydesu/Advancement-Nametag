package me.xingyan.advancementnametag.listeners;

import me.xingyan.advancementnametag.AdvancementNametag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        try {
            AdvancementNametag.getInstance().getDatabase().addPlayer(player.getUniqueId().toString());
        } catch (SQLException e) {
            AdvancementNametag.getInstance().getLogger().severe(
                    "Failed to register player " + player.getName() + " in database: " + e.getMessage());
        }
    }
}
