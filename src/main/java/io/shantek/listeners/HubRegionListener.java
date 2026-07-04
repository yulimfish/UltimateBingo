package io.shantek.listeners;

import io.shantek.UltimateBingo;
import io.shantek.tools.WorldGuardHelper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * When hub mode is active and a game has ended, players receive a bingo card
 * to review in the hub world. This listener removes that card if they walk
 * out of the hub WorldGuard region.
 *
 * Only active when hub mode is enabled (hub.yml configured + WorldGuard present).
 * Uses a lightweight block-change check to avoid running WorldGuard queries on every tick.
 */
public class HubRegionListener implements Listener {

    private final UltimateBingo plugin;
    // Track players we know are inside the region (to detect exits)
    private final Set<UUID> playersInRegion = new HashSet<>();

    public HubRegionListener(UltimateBingo plugin) {
        this.plugin = plugin;
    }

    /**
     * Call this to mark players as being in the hub region
     * (called after teleporting them back from a game).
     */
    public void markPlayerInRegion(UUID playerId) {
        playersInRegion.add(playerId);
    }

    public void clearAll() {
        playersInRegion.clear();
    }

    public boolean isTracked(UUID playerId) {
        return playersInRegion.contains(playerId);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Quick exit: only care about hub mode
        if (!plugin.isHubModeActive()) return;

        // Quick exit: only trigger on block change (not head rotation)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();

        // Only check players we're tracking (those who got cards in the hub)
        if (!playersInRegion.contains(player.getUniqueId())) return;

        // Only check if they're in the hub world
        if (!player.getWorld().getName().equalsIgnoreCase(plugin.hubWorld)) {
            // They left the hub world entirely — remove card and stop tracking
            removeBingoCard(player);
            playersInRegion.remove(player.getUniqueId());
            return;
        }

        // Check if they're still in the hub region
        List<Player> inRegion = WorldGuardHelper.getPlayersInRegion(plugin.hubWorld, plugin.hubRegion);
        boolean stillInside = false;
        for (Player p : inRegion) {
            if (p.getUniqueId().equals(player.getUniqueId())) {
                stillInside = true;
                break;
            }
        }

        if (!stillInside) {
            removeBingoCard(player);
            playersInRegion.remove(player.getUniqueId());
        }
    }

    private void removeBingoCard(Player player) {
        // Remove any filled maps that are bingo cards
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() == Material.FILLED_MAP) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName()
                        && meta.getDisplayName().contains("宾果卡片")) {
                    player.getInventory().setItem(i, null);
                }
            }
        }
    }
}
