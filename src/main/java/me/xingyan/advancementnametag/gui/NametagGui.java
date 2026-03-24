package me.xingyan.advancementnametag.gui;

import io.papermc.paper.advancement.AdvancementDisplay;
import me.xingyan.advancementnametag.AdvancementNametag;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NametagGui implements Listener {

    private static final int MAX_PAGES = 10;
    private static final int SLOT_RESET = 0;
    private static final int SLOT_PREV = 45;
    private static final int SLOT_NEXT = 53;
    private static final int SLOT_LAST_ITEM = 44;

    private static final Component TITLE = Component.text("Tags").color(NamedTextColor.GOLD);
    private static final Component NEXT_PAGE_NAME = Component.text("Next Page").color(NamedTextColor.GREEN);
    private static final Component PREV_PAGE_NAME = Component.text("Previous Page").color(NamedTextColor.GREEN);

    /** Stores per-player inventory pages so concurrent GUI sessions don't interfere. */
    private static final Map<UUID, List<Inventory>> playerPages = new ConcurrentHashMap<>();

    public void openInventory(Player player) {
        List<Inventory> pages = buildPages(player);
        playerPages.put(player.getUniqueId(), pages);
        player.openInventory(pages.get(0));
    }

    private List<Inventory> buildPages(Player player) {
        Inventory[] pageArray = new Inventory[MAX_PAGES];
        for (int i = 0; i < MAX_PAGES; i++) {
            pageArray[i] = Bukkit.createInventory(null, 54, TITLE);
        }

        // Reset button on page 1 occupies slot 0
        pageArray[0].setItem(SLOT_RESET, createItem(Material.BARRIER,
                Component.text("Reset").color(NamedTextColor.RED)));

        // Populate advancement items across pages
        int currentPage = 0;
        Iterator<Advancement> advancements = Bukkit.getServer().advancementIterator();
        while (advancements.hasNext() && currentPage < MAX_PAGES) {
            Advancement advancement = advancements.next();
            if (advancement.getKey().toString().contains("recipes")) continue;
            if (advancement.getDisplay() == null) continue;
            if (!player.getAdvancementProgress(advancement).isDone()) continue;

            // Move to next page when current page's last item slot is occupied
            if (pageArray[currentPage].getItem(SLOT_LAST_ITEM) != null) {
                currentPage++;
                if (currentPage >= MAX_PAGES) break;
            }
            pageArray[currentPage].addItem(buildAdvancementItem(advancement));
        }

        // Collect only pages that actually contain content
        List<Inventory> pages = new ArrayList<>();
        pages.add(pageArray[0]); // Always include the first page (has reset button)
        for (int i = 1; i < MAX_PAGES; i++) {
            if (pageArray[i].isEmpty()) break;
            pages.add(pageArray[i]);
        }

        // Add navigation arrows
        ItemStack nextItem = createItem(Material.ARROW, NEXT_PAGE_NAME);
        ItemStack prevItem = createItem(Material.ARROW, PREV_PAGE_NAME);
        for (int i = 0; i < pages.size(); i++) {
            if (i + 1 < pages.size()) pages.get(i).setItem(SLOT_NEXT, nextItem);
            if (i > 0) pages.get(i).setItem(SLOT_PREV, prevItem);
        }

        return pages;
    }

    private ItemStack buildAdvancementItem(Advancement advancement) {
        ItemStack item = new ItemStack(advancement.getDisplay().icon().getType());
        ItemMeta meta = item.getItemMeta();
        addItemFlags(meta);

        AdvancementDisplay.Frame frame = advancement.getDisplay().frame();
        if (frame == AdvancementDisplay.Frame.CHALLENGE) {
            meta.displayName(advancement.getDisplay().title()
                    .color(NamedTextColor.DARK_PURPLE).decoration(TextDecoration.ITALIC, false));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, false);
        } else if (frame == AdvancementDisplay.Frame.GOAL) {
            meta.displayName(advancement.getDisplay().title()
                    .color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, false);
        } else {
            meta.displayName(advancement.getDisplay().title()
                    .color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(List.of(advancement.getDisplay().description().decoration(TextDecoration.ITALIC, false)));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createItem(Material material, Component name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        addItemFlags(meta);
        item.setItemMeta(meta);
        return item;
    }

    private void addItemFlags(ItemMeta meta) {
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE, ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_DESTROYS,
                ItemFlag.HIDE_ARMOR_TRIM, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        List<Inventory> pages = playerPages.get(player.getUniqueId());
        if (pages == null || !pages.contains(event.getInventory())) return;

        event.setCancelled(true);

        Inventory clicked = event.getClickedInventory();
        if (clicked == null || clicked.equals(player.getInventory())) return;

        int currentPageIndex = pages.indexOf(clicked);
        if (currentPageIndex == -1) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        if (item.getType() == Material.BARRIER) {
            handleReset(player);
        } else if (item.getType() == Material.ARROW) {
            handleNavigation(player, item, pages, currentPageIndex);
        } else {
            handleTagSelect(player, item);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        List<Inventory> pages = playerPages.get(player.getUniqueId());
        if (pages == null || !pages.contains(event.getInventory())) return;

        // Delay cleanup by one tick to allow page navigation (close + immediate reopen)
        Bukkit.getScheduler().runTaskLater(AdvancementNametag.getInstance(), () -> {
            List<Inventory> current = playerPages.get(player.getUniqueId());
            if (current != null && !current.contains(player.getOpenInventory().getTopInventory())) {
                playerPages.remove(player.getUniqueId());
            }
        }, 1L);
    }

    private void handleReset(Player player) {
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                Objects.requireNonNull(AdvancementNametag.getInstance().getConfig().getString("message.reset"))));
        player.closeInventory();
        player.playSound(Sound.sound(org.bukkit.Sound.UI_BUTTON_CLICK, Sound.Source.PLAYER, 1f, 1f));
        try {
            AdvancementNametag.getInstance().getDatabase().setNametag(
                    player.getUniqueId().toString(), null, null, null);
        } catch (SQLException e) {
            AdvancementNametag.getInstance().getLogger().severe(
                    "Failed to reset nametag for " + player.getName() + ": " + e.getMessage());
        }
    }

    private void handleNavigation(Player player, ItemStack item, List<Inventory> pages, int currentPageIndex) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        Component displayName = meta.displayName();

        if (NEXT_PAGE_NAME.equals(displayName) && currentPageIndex + 1 < pages.size()) {
            player.closeInventory();
            player.openInventory(pages.get(currentPageIndex + 1));
            player.playSound(Sound.sound(org.bukkit.Sound.UI_BUTTON_CLICK, Sound.Source.PLAYER, 1f, 1f));
        } else if (PREV_PAGE_NAME.equals(displayName) && currentPageIndex > 0) {
            player.closeInventory();
            player.openInventory(pages.get(currentPageIndex - 1));
            player.playSound(Sound.sound(org.bukkit.Sound.UI_BUTTON_CLICK, Sound.Source.PLAYER, 1f, 1f));
        }
    }

    private void handleTagSelect(Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String plain = PlainTextComponentSerializer.plainText().serialize(meta.displayName());
        String colored = LegacyComponentSerializer.legacySection().serialize(meta.displayName());
        String icon = item.getType().name();

        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                Objects.requireNonNull(AdvancementNametag.getInstance().getConfig().getString("message.set"))
                        .replace("%tag%", plain)));
        player.closeInventory();
        player.playSound(Sound.sound(org.bukkit.Sound.UI_BUTTON_CLICK, Sound.Source.PLAYER, 1f, 1f));
        try {
            AdvancementNametag.getInstance().getDatabase().setNametag(
                    player.getUniqueId().toString(), plain, colored, icon);
        } catch (SQLException e) {
            AdvancementNametag.getInstance().getLogger().severe(
                    "Failed to save nametag for " + player.getName() + ": " + e.getMessage());
        }
    }
}
