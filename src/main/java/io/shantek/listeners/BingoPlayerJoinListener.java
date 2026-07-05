package io.shantek.listeners;

import io.shantek.UltimateBingo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BingoPlayerJoinListener implements Listener {

    UltimateBingo ultimateBingo;

    public BingoPlayerJoinListener(UltimateBingo ultimateBingo) {
        this.ultimateBingo = ultimateBingo;
    }

    @EventHandler
    public void onDimensionChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String toWorld = player.getWorld().getName();

        // Hide scoreboard if player left the active bingo world
        if (!ultimateBingo.bingoFunctions.isActivePlayer(player)) {
            ultimateBingo.bingoScoreboardManager.hideBoard(player);
        } else if (!ultimateBingo.bingoStarted) {
            // In bingo world, game not running → show record board
            ultimateBingo.bingoScoreboardManager.showRecordBoard(player);
        }

        // Multi-world: handle entering bingo world
        if (ultimateBingo.multiWorldServer) {
            if (toWorld.equalsIgnoreCase(ultimateBingo.bingoWorld)) {
                if (ultimateBingo.bingoStarted && ultimateBingo.bingoFunctions.isPlayerInGame(player.getUniqueId())) {
                    // Returning player — restore game state
                    restorePlayerState(player);
                } else if (!ultimateBingo.bingoStarted || !ultimateBingo.bingoFunctions.isPlayerInGame(player.getUniqueId())) {
                    // New player or game not running — reset
                    ultimateBingo.bingoFunctions.resetIndividualPlayer(player, true);
                    player.sendMessage("你不在进行中的宾果游戏中。你的背包已被重置。");
                }
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        // Multi-world: prompt players in non-bingo worlds about active game
        if (ultimateBingo.multiWorldServer && !player.getWorld().getName().equalsIgnoreCase(ultimateBingo.bingoWorld.toLowerCase()) && ultimateBingo.bingoStarted) {
            if (!ultimateBingo.bingoFunctions.isPlayerInGame(player.getUniqueId())) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(ultimateBingo, () -> {
                    player.sendMessage(ChatColor.GREEN + "宾果游戏正在进行中。前往宾果世界并输入 /bingo 即可加入");
                }, 200);
            }
        }

        if (ultimateBingo.bingoFunctions.isActivePlayer(player)) {
            if (!ultimateBingo.playedSinceReboot) {
                // Never played since reboot — full reset
                player.setWalkSpeed(0.2f);
                player.removePotionEffect(PotionEffectType.SLOW);
                player.removePotionEffect(PotionEffectType.JUMP);
                ultimateBingo.bingoFunctions.resetIndividualPlayer(player, true);
                return;

            } else if (!ultimateBingo.bingoStarted) {
                // Game not running
                player.setWalkSpeed(0.2f);
                player.removePotionEffect(PotionEffectType.SLOW);
                player.removePotionEffect(PotionEffectType.JUMP);
                ultimateBingo.bingoFunctions.resetIndividualPlayer(player, true);

                if (ultimateBingo.bingoManager.checkHasBingoCard(player)) {
                    ultimateBingo.bingoFunctions.giveBingoCard(player);
                }

            } else if (ultimateBingo.bingoStarted && ultimateBingo.bingoFunctions.isPlayerInGame(player.getUniqueId())) {
                // Game is active and player is in it — restore state (reconnect / rejoin)
                player.setWalkSpeed(0.2f);
                player.removePotionEffect(PotionEffectType.SLOW);
                player.removePotionEffect(PotionEffectType.JUMP);
                restorePlayerState(player);

            } else {
                // Game active but player not in it — prompt to join
                player.setWalkSpeed(0.2f);
                player.removePotionEffect(PotionEffectType.SLOW);
                player.removePotionEffect(PotionEffectType.JUMP);

                Bukkit.getScheduler().scheduleSyncDelayedTask(ultimateBingo, () -> {
                    player.sendMessage(ChatColor.GREEN + "宾果游戏正在进行中。输入 /bingo 加入！");
                }, 200);

                if (ultimateBingo.bingoManager.checkHasBingoCard(player)
                        && (ultimateBingo.currentGameMode.equals("speedrun") || ultimateBingo.currentGameMode.equals("group"))) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false, true));
                }
            }

            // Game not running → show historical record board
            if (!ultimateBingo.bingoStarted) {
                ultimateBingo.bingoScoreboardManager.showRecordBoard(player);
            }
        }
    }

    /**
     * Restore a returning player's game state: card, scoreboard, night vision.
     */
    private void restorePlayerState(Player player) {
        // Re-give the bingo card (map items are per-world)
        ultimateBingo.bingoFunctions.giveBingoCard(player);

        // Show their scoreboard
        ultimateBingo.bingoScoreboardManager.showBoard(player);

        // Re-apply night vision for applicable modes
        if (ultimateBingo.currentGameMode.equals("speedrun")
                || ultimateBingo.currentGameMode.equals("group")
                || ultimateBingo.currentGameMode.equalsIgnoreCase("teams")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false, true));
        }

        player.sendMessage(ChatColor.GREEN + "已恢复你的宾果游戏状态，继续加油！");
    }
}
