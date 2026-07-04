package io.shantek.listeners;

import io.shantek.UltimateBingo;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class BingoGUIListener implements Listener {

    public UltimateBingo ultimateBingo;
    public BingoGUIListener(UltimateBingo ultimateBingo) {
        this.ultimateBingo = ultimateBingo;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        String title = ChatColor.translateAlternateColorCodes('&', e.getView().getTitle());

        // Only match our specific bingo card inventories (contains bold "宾果" which is our format)
        // This avoids matching other plugins that might have "宾果" in their inventory names
        if (!title.contains(ChatColor.BOLD + "宾果")) {
            return;
        }

            // Get the player who clicked in the inventory
            Player player = (Player) e.getWhoClicked();
            int slot = e.getRawSlot();

            // Spyglass item was clicked - Check if the option is enabled and open the player cards menu
            if (slot == 17) {
                if (ultimateBingo.currentRevealCards) {
                    e.setCancelled(true);
                    player.closeInventory();
                    player.openInventory(ultimateBingo.bingoPlayerGUIManager.setupPlayersBingoCardsInventory());
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                }
            }
            // Cancel all other events
            e.setCancelled(true);
    }
}

