package io.shantek.managers;

import io.shantek.UltimateBingo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.*;

import java.util.*;

/**
 * Sidebar scoreboard showing bingo rankings.
 * Individual modes: top player completion counts.
 * Teams mode: team rankings.
 * Group mode: shared progress.
 */
public class BingoScoreboardManager {

    private final UltimateBingo plugin;
    private final Map<UUID, Scoreboard> playerBoards = new HashMap<>();
    private int timerTaskId = -1;

    public BingoScoreboardManager(UltimateBingo plugin) {
        this.plugin = plugin;
    }

    public void showBoard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("bingo", "dummy",
                ChatColor.GOLD + "" + ChatColor.BOLD + "宾果排行榜");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        playerBoards.put(player.getUniqueId(), board);
        rebuildAll();

        // Start the time-refresh ticker if not already running
        if (timerTaskId == -1) startTimeUpdater();
    }

    /** Called when any player completes an item — refreshes ALL boards. */
    public void updateAllBoards() {
        rebuildAll();
    }

    /** Called when a single player's board should refresh (same as all, ranking is global). */
    public void updateBoard(Player player) {
        rebuildAll();
    }

    public void hideBoard(Player player) {
        playerBoards.remove(player.getUniqueId());
        Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
        if (player.isOnline()) player.setScoreboard(main);
    }

    public void hideAllBoards() {
        stopTimeUpdater();
        Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
        for (UUID id : playerBoards.keySet()) {
            Player p = Bukkit.getPlayer(id);
            if (p != null && p.isOnline()) p.setScoreboard(main);
        }
        playerBoards.clear();
    }

    // ─── timer tick ──────────────────────────────────────

    private void startTimeUpdater() {
        timerTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (playerBoards.isEmpty()) {
                stopTimeUpdater();
                return;
            }
            refreshTimeOnly();
        }, 20L, 20L);
    }

    private void stopTimeUpdater() {
        if (timerTaskId != -1) {
            Bukkit.getScheduler().cancelTask(timerTaskId);
            timerTaskId = -1;
        }
    }

    /** Lightweight: only updates the time lines, keeps rankings intact. */
    private void refreshTimeOnly() {
        String timeLine = buildTimeLine();
        for (Map.Entry<UUID, Scoreboard> entry : playerBoards.entrySet()) {
            Player p = Bukkit.getPlayer(entry.getKey());
            if (p == null || !p.isOnline()) continue;

            Scoreboard board = entry.getValue();
            Objective obj = board.getObjective("bingo");
            if (obj == null) continue;

            // Remove old time/footer entries (score 0-1) and add updated time
            for (String s : board.getEntries()) {
                Score score = obj.getScore(s);
                if (score.getScore() <= 1) board.resetScores(s);
            }
            obj.getScore(timeLine).setScore(1);
        }
    }

    // ─── internal ────────────────────────────────────────

    private void rebuildAll() {
        // Gather data once
        List<String> lines = buildLines();

        for (Map.Entry<UUID, Scoreboard> entry : playerBoards.entrySet()) {
            Player p = Bukkit.getPlayer(entry.getKey());
            if (p == null || !p.isOnline()) continue;

            Scoreboard board = entry.getValue();
            Objective obj = board.getObjective("bingo");
            if (obj == null) continue;

            // Clear old entries
            for (String s : board.getEntries()) board.resetScores(s);

            // Write new entries (higher score = higher position on sidebar)
            int score = lines.size();
            for (String line : lines) {
                obj.getScore(line).setScore(score--);
            }

            p.setScoreboard(board);
        }
    }

    private List<String> buildLines() {
        boolean isTeams  = plugin.currentGameMode.equalsIgnoreCase("teams");
        boolean isGroup  = plugin.currentGameMode.equalsIgnoreCase("group");

        if (isGroup) return buildGroupLines();
        if (isTeams)  return buildTeamLines();
        return buildIndividualLines();
    }

    // ── individual ranking ────────────────────────────────

    private List<String> buildIndividualLines() {
        List<String> out = new ArrayList<>();
        int totalItems = getTotalItems();

        // Collect all active players and their completion counts
        List<PlayerScore> scores = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!plugin.bingoFunctions.isActivePlayer(p)) continue;
            Inventory gui = getPlayerGUI(p);
            int done = countCompleted(gui);
            scores.add(new PlayerScore(p.getName(), done));
        }
        scores.sort((a, b) -> Integer.compare(b.done, a.done));

        int rank = 1;
        for (int i = 0; i < scores.size() && i < 10; i++) {
            PlayerScore ps = scores.get(i);
            String prefix = rank <= 3 ? ("§e" + rank + ". ") : ("§7" + rank + ". ");
            String line = prefix + ps.name + "  §a" + ps.done + "§7/" + totalItems;
            // Trim long names to fit
            if (line.length() > 40) {
                String shortName = ps.name.length() > 12 ? ps.name.substring(0, 11) + "…" : ps.name;
                line = prefix + shortName + "  §a" + ps.done + "§7/" + totalItems;
            }
            out.add(line);
            rank++;
        }

        if (scores.isEmpty()) {
            out.add(ChatColor.GRAY + "暂无玩家");
        }

        // Spacer + time
        out.add(" ");
        out.add(buildTimeLine());

        return out;
    }

    // ── team ranking ──────────────────────────────────────

    private List<String> buildTeamLines() {
        List<String> out = new ArrayList<>();
        int totalItems = getTotalItems();

        List<TeamScore> teams = new ArrayList<>();
        teams.add(new TeamScore("§c红队", countCompleted(plugin.redTeamInventory)));
        teams.add(new TeamScore("§e黄队", countCompleted(plugin.yellowTeamInventory)));
        teams.add(new TeamScore("§9蓝队", countCompleted(plugin.blueTeamInventory)));
        teams.sort((a, b) -> Integer.compare(b.done, a.done));

        int rank = 1;
        for (TeamScore ts : teams) {
            String prefix = (rank == 1 ? "§6§l" : rank == 2 ? "§e" : "§7") + rank + ". ";
            out.add(prefix + ts.name + "  §a" + ts.done + "§7/" + totalItems);
            rank++;
        }

        out.add(" ");
        out.add(buildTimeLine());

        return out;
    }

    // ── group progress ────────────────────────────────────

    private List<String> buildGroupLines() {
        List<String> out = new ArrayList<>();
        int totalItems = getTotalItems();
        int done = countCompleted(plugin.groupInventory);

        out.add(ChatColor.GRAY + "团队进度");
        out.add("§a" + done + " §7/ " + totalItems);
        out.add("§7(" + (totalItems - done) + " 剩余)");
        out.add(" ");
        out.add(buildTimeLine());

        return out;
    }

    // ── helpers ───────────────────────────────────────────

    private String buildTimeLine() {
        if (plugin.gameTime > 0 && plugin.gameStartTime > 0) {
            long remaining = (long) plugin.gameTime * 60000
                    - (System.currentTimeMillis() - plugin.gameStartTime);
            if (remaining > 0) {
                long min = remaining / 60000;
                long sec = (remaining % 60000) / 1000;
                return ChatColor.WHITE + "剩余: " + ChatColor.AQUA + min + "分" + sec + "秒";
            }
        }
        long elapsed = plugin.bingoStarted
                ? (System.currentTimeMillis() - plugin.gameStartTime) / 1000
                : 0;
        return ChatColor.GRAY + "已过: " + (elapsed / 60) + "分" + (elapsed % 60) + "秒";
    }

    private int countCompleted(Inventory gui) {
        if (gui == null) return 0;
        int[] slots = plugin.bingoManager.getSlots();
        if (slots == null) return 0;
        int count = 0;
        for (int slot : slots) {
            ItemStack item = gui.getItem(slot);
            if (item != null && item.getType() == plugin.tickedItemMaterial) count++;
        }
        return count;
    }

    private int getTotalItems() {
        int[] slots = plugin.bingoManager.getSlots();
        return slots != null ? slots.length : 0;
    }

    private Inventory getPlayerGUI(Player player) {
        if (plugin.currentGameMode.equalsIgnoreCase("group")) return plugin.groupInventory;
        if (plugin.currentGameMode.equalsIgnoreCase("teams")) return plugin.bingoFunctions.getTeamInventory(player);
        Map<UUID, Inventory> guis = plugin.bingoManager.getBingoGUIs();
        return guis != null ? guis.get(player.getUniqueId()) : null;
    }

    // ── data classes ──────────────────────────────────────

    private record PlayerScore(String name, int done) {}
    private record TeamScore(String name, int done) {}
}
