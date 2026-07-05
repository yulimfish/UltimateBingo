package io.shantek.managers;

import io.shantek.UltimateBingo;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages bingo card maps for players
 * Creates, updates, and tracks map items that display the live bingo card
 */
public class BingoMapManager {
    
    private final UltimateBingo plugin;
    private final Map<UUID, MapView> playerMaps = new HashMap<>();
    private final Map<UUID, BingoCardMapRenderer> playerRenderers = new HashMap<>();
    
    public BingoMapManager(UltimateBingo plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Create and give a bingo card map to a player
     */
    public void giveBingoMap(Player player) {
        // Remove existing map if present
        removePlayerMap(player);
        
        // Create new map
        MapView mapView = createMapView(player);
        
        // Store the map and renderer
        playerMaps.put(player.getUniqueId(), mapView);
        
        // Create the map item
        ItemStack mapItem = createMapItem(mapView);
        
        // Give to player
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItem(player.getLocation(), mapItem);
        } else {
            // Try to place in slot 0 first
            if (player.getInventory().getItem(0) == null) {
                player.getInventory().setItem(0, mapItem);
            } else {
                player.getInventory().addItem(mapItem);
            }
        }
        
        // Force an immediate render by sending the map to the player
        // This ensures the map shows content right away
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.sendMap(mapView);
            }
        }, 5L); // Small delay to ensure everything is loaded
    }
    
    /**
     * Create a MapView with custom renderer for a player
     */
    private MapView createMapView(Player player) {
        MapView mapView = Bukkit.createMap(player.getWorld());
        
        // Remove default renderers
        for (MapRenderer renderer : mapView.getRenderers()) {
            mapView.removeRenderer(renderer);
        }
        
        // Add our custom renderer
        BingoCardMapRenderer renderer = new BingoCardMapRenderer(this, player);
        mapView.addRenderer(renderer);
        playerRenderers.put(player.getUniqueId(), renderer);
        
        return mapView;
    }
    
    /**
     * Create the physical map item
     */
    private ItemStack createMapItem(MapView mapView) {
        ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) mapItem.getItemMeta();
        
        if (meta != null) {
            meta.setMapView(mapView);
            meta.setDisplayName(org.bukkit.ChatColor.GOLD + "宾果卡片");
            
            List<String> lore = new java.util.ArrayList<>();
            lore.add(org.bukkit.ChatColor.GRAY + "右键打开卡片界面");
            lore.add(org.bukkit.ChatColor.GRAY + "手持以查看实时卡片");
            meta.setLore(lore);
            
            mapItem.setItemMeta(meta);
        }
        
        return mapItem;
    }
    
    /**
     * Update a player's map to reflect current card state
     */
    public void updatePlayerMap(Player player) {
        BingoCardMapRenderer renderer = playerRenderers.get(player.getUniqueId());
        if (renderer != null) {
            renderer.markDirty();
            
            // Force update by sending to player
            MapView mapView = playerMaps.get(player.getUniqueId());
            if (mapView != null) {
                player.sendMap(mapView);
            }
        }
    }
    
    /**
     * Update all player maps
     */
    public void updateAllMaps() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.bingoFunctions.isActivePlayer(player)) {
                updatePlayerMap(player);
            }
        }
    }
    
    /**
     * Remove a player's map
     */
    public void removePlayerMap(Player player) {
        UUID playerId = player.getUniqueId();
        playerMaps.remove(playerId);
        playerRenderers.remove(playerId);
    }
    
    /**
     * Clear all maps
     */
    public void clearAllMaps() {
        playerMaps.clear();
        playerRenderers.clear();
    }
    
    /**
     * Get a player's bingo card items.
     * For group/teams mode, extracts items from the shared inventory.
     */
    public List<ItemStack> getPlayerCard(Player player) {
        // Group mode: extract from shared groupInventory
        if (plugin.currentGameMode.equalsIgnoreCase("group")) {
            return extractCardItems(plugin.groupInventory);
        }
        // Teams mode: extract from player's team inventory
        if (plugin.currentGameMode.equalsIgnoreCase("teams")) {
            org.bukkit.inventory.Inventory teamInv = plugin.bingoFunctions.getTeamInventory(player);
            return extractCardItems(teamInv);
        }
        // Individual mode: use per-player card map
        Map<UUID, List<ItemStack>> cards = plugin.bingoManager.getPlayerBingoCards();
        if (cards == null) {
            return null;
        }
        return cards.get(player.getUniqueId());
    }

    /**
     * Extract bingo card items from a GUI inventory using the slot pattern.
     */
    private List<ItemStack> extractCardItems(org.bukkit.inventory.Inventory inv) {
        if (inv == null) return null;
        List<ItemStack> items = new java.util.ArrayList<>();
        int[] slots = plugin.bingoManager.getSlots();
        if (slots == null) return null;
        for (int slot : slots) {
            ItemStack item = inv.getItem(slot);
            if (item != null) {
                items.add(item);
            }
        }
        return items.isEmpty() ? null : items;
    }
    
    /**
     * Check if a task at a given card position is completed for a player.
     * A task is completed if the corresponding GUIslot shows the tickedItemMaterial.
     *
     * @param cardPosition index into the player's card list (0..N-1)
     */
    public boolean isTaskCompleted(Player player, int cardPosition) {
        // Get the player's inventory (GUI)
        org.bukkit.inventory.Inventory inv = null;
        
        if (plugin.currentGameMode.equalsIgnoreCase("group")) {
            inv = plugin.groupInventory;
        } else if (plugin.currentGameMode.equalsIgnoreCase("teams")) {
            inv = plugin.bingoFunctions.getTeamInventory(player);
        } else {
            inv = plugin.bingoManager.getBingoGUIs().get(player.getUniqueId());
        }
        
        if (inv == null) return false;
        
        int[] slots = plugin.bingoManager.getSlots();
        if (slots == null || cardPosition < 0 || cardPosition >= slots.length) return false;
        
        ItemStack guiItem = inv.getItem(slots[cardPosition]);
        return guiItem != null && guiItem.getType() == plugin.tickedItemMaterial;
    }
    
    /**
     * Check if a player has a bingo map in their inventory
     */
    public boolean hasBingoMap(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.FILLED_MAP) {
                MapMeta meta = (MapMeta) item.getItemMeta();
                if (meta != null && meta.hasDisplayName() && 
                    meta.getDisplayName().equals(org.bukkit.ChatColor.GOLD + "宾果卡片")) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Get the game mode text for display on the map
     */
    public String getGameModeText() {
        if (plugin.currentFullCard) {
            return "满卡";
        } else {
            return "单行";
        }
    }
    
    /**
     * Replace compass with map for a player
     */
    public void replaceCompassWithMap(Player player) {
        // Remove any compass bingo cards
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() == Material.COMPASS) {
                MapMeta meta = (MapMeta) item.getItemMeta();
                if (meta != null && meta.hasDisplayName() && 
                    meta.getDisplayName().contains("宾果卡片")) {
                    player.getInventory().setItem(i, null);
                    break;
                }
            }
        }
        
        // Give new map
        giveBingoMap(player);
    }

    /**
     * Forces all active bingo maps to re-render on next update.
     * Used for hot-reloading the map background image.
     */
    public void forceRefreshAllMaps() {
        for (BingoCardMapRenderer renderer : playerRenderers.values()) {
            if (renderer != null) {
                renderer.forceRedraw();
            }
        }
        // Optionally push an immediate update to online players
        for (Map.Entry<UUID, MapView> e : playerMaps.entrySet()) {
            Player p = Bukkit.getPlayer(e.getKey());
            if (p != null && p.isOnline()) {
                p.sendMap(e.getValue());
            }
        }
    }

    public UltimateBingo getPlugin() {
        return plugin;
    }

}
