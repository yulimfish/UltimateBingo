package io.shantek.listeners;

import io.shantek.UltimateBingo;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

/**
 * Handles interactions with the bingo card map
 * Right-clicking opens the GUI, holding the map shows the live card
 */
public class BingoMapInteractListener implements Listener {
    
    private final UltimateBingo plugin;
    
    public BingoMapInteractListener(UltimateBingo plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onMapInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // Check if player is holding the bingo card map
        if (item == null || item.getType() != Material.FILLED_MAP) {
            return;
        }
        
        MapMeta meta = (MapMeta) item.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || 
            !meta.getDisplayName().equals(ChatColor.GOLD + "宾果卡片")) {
            return;
        }
        
        // Only handle right-click to open GUI
        if (event.getAction() == Action.RIGHT_CLICK_AIR || 
            event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            
            if (!plugin.bingoFunctions.canInteractWithCard(player)) {
                return;
            }
            
            if (plugin.playedSinceReboot) {
                if (plugin.currentGameMode.equalsIgnoreCase("group") || 
                    plugin.currentGameMode.equalsIgnoreCase("teams")) {
                    if (!plugin.bingoFunctions.isPlayerInGame(player.getUniqueId())) {
                        player.sendMessage(ChatColor.RED + "输入 /bingo 加入游戏。");
                    } else {
                        plugin.bingoCommand.openBingo(player);
                    }
                } else if (plugin.bingoCardActive || 
                          (!plugin.bingoManager.getBingoGUIs().isEmpty() && 
                           plugin.bingoManager.checkHasBingoCard(player))) {
                    plugin.bingoCommand.openBingo(player);
                } else {
                    player.sendMessage(ChatColor.RED + "宾果尚未开始！");
                }
                
                event.setCancelled(true);
            }
        }
    }
}
