package io.shantek.tools;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Reflection-based WorldGuard helper. No compile-time dependency on WorldGuard.
 * Queries players inside a named region in a given world.
 */
public class WorldGuardHelper {

    /**
     * Get all online players standing inside the given WorldGuard region.
     *
     * @param worldName  The world containing the region
     * @param regionName The region ID to check
     * @return List of players inside the region, or empty list if WG not available
     */
    public static List<Player> getPlayersInRegion(String worldName, String regionName) {
        List<Player> result = new ArrayList<>();

        try {
            Plugin wgPlugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
            if (wgPlugin == null) return result;

            World world = Bukkit.getWorld(worldName);
            if (world == null) return result;

            // WorldGuard 7.x API via reflection:
            // WorldGuard.getInstance().getPlatform().getRegionContainer()
            Class<?> wgClass = Class.forName("com.sk89q.worldguard.WorldGuard");
            Object wgInstance = wgClass.getMethod("getInstance").invoke(null);
            Object platform = wgInstance.getClass().getMethod("getPlatform").invoke(wgInstance);
            Object regionContainer = platform.getClass().getMethod("getRegionContainer").invoke(platform);

            // BukkitAdapter.adapt(world)
            Class<?> bukkitAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
            Object weWorld = bukkitAdapterClass.getMethod("adapt", World.class).invoke(null, world);

            // regionContainer.get(weWorld)
            Method getMethod = regionContainer.getClass().getMethod("get", Class.forName("com.sk89q.worldedit.world.World"));
            Object regionManager = getMethod.invoke(regionContainer, weWorld);
            if (regionManager == null) return result;

            // regionManager.getRegion(regionName)
            Object region = regionManager.getClass().getMethod("getRegion", String.class).invoke(regionManager, regionName);
            if (region == null) return result;

            // For each online player in the target world, check if they're inside the region
            // region.contains(BlockVector3.at(x, y, z))
            Class<?> bv3Class = Class.forName("com.sk89q.worldedit.math.BlockVector3");
            Method atMethod = bv3Class.getMethod("at", int.class, int.class, int.class);
            Method containsMethod = region.getClass().getMethod("contains", bv3Class);

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.getWorld().getName().equalsIgnoreCase(worldName)) continue;

                Location loc = player.getLocation();
                Object bv3 = atMethod.invoke(null, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                boolean inside = (boolean) containsMethod.invoke(region, bv3);
                if (inside) {
                    result.add(player);
                }
            }

        } catch (ClassNotFoundException e) {
            // WorldGuard classes not found — WG not installed or wrong version
            Bukkit.getLogger().log(Level.WARNING, "[终极宾果] 未找到 WorldGuard 类。是否已安装 WorldGuard 7.x？");
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "[终极宾果] 查询 WorldGuard 区域时出错", e);
        }

        return result;
    }

    /**
     * Check if WorldGuard is available and loaded.
     */
    public static boolean isAvailable() {
        return Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
    }
}
