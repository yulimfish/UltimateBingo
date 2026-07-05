// This project is based on Mega Bingo by Elmer Lion
// You can find the original project here https://github.com/ElmerLion/megabingo

// Distributed under the GNU General Public License v3.0

package io.shantek;

import io.shantek.listeners.*;
import io.shantek.managers.*;
import io.shantek.tools.MaterialList;
import io.shantek.tools.AdvancementList;
import io.shantek.tools.BingoFunctions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.Arrays;
import java.io.File;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.bukkit.map.MapPalette;

public final class UltimateBingo extends JavaPlugin {
    public BingoManager bingoManager;
    private MaterialList materialList;
    private AdvancementList advancementList;
    public BingoFunctions bingoFunctions;
    public BingoGameGUIManager bingoGameGUIManager;
    public BingoPlayerGUIManager bingoPlayerGUIManager;
    public BingoPlayerGUIListener bingoPlayerGUIListener;
    public BingoCommand bingoCommand;
    public BingoMapManager bingoMapManager;
    public BingoScoreboardManager bingoScoreboardManager;
    public RecordBoard recordBoard;
    // === Optional custom map background (128x128 PNG) ===
    // Cached as map palette bytes for fast rendering. Reloadable via /bingo reload.
    private volatile byte[] cachedParchmentBase = null;
    private volatile byte[] cachedOverlayBase = null;
    private volatile boolean[] cachedOverlayMask = null;
    private final Object mapImageLock = new Object();

    public Location bingoSpawnLocation;
    public ConfigFile configFile;
    public int gameTime = 0;
    private YamlConfiguration gameConfig;
    public CardTypes cardTypes;
    public boolean consoleLogs = true;
    public boolean bingoCardActive = false;
    public boolean respawnTeleport = true;
    public boolean bingoStarted = false;
    public Material bingoCardMaterial = Material.FILLED_MAP;
    public long gameStartTime;
    public boolean playedSinceReboot = false;
    public Metrics metrics;

    private SettingsManager settingsManager;
    public InGameConfigManager inGameConfigManager;
    public HubConfig hubConfig;
    public io.shantek.listeners.HubRegionListener hubRegionListener;

    // Add Leaderboard field
    private Leaderboard leaderboard;
    // Saved config for setting up games
    public String fullCard = "full card";
    public String difficulty;
    public String cardSize;
    public String uniqueCard;
    public String gameMode = "traditional";
    public String revealCards = "enabled";
    public int loadoutType = 1;
    public String bingoWorld = "default";
    public boolean multiWorldServer = false;
    public boolean countSoloGames = false;
    public int shuffleIntervalMinutes = 5;
    public int teleportRadius = 5000;

    // Hub world settings (hidden config keys - manually added to config.yml)
    public String hubWorld = "";
    public String hubRegion = "";
    public int hubTeleportDelay = 5;
    public Location hubSpawnLocation = null;

    // Current game configuration - Implemented to allow
    // random assignment of game setup
    public boolean currentFullCard = false;
    public String currentDifficulty;
    public String currentCardSize;
    public boolean currentUniqueCard;
    public String currentGameMode = "traditional";
    public boolean currentRevealCards = true;
    public int currentLoadoutType = 1;

    public boolean bingoButtonActive = true;

    // Shuffle mode tracking
    public int shuffleTaskId = -1;

    // Inventory used for group game mode
    public Inventory groupInventory;

    // Inventories used for teams mode
    public Inventory blueTeamInventory;
    public Inventory redTeamInventory;
    public Inventory yellowTeamInventory;


    // Very important this is never set to an item you have included in your bingo cards
    // as this will break the functionality of your game!
    public Material tickedItemMaterial = Material.LIME_CONCRETE;

    public static UltimateBingo instance;

