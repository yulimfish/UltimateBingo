package io.shantek.listeners;

import io.shantek.UltimateBingo;
import io.shantek.managers.InGameConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BingoSignListener implements Listener {
    private final UltimateBingo plugin;
    private final InGameConfigManager inGameConfigManager;

    public BingoSignListener(UltimateBingo plugin, InGameConfigManager inGameConfigManager) {
        this.plugin = plugin;
        this.inGameConfigManager = inGameConfigManager;

    }



    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null) return;


        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {

            if (block.getState() instanceof Sign sign) {

                for (Map.Entry<String, Location> entry : plugin.bingoFunctions.signLocations.entrySet()) {
                    if (block.getLocation().equals(entry.getValue())) {

                        if (player.hasPermission("shantek.ultimatebingo.signs")) {

                            if (!plugin.bingoButtonActive) {
                                player.sendMessage(ChatColor.RED + "宾果游戏已经在进行中！");
                            } else {
                                plugin.bingoFunctions.updateSetting(entry.getKey(), player);
                            }
                            event.setCancelled(true);
                        } else {
                            player.sendMessage(ChatColor.RED + "你没有权限更改设置！");
                            event.setCancelled(true);
                        }

                    }
                }

                // Team sign click handling - any player can click these
                for (Map.Entry<String, Location> entry : plugin.bingoFunctions.teamSignLocations.entrySet()) {
                    if (block.getLocation().equals(entry.getValue())) {
                        event.setCancelled(true);
                        String team = entry.getKey();
                        plugin.bingoFunctions.setManualTeam(player.getUniqueId(), team);

                        ChatColor teamColor = switch (team.toLowerCase()) {
                            case "red" -> ChatColor.RED;
                            case "blue" -> ChatColor.BLUE;
                            case "yellow" -> ChatColor.YELLOW;
                            default -> ChatColor.WHITE;
                        };
                        String teamName = team.substring(0, 1).toUpperCase() + team.substring(1);
                        player.sendMessage(ChatColor.GREEN + "你已选择 " + teamColor + teamName + ChatColor.GREEN + " 队干得漂亮！");
                        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                        break;
                    }
                }
            }
        }

        if (player.hasPermission("shantek.ultimatebingo.signs")) {
            if (block.getType().name().endsWith("_BUTTON") && plugin.bingoFunctions.startButtonLocation != null) {
                Location clickedLocation = block.getLocation();

                // Ensure startButtonLocation has a valid world before comparing
                if (plugin.bingoFunctions.startButtonLocation.getWorld() == null || clickedLocation.getWorld() == null) {
                    return;
                }

                // Normalize locations to block coordinates (ignore decimal precision)
                if (Objects.equals(clickedLocation.getWorld(), plugin.bingoFunctions.startButtonLocation.getWorld()) &&
                        clickedLocation.getBlockX() == plugin.bingoFunctions.startButtonLocation.getBlockX() &&
                        clickedLocation.getBlockY() == plugin.bingoFunctions.startButtonLocation.getBlockY() &&
                        clickedLocation.getBlockZ() == plugin.bingoFunctions.startButtonLocation.getBlockZ()) {

                    if (!plugin.bingoButtonActive) {
                        // Game is already active
                        if (plugin.isHubModeActive()) {
                            // Hub mode: during active game, button press teleports clicker to bingo world
                            // and prompts them to join
                            event.setCancelled(true);
                            org.bukkit.World bingoWorld = Bukkit.getWorld(plugin.bingoWorld);
                            if (bingoWorld != null) {
                                Location spawn = bingoWorld.getSpawnLocation();
                                player.teleport(spawn);
                                player.sendMessage(ChatColor.GREEN + "宾果游戏正在进行中。输入 /bingo 加入！");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "宾果游戏已经在进行中！");
                        }
                    } else {
                        event.setCancelled(true);

                        if (plugin.isHubModeActive()) {
                            // === HUB MODE START ===
                            java.util.List<Player> hubPlayers = io.shantek.tools.WorldGuardHelper.getPlayersInRegion(
                                    plugin.hubWorld, plugin.hubRegion);

                            if (hubPlayers.isEmpty()) {
                                player.sendMessage(ChatColor.RED + "大厅区域内未找到玩家！");
                                return;
                            }

                            plugin.bingoButtonActive = false;

                            // Set game config
                            plugin.bingoGameGUIManager.setGameConfiguration();

                            // Use bingo world spawn as the spawn location
                            org.bukkit.World bingoWorld = Bukkit.getWorld(plugin.bingoWorld);
                            if (bingoWorld == null) {
                                player.sendMessage(ChatColor.RED + "未找到宾果世界！");
                                plugin.bingoButtonActive = true;
                                return;
                            }

                            Location bingoSpawn = bingoWorld.getSpawnLocation();
                            plugin.bingoSpawnLocation = bingoSpawn;

                            // Notify and teleport all hub players to bingo world
                            for (Player hubPlayer : hubPlayers) {
                                hubPlayer.sendMessage(ChatColor.GREEN + "正在传送至宾果世界……");
                                hubPlayer.teleport(bingoSpawn);
                            }

                            // Wait for hub-teleport-delay, then start the game
                            long delayTicks = plugin.hubTeleportDelay * 20L;
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                plugin.bingoCommand.startBingo(player);
                            }, delayTicks);

                        } else {
                            // === NORMAL MODE START ===
                            plugin.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.YELLOW + "游戏将在 5 秒后开始……");

                            plugin.bingoButtonActive = false;

                            // Set all the game config ready to play
                            plugin.bingoGameGUIManager.setGameConfiguration();

                            plugin.bingoSpawnLocation = player.getLocation();

                            // Start game with delay
                            Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.bingoCommand.startBingo(player), 100L);
                        }
                    }
                }
            }

        }
    }

    // Protect all bingo signs (settings signs, team signs, start button) from being broken by non-ops
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location blockLoc = block.getLocation();

        // Check setting signs
        for (Location signLoc : plugin.bingoFunctions.signLocations.values()) {
            if (blockLoc.equals(signLoc)) {
                if (!event.getPlayer().isOp()) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "你不能破坏宾果告示牌！");
                }
                return;
            }
        }

        // Check team signs
        for (Location signLoc : plugin.bingoFunctions.teamSignLocations.values()) {
            if (blockLoc.equals(signLoc)) {
                if (!event.getPlayer().isOp()) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "你不能破坏宾果告示牌！");
                }
                return;
            }
        }
    }

}