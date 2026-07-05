package io.shantek.managers;

import io.shantek.UltimateBingo;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class RecordBoard {

    private final UltimateBingo plugin;
    private final File file;
    private FileConfiguration config;

    // All historical records with unique IDs
    private final List<GameRecord> allRecords = new ArrayList<>();
    private int nextId = 1;

    // Cached leaderboard for sidebar (top 10)
    private List<GameRecord> fastestCache = new ArrayList<>();
    private Map<String, Integer> mostTasksCache = new LinkedHashMap<>();
    private static final int MAX_LEADERBOARD = 10;

    public RecordBoard(UltimateBingo plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "records.yml");
        load();
    }

    /** Record a completed game. Returns the assigned ID. */
    public int addGame(String playerName, int tasksCompleted, long durationMillis, int cardSize) {
        if (playerName == null || tasksCompleted <= 0) return -1;

        int timeSec = (int)(durationMillis / 1000);
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());

        GameRecord rec = new GameRecord(nextId, playerName, tasksCompleted, timeSec, cardSize, date);
        allRecords.add(rec);
        nextId++;

        rebuildCaches();
        save();
        return rec.id;
    }

    /** Delete a record by ID. Returns true if found and deleted. */
    public boolean deleteRecord(int id) {
        boolean removed = allRecords.removeIf(r -> r.id == id);
        if (removed) {
            rebuildCaches();
            save();
        }
        return removed;
    }

    /** Get all records sorted by most recent first. */
    public List<GameRecord> getAllRecords() {
        List<GameRecord> copy = new ArrayList<>(allRecords);
        copy.sort((a, b) -> Integer.compare(b.id, a.id)); // newest first
        return copy;
    }

    // ── sidebar leaderboard (computed from allRecords) ───

    public List<GameRecord> getFastestGames() {
        return new ArrayList<>(fastestCache);
    }

    public Map<String, Integer> getMostTasks() {
        return new LinkedHashMap<>(mostTasksCache);
    }

    // ── internal ──────────────────────────────────────────

    private void rebuildCaches() {
        // Fastest — sorted by time ascending
        fastestCache = new ArrayList<>(allRecords);
        fastestCache.sort(Comparator.comparingInt(r -> r.timeSec));
        if (fastestCache.size() > MAX_LEADERBOARD)
            fastestCache = fastestCache.subList(0, MAX_LEADERBOARD);

        // Most tasks — keep best per player
        Map<String, Integer> map = new HashMap<>();
        for (GameRecord r : allRecords) {
            map.merge(r.name, r.tasks, Integer::max);
        }
        List<Map.Entry<String, Integer>> sorted = map.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());
        mostTasksCache.clear();
        for (int i = 0; i < sorted.size() && i < MAX_LEADERBOARD; i++)
            mostTasksCache.put(sorted.get(i).getKey(), sorted.get(i).getValue());
    }

    private void load() {
        if (!file.exists()) { config = new YamlConfiguration(); return; }
        config = YamlConfiguration.loadConfiguration(file);

        allRecords.clear();
        if (config.contains("all")) {
            for (String key : config.getConfigurationSection("all").getKeys(false)) {
                String path = "all." + key;
                int id = parseInt(key);
                String name = config.getString(path + ".name", "???");
                int tasks = config.getInt(path + ".tasks", 0);
                int time = config.getInt(path + ".time", 0);
                int size = config.getInt(path + ".size", 0);
                String date = config.getString(path + ".date", "");
                allRecords.add(new GameRecord(id, name, tasks, time, size, date));
                if (id >= nextId) nextId = id + 1;
            }
        }
        rebuildCaches();
    }

    private void save() {
        config = new YamlConfiguration();
        for (GameRecord r : allRecords) {
            String path = "all." + r.id;
            config.set(path + ".name", r.name);
            config.set(path + ".tasks", r.tasks);
            config.set(path + ".time", r.timeSec);
            config.set(path + ".size", r.cardSize);
            config.set(path + ".date", r.date);
        }
        try { config.save(file); } catch (IOException ignored) {
            plugin.getLogger().warning("无法保存 records.yml");
        }
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return 0; }
    }

    // ── data class ────────────────────────────────────────

    public record GameRecord(int id, String name, int tasks, int timeSec, int cardSize, String date) {
        public String formattedTime() {
            if (timeSec < 60) return timeSec + "秒";
            return (timeSec / 60) + "分" + (timeSec % 60) + "秒";
        }
        public String formattedLine() {
            return "§6#" + id + " §7| §f" + name + " §7| §a" + tasks + "项 §7| "
                    + formattedTime() + " §7| " + cardSize + "x" + cardSize + " §7| " + date;
        }
    }
}