    @Override
    public void onEnable() {

        // Save the instance of the plugin
        instance = this;

        // Load cached map backgrounds (generated parchment + optional overlay image)
        loadMapBackgrounds();

        // Initialize managers in the correct order
        settingsManager = new SettingsManager(this);

        // ===============================
        // FIX: BingoFunctions MUST exist
        // before InGameConfigManager
        // ===============================
        bingoFunctions = new BingoFunctions(this);

        // Initialize config managers early (needed by BingoCommand and listeners)
        inGameConfigManager = new InGameConfigManager(this);
        hubConfig = new HubConfig(this);

        // Initialize BingoManager first without BingoCommand
        bingoManager = new BingoManager(this, null); // Temporarily set null for BingoCommand

        // Now initialize BingoCommand and pass the actual bingoManager reference
        bingoCommand = new BingoCommand(this, settingsManager, bingoManager, inGameConfigManager);

        // Set the BingoCommand reference in BingoManager
        bingoManager.setBingoCommand(bingoCommand);

        // Continue with other managers
        materialList = new MaterialList(this);
        advancementList = new AdvancementList(this);
        bingoGameGUIManager = new BingoGameGUIManager(this);
        bingoPlayerGUIManager = new BingoPlayerGUIManager(this);
        bingoMapManager = new BingoMapManager(this);
        bingoScoreboardManager = new BingoScoreboardManager(this);
        recordBoard = new RecordBoard(this);
        cardTypes = new CardTypes(this);
        configFile = new ConfigFile(this);
        leaderboard = new Leaderboard(this);

        // Register commands
        getCommand("bingo").setExecutor(bingoCommand);
        getCommand("bingo").setTabCompleter(new BingoCompleter());

        // Check if PlaceholderAPI is installed and register placeholders
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new BingoPlaceholderExpansion(this).register();
            getLogger().info("检测到 PlaceholderAPI，正在注册占位符。");
        } else {
            getLogger().info("未找到 PlaceholderAPI，跳过占位符注册。");
        }

        registerEventListeners();

        // Ensure game settings exist
        configFile.checkforDataFolder();
        configFile.reloadConfigFile();
        hubConfig.load();

        // Register bStats
        int pluginId = 21982;
        Metrics metrics = new Metrics(this, pluginId);

