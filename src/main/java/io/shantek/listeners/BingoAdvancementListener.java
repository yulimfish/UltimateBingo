package io.shantek.listeners;

import io.shantek.UltimateBingo;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * Handles advancement completion detection for bingo cards.
 * When a player completes an advancement that matches a KNOWLEDGE_BOOK
 * task on their bingo card, the slot is marked as complete.
 */
public class BingoAdvancementListener implements Listener {

    private final UltimateBingo plugin;

    public BingoAdvancementListener(UltimateBingo plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAdvancementDone(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();

        if (!plugin.bingoFunctions.isActivePlayer(player) || !plugin.bingoStarted) {
            return;
        }

        String advancementKey = event.getAdvancement().getKey().toString();

        // Determine which inventory to check
        Inventory inv;
        if (plugin.currentGameMode.equalsIgnoreCase("group")) {
            inv = plugin.groupInventory;
        } else if (plugin.currentGameMode.equalsIgnoreCase("teams")) {
            inv = plugin.bingoFunctions.getTeamInventory(player);
        } else {
            inv = plugin.bingoManager.getBingoGUIs().get(player.getUniqueId());
        }

        if (inv == null) return;

        int[] slots = plugin.bingoManager.getSlots();
        if (slots == null) return;

        NamespacedKey pdcKey = new NamespacedKey(plugin, "bingo_adv");

        // Search card slots for a KNOWLEDGE_BOOK with matching advancement key
        for (int slotIndex : slots) {
            ItemStack item = inv.getItem(slotIndex);
            if (item == null || item.getType() != Material.KNOWLEDGE_BOOK) continue;

            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            String storedKey = meta.getPersistentDataContainer().get(pdcKey, PersistentDataType.STRING);
            if (storedKey != null && storedKey.equals(advancementKey)) {
                // Found matching advancement task — mark it complete
                plugin.bingoManager.markSlotAsComplete(player, slotIndex);
                return;
            }
        }
    }
}
