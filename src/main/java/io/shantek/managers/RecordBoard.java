package io.shantek.managers;

import io.shantek.UltimateBingo;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Stores and ranks historical records: fastest BINGO completions
 * and most tasks ticked off per game. Persisted to records.yml.
 */
public class RecordBoard {

    private final UltimateBingo plugin;
    private final File file;
    private FileConfiguration config;

    // fastest completions: sorted list of {playerName, timeSeconds}
    private final List<Record> fastestGames = new ArrayList<>();
    // most tasks completed: playerName → maxTasks
    private final Map<String, Integer> mostTasks = new LinkedHashMap<>();

    // max entries to keep in each category
    private static final int MAX_RECORDS = 10;

    public RecordBoard(UltimateBingo plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "records.yml");
        load();
    }

    /** Record a completed game. */
    public void addGame(String playerName, int tasksCompleted, long durationMillis) {
        if (playerName == null || tasksCompleted <= 0) return;

        int timeSec = (int)(durationMillis / 1000);

        // Fastest games — sorted by time ascending
        fastestGames.add(new Record(playerName, tasksCompleted, timeSec));
        fastestGames.sort(Comparator.comparingInt(r -> r.timeSec));
        while (fastestGames.size() > MAX_RECORDS) fastestGames.remove(fastestGames.size() - 1);

        // Most tasks — keep best per player
        int prev = mostTasks.getOrDefault(playerName, 0);
        if (tasksCompleted > prev) {
            mostTasks.put(playerName, tasksCompleted);
        }
        // Sort by tasks descending, keep top N
        List<Map.Entry<String, Integer>> sorted = mostTasks.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());
        mostTasks.clear();
        for (int i = 0; i < sorted.size() && i < MAX_RECORDS; i++) {
            mostTasks.put(sorted.get(i).getKey(), sorted.get(i).getValue());
        }

        save();
    }

    // ── getters for display ──────────────────────────────

    public List<Record> getFastestGames() {
        return new ArrayList<>(fastestGames);
    }

    public Map<String, Integer> getMostTasks() {
        return new LinkedHashMap<>(mostTasks);
    }

    // ── persistence ──────────────────────────────────────

    private void load() {
        if (!file.exists()) {
            config = new YamlConfiguration();
            return;
        }
        config = YamlConfiguration.loadConfiguration(file);

        fastestGames.clear();
        if (config.contains("fastest")) {
            for (String key : config.getConfigurationSection("fastest").getKeys(false)) {
                String path = "fastest." + key;
                String name = config.getString(path + ".name", "???");
                int tasks = config.getInt(path + ".tasks", 0);
                int time = config.getInt(path + ".time", 0);
                fastestGames.add(new Record(name, tasks, time));
            }
        }

        mostTasks.clear();
        if (config.contains("most_tasks")) {
            for (String name : config.getConfigurationSection("most_tasks").getKeys(false)) {
                int tasks = config.getInt("most_tasks." + name, 0);
                if (tasks > 0) mostTasks.put(name, tasks);
            }
        }
    }

    private void save() {
        config = new YamlConfiguration();
        int i = 0;
        for (Record r : fastestGames) {
            String path = "fastest." + i;
            config.set(path + ".name", r.name);
            config.set(path + ".tasks", r.tasks);
            config.set(path + ".time", r.timeSec);
            i++;
        }
        for (Map.Entry<String, Integer> e : mostTasks.entrySet()) {
            config.set("most_tasks." + e.getKey(), e.getValue());
        }
        try {
            config.save(file);
        } catch (IOException ignored) {
            plugin.getLogger().warning("无法保存 records.yml");
        }
    }

    public record Record(String name, int tasks, int timeSec) {
        public String formattedTime() {
            if (timeSec < 60) return timeSec + "秒";
            return (timeSec / 60) + "分" + (timeSec % 60) + "秒";
        }
    }
}