        // Set signs to the correct values
        bingoFunctions.updateAllSigns();
    }


    private void registerEventListeners() {
        // Register each listener with the Bukkit plugin manager
        Bukkit.getPluginManager().registerEvents(new EntityDamageListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BingoPickupListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BingoAdvancementListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BingoInteractListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BingoMapInteractListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BingoInventoryCloseListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BingoPlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BingoGUIListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SettingsListener(materialList, settingsManager, bingoGameGUIManager, this), this);
        Bukkit.getPluginManager().registerEvents(new BingoPlayerGUIListener(materialList, bingoPlayerGUIManager, this), this);
        Bukkit.getPluginManager().registerEvents(new BingoSignListener(this, inGameConfigManager), this);
        hubRegionListener = new io.shantek.listeners.HubRegionListener(this);
        Bukkit.getPluginManager().registerEvents(hubRegionListener, this);
    }

    public BingoManager getBingoManager() {
        return bingoManager;
    }

    public MaterialList getMaterialList(){
        return materialList;
    }

    public AdvancementList getAdvancementList() {
        return advancementList;
    }

    public Leaderboard getLeaderboard() {
        return leaderboard;
    }

    public BingoFunctions getBingoFunctions(){
        return bingoFunctions;
    }

    @Override
    public void onDisable() {
        if (bingoManager != null) {
            bingoManager.clearData();
        }
        if (bingoMapManager != null) {
            bingoMapManager.clearAllMaps();
        }
        bingoStarted = false;
        instance = null;
    }

    /**
     * Check if hub mode is active. Requires multiWorldServer + non-empty hubWorld + non-empty hubRegion + WorldGuard.
     */
    public boolean isHubModeActive() {
        return multiWorldServer
                && hubWorld != null && !hubWorld.isEmpty()
                && hubRegion != null && !hubRegion.isEmpty()
                && io.shantek.tools.WorldGuardHelper.isAvailable();
    }

    public static UltimateBingo getInstance() {
        return instance;
    }


    // ---------------------------------------------------------------------
    // Map background caching + hot reload
    // ---------------------------------------------------------------------

    /**
     * Returns a cached 128x128 parchment background as MapPalette bytes.
     * Always available (generated on load).
     */
    public byte[] getCachedParchmentBase() {
        return cachedParchmentBase;
    }

    /**
     * Returns an optional cached 128x128 overlay image as MapPalette bytes, or null.
     * Only pixels with mask=true should be applied (supports PNG transparency).
     */
    public byte[] getCachedOverlayBase() {
        return cachedOverlayBase;
    }

    /**
     * Mask for overlay pixels (true = draw overlay pixel, false = leave base as-is).
     */
    public boolean[] getCachedOverlayMask() {
        return cachedOverlayMask;
    }

    /**
     * Reload cached map backgrounds (used by /bingo reload).
     */
    public void reloadMapBackgrounds() {
        loadMapBackgrounds();
        if (bingoMapManager != null) {
            bingoMapManager.forceRefreshAllMaps();
        }
    }

    /**
     * Loads the generated parchment background and (optionally) an overlay image from:
     * plugins/UltimateBingo/map/base.png (must be 128x128 PNG).
     */
    private void loadMapBackgrounds() {
        synchronized (mapImageLock) {
            // Ensure folders exist
            if (!getDataFolder().exists()) {
                //noinspection ResultOfMethodCallIgnored
                getDataFolder().mkdirs();
            }
            File mapDir = new File(getDataFolder(), "map");
            if (!mapDir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                mapDir.mkdirs();
            }

            // Always build parchment base
            cachedParchmentBase = buildParchmentBase();

            // Optional overlay
            File overlayFile = new File(mapDir, "base.png");
            if (!overlayFile.exists()) {
                cachedOverlayBase = null;
                cachedOverlayMask = null;
                getLogger().info("未找到地图覆盖层 (map/base.png)。将仅使用内置羊皮纸背景。");
                return;
            }

            try {
                BufferedImage img = ImageIO.read(overlayFile);
                if (img == null) throw new IllegalStateException("ImageIO 返回 null（图片无效？）");

                if (img.getWidth() != 128 || img.getHeight() != 128) {
                    getLogger().warning("map/base.png 必须恰好为 128x128。已忽略覆盖层。");
                    cachedOverlayBase = null;
                    cachedOverlayMask = null;
                    return;
                }

                byte[] bytes = new byte[128 * 128];
                boolean[] mask = new boolean[128 * 128];

                for (int y = 0; y < 128; y++) {
                    for (int x = 0; x < 128; x++) {
                        int argb = img.getRGB(x, y);
                        int a = (argb >> 24) & 0xFF;
                        if (a <= 0) {
                            // fully transparent
                            mask[y * 128 + x] = false;
                            bytes[y * 128 + x] = 0;
                            continue;
                        }

                        Color c = new Color(argb, true);

                        // If partially transparent, blend onto parchment first so it looks nicer.
                        if (a < 255) {
                            Color base = new Color(MapPalette.getColor(cachedParchmentBase[y * 128 + x]).getRGB());
                            float af = a / 255.0f;
                            int r = Math.round((c.getRed() * af) + (base.getRed() * (1f - af)));
                            int g = Math.round((c.getGreen() * af) + (base.getGreen() * (1f - af)));
                            int b = Math.round((c.getBlue() * af) + (base.getBlue() * (1f - af)));
                            bytes[y * 128 + x] = MapPalette.matchColor(r, g, b);
                            mask[y * 128 + x] = true;
                        } else {
                            bytes[y * 128 + x] = MapPalette.matchColor(c);
                            mask[y * 128 + x] = true;
                        }
                    }
                }

                cachedOverlayBase = bytes;
                cachedOverlayMask = mask;
                getLogger().info("已加载地图覆盖层图片：map/base.png");

            } catch (Exception e) {
                cachedOverlayBase = null;
                cachedOverlayMask = null;
                getLogger().warning("加载 map/base.png 失败。将仅使用内置羊皮纸背景。");
                e.printStackTrace();
            }
        }
    }

    /**
     * Generates a tasteful parchment-style background (128x128) as map palette bytes.
     * This is deterministic and cached on startup/reload.
     */
    private byte[] buildParchmentBase() {
        byte[] out = new byte[128 * 128];

        // Base warm parchment tone
        int baseR = 226, baseG = 214, baseB = 186;

        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 128; x++) {
                // Very subtle noise (deterministic)
                int n = ((x * 31) ^ (y * 17)) & 3; // 0..3
                int nr = baseR + (n - 1);
                int ng = baseG + (n - 1);
                int nb = baseB + (n - 1);

                // Vignette (darken edges)
                int dx = Math.min(x, 127 - x);
                int dy = Math.min(y, 127 - y);
                int d = Math.min(dx, dy); // 0 near edge

                float edge = 1.0f;
                if (d < 18) {
                    // darken within 18px of edge
                    edge = 0.78f + (d / 18.0f) * 0.22f;
                }

                int r = Math.round(nr * edge);
                int g = Math.round(ng * edge);
                int b = Math.round(nb * edge);

                out[y * 128 + x] = MapPalette.matchColor(r, g, b);
            }
        }

        return out;
    }

}
