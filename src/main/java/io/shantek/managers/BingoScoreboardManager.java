package io.shantek.managers;

import io.shantek.UltimateBingo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages per-player scoreboards showing bingo progress on the sidebar.
 * Created when player joins game, removed when they leave or game ends.
 */
public class BingoScoreboardManager {

    private final UltimateBingo plugin;
    private final Map<UUID, Scoreboard> playerBoards = new HashMap<>();

    public BingoScoreboardManager(UltimateBingo plugin) {
        this.plugin = plugin;
    }

    /** Show the bingo scoreboard for a player. */
    public void showBoard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("bingo", "dummy",
                ChatColor.GOLD + "" + ChatColor.BOLD + "宾果进度");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        updateBoardContent(player, obj);
        player.setScoreboard(board);
        playerBoards.put(player.getUniqueId(), board);
    }

    /** Refresh the scoreboard content for a player. */
    public void updateBoard(Player player) {
        Scoreboard board = playerBoards.get(player.getUniqueId());
        if (board == null) return;

        Objective obj = board.getObjective("bingo");
        if (obj == null) return;

        // Clear old entries
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }

        updateBoardContent(player, obj);
    }

    private void updateBoardContent(Player player, Objective obj) {
        org.bukkit.inventory.Inventory gui = getPlayerGUI(player);
        int completed = 0;
        int total = 0;

        if (gui != null) {
            int[] slots = plugin.bingoManager.getSlots();
            if (slots != null) {
                total = slots.length;
                for (int slot : slots) {
                    org.bukkit.inventory.ItemStack item = gui.getItem(slot);
                    if (item != null && item.getType() == plugin.tickedItemMaterial) {
                        completed++;
                    }
                }
            }
        }

        int remaining = total - completed;
        String progress = ChatColor.WHITE + "已完成: " + ChatColor.GREEN + completed
                + ChatColor.WHITE + " / " + total
                + "  (" + ChatColor.YELLOW + remaining + ChatColor.WHITE + " 剩余)";

        setScore(obj, ChatColor.GREEN + "进度", 5);
        setScore(obj, progress, 4);
        setScore(obj, " ", 3);

        // Time remaining (if game has a time limit)
        if (plugin.gameTime > 0 && plugin.gameStartTime > 0) {
            long elapsed = System.currentTimeMillis() - plugin.gameStartTime;
            long remainingMs = (long) plugin.gameTime * 60 * 1000 - elapsed;
            if (remainingMs > 0) {
                long remainingMin = remainingMs / 60000;
                long remainingSec = (remainingMs % 60000) / 1000;
                String time = ChatColor.WHITE + "剩余时间: " + ChatColor.AQUA
                        + remainingMin + "分" + remainingSec + "秒";
                setScore(obj, time, 2);
            }
        }

        setScore(obj, ChatColor.GRAY + "UltimateBingo", 0);
    }

    private void setScore(Objective obj, String text, int score) {
        Score s = obj.getScore(text);
        s.setScore(score);
    }

    private org.bukkit.inventory.Inventory getPlayerGUI(Player player) {
        if (plugin.currentGameMode.equalsIgnoreCase("group")) {
            return plugin.groupInventory;
        }
        if (plugin.currentGameMode.equalsIgnoreCase("teams")) {
            return plugin.bingoFunctions.getTeamInventory(player);
        }
        Map<UUID, org.bukkit.inventory.Inventory> guis = plugin.bingoManager.getBingoGUIs();
        if (guis != null) {
            return guis.get(player.getUniqueId());
        }
        return null;
    }

    /** Hide and remove the scoreboard for a player. */
    public void hideBoard(Player player) {
        playerBoards.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    /** Remove all scoreboards (called on game end). */
    public void hideAllBoards() {
        for (UUID id : playerBoards.keySet()) {
            Player p = Bukkit.getPlayer(id);
            if (p != null && p.isOnline()) {
                p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
        }
        playerBoards.clear();
    }
}
