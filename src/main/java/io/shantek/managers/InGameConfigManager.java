package io.shantek.managers;

import io.shantek.UltimateBingo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class InGameConfigManager {

    private final UltimateBingo plugin;
    private final File configFile;
    private FileConfiguration config;

    public InGameConfigManager(UltimateBingo plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "ingameconfig.yml");
        loadConfig();
    }

    private void loadConfig() {
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("无法创建 ingameconfig.yml！");
            }
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        loadSignLocations();
        loadButtonLocation();
        loadTeamSignLocations();
    }

    public void loadSignLocations() {
        if (!configFile.exists()) {
            saveConfig(); // Ensure the file exists before loading
            return;
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        if (!config.contains("signs")) {
            config.createSection("signs"); // Ensure section exists
            saveConfig();
        }

        ConfigurationSection signsSection = config.getConfigurationSection("signs");
        if (signsSection == null) return; // Prevent null error

        for (String key : signsSection.getKeys(false)) {
            Location loc = parseLocation(config.getString("signs." + key));
            if (loc != null) {
                plugin.bingoFunctions.signLocations.put(key, loc);
            }
        }
    }

    private void loadButtonLocation() {
        String value = config.getString("button.startbutton");
        if (value != null && !value.isEmpty()) {
            plugin.bingoFunctions.startButtonLocation = parseLocation(value);
        }
    }

    public void saveSignLocation(String name, Location location) {
        plugin.bingoFunctions.signLocations.put(name, location);
        config.set("signs." + name, serializeLocation(location));
        saveConfig();

        loadSignLocations();
    }

    public void saveButtonLocation(Location location) {
        plugin.bingoFunctions.startButtonLocation = location;
        config.set("button.startbutton", serializeLocation(location));
        saveConfig();

        loadSignLocations();
    }

    public Location getSignLocation(String name) {
        return plugin.bingoFunctions.signLocations.get(name);
    }

    public Location getStartButtonLocation() {
        return plugin.bingoFunctions.startButtonLocation;
    }

    // --- Team signs ---

    public void saveTeamSignLocation(String team, Location location) {
        plugin.bingoFunctions.teamSignLocations.put(team.toLowerCase(), location);
        config.set("teamsigns." + team.toLowerCase(), serializeLocation(location));
        saveConfig();
    }

    public void removeTeamSign(String team) {
        plugin.bingoFunctions.teamSignLocations.remove(team.toLowerCase());
        config.set("teamsigns." + team.toLowerCase(), null);
        saveConfig();
    }

    public void loadTeamSignLocations() {
        plugin.bingoFunctions.teamSignLocations.clear();
        if (!config.contains("teamsigns")) return;
        ConfigurationSection section = config.getConfigurationSection("teamsigns");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            Location loc = parseLocation(config.getString("teamsigns." + key));
            if (loc != null) {
                plugin.bingoFunctions.teamSignLocations.put(key.toLowerCase(), loc);
            }
        }
    }

    private String serializeLocation(Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    private Location parseLocation(String serialized) {
        String[] parts = serialized.split(",");
        if (parts.length != 4) return null;
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        return new Location(world, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
    }

    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().warning("无法保存 ingameconfig.yml！");
        }
    }
}
