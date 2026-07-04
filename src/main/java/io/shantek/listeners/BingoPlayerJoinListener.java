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

        // Check if multiworld server setting is enabled
        if (ultimateBingo.multiWorldServer) {
            // Check if the player teleported to the bingo world
            if (toWorld.equalsIgnoreCase(ultimateBingo.bingoWorld)) {
                // Check if bingo isn't active
                if (!ultimateBingo.bingoStarted || !ultimateBingo.bingoFunctions.isPlayerInGame(player.getUniqueId())) {
                    // Reset the player and their inventory
                    ultimateBingo.bingoFunctions.resetIndividualPlayer(player, true);
                    player.sendMessage("你不在进行中的宾果游戏中。你的背包已被重置。");
                }
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        // Get the player who just joined
        Player player = e.getPlayer();


        if (ultimateBingo.multiWorldServer && !player.getWorld().getName().equalsIgnoreCase(ultimateBingo.bingoWorld.toLowerCase()) && ultimateBingo.bingoStarted) {

            if (!ultimateBingo.bingoFunctions.isPlayerInGame(player.getUniqueId())) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(ultimateBingo, () -> {
                    player.sendMessage(ChatColor.GREEN + "宾果游戏正在进行中。前往宾果世界并输入 /bingo 即可加入");
                }, 200);
            }
        }

        if (ultimateBingo.bingoFunctions.isActivePlayer(player)) {
            if (!ultimateBingo.playedSinceReboot) {
                // A game hasn't been played since the reboot - Reset the player
                player.setWalkSpeed(0.2f);
                player.removePotionEffect(PotionEffectType.SLOW);
                player.removePotionEffect(PotionEffectType.JUMP);
                ultimateBingo.bingoFunctions.resetIndividualPlayer(player, true);
                return;

            } else if (!ultimateBingo.bingoStarted) {

                // A game has been played since reboot - see what we need to do with the player
                player.setWalkSpeed(0.2f);
                player.removePotionEffect(PotionEffectType.SLOW);
                player.removePotionEffect(PotionEffectType.JUMP);

                // If they joined and a game isn't active, reset their inventory in case they
                // carried anything over from a prior game
                ultimateBingo.bingoFunctions.resetIndividualPlayer(player, true);

                // Give them a replacement card so they can view results from a prior game
                if (ultimateBingo.bingoManager.checkHasBingoCard(player)) {
                    ultimateBingo.bingoFunctions.giveBingoCard(player);
                }

            } else {

                // Check if bingo is active and if they have a card. If they don't,
                // prompt them on how to join the game. Delay the message by 5 seconds
                player.setWalkSpeed(0.2f);
                player.removePotionEffect(PotionEffectType.SLOW);
                player.removePotionEffect(PotionEffectType.JUMP);

                Bukkit.getScheduler().scheduleSyncDelayedTask(ultimateBingo, () -> {
                    player.sendMessage(ChatColor.GREEN + "宾果游戏正在进行中。输入 /bingo 加入！");
                }, 200);

                if (ultimateBingo.bingoStarted && ultimateBingo.bingoManager.checkHasBingoCard(player) && (ultimateBingo.currentGameMode.equals("speedrun") || ultimateBingo.currentGameMode.equals("group"))) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false, true));
                }
            }

        }

    }

}
