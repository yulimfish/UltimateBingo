package io.shantek.managers;

import io.shantek.UltimateBingo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Manages the optional hub.yml config file.
 * This file is never auto-created — the server admin must manually create it
 * to enable hub world functionality. If the file doesn't exist, hub mode
 * stays disabled and this class does nothing.
 *
 * Example hub.yml:
 * <pre>
 * # The Multiverse world name for your lobby
 * hub-world: lobby
 *
 * # The WorldGuard region name players must be in to join
 * hub-region: bingo_lobby
 *
 * # Seconds to wait after teleporting players before starting countdown
 * hub-teleport-delay: 5
 * </pre>
 *
 * The hub-spawn key is written automatically by /bingo set hubspawn
 */
public class HubConfig {

    private final UltimateBingo plugin;
    private final File hubFile;

    public HubConfig(UltimateBingo plugin) {
        this.plugin = plugin;
        this.hubFile = new File(plugin.getDataFolder(), "hub.yml");
    }

    /**
     * Attempt to load hub.yml. If the file doesn't exist, hub mode stays disabled.
     * Called on plugin enable and on /bingo reload.
     */
    public void load() {
        // Reset to defaults
        plugin.hubWorld = "";
        plugin.hubRegion = "";
        plugin.hubTeleportDelay = 5;
        plugin.hubSpawnLocation = null;

        if (!hubFile.exists()) {
            // No hub.yml — hub mode disabled, nothing to do
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(hubFile);

        plugin.hubWorld = config.getString("hub-world", "");
        plugin.hubRegion = config.getString("hub-region", "");
        plugin.hubTeleportDelay = config.getInt("hub-teleport-delay", 5);

        if (config.contains("hub-spawn")) {
            plugin.hubSpawnLocation = parseLocation(config.getString("hub-spawn"));
        }

        // Log what we found
        if (!plugin.hubWorld.isEmpty() && !plugin.hubRegion.isEmpty()) {
            plugin.getLogger().info("大厅模式已启用：世界=" + plugin.hubWorld
                    + ", 区域=" + plugin.hubRegion
                    + ", 延迟=" + plugin.hubTeleportDelay + "秒"
                    + (plugin.hubSpawnLocation != null ? ", 出生点已设置" : ", 出生点未设置（使用 /bingo set hubspawn）"));
        } else {
            plugin.getLogger().info("找到 hub.yml，但 hub-world 或 hub-region 为空。大厅模式已禁用。");
        }
    }

    /**
     * Save the hub spawn location to hub.yml.
     * Only writes the hub-spawn key — preserves all other keys.
     */
    public void saveHubSpawn(Location location) {
        if (!hubFile.exists()) {
            plugin.getLogger().warning("无法保存大厅出生点 —— hub.yml 不存在！");
            return;
        }

        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(hubFile);
            config.set("hub-spawn", serializeLocation(location));
            config.save(hubFile);
            plugin.hubSpawnLocation = location;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "保存大厅出生点到 hub.yml 失败", e);
        }
    }

    /**
     * Check whether hub.yml exists on disk.
     */
    public boolean exists() {
        return hubFile.exists();
    }

    private String serializeLocation(Location loc) {
        if (loc == null || loc.getWorld() == null) return "";
        return loc.getWorld().getName() + ","
                + loc.getX() + "," + loc.getY() + "," + loc.getZ() + ","
                + loc.getYaw() + "," + loc.getPitch();
    }

    private Location parseLocation(String locString) {
        if (locString == null || locString.isEmpty()) return null;
        String[] parts = locString.split(",");
        if (parts.length < 4) return null;
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0;
        float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0;
        return new Location(world, x, y, z, yaw, pitch);
    }
}
