package io.shantek.managers;

import io.shantek.UltimateBingo;
import io.shantek.tools.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BingoPlayerGUIManager {
    UltimateBingo ultimateBingo;

    public BingoPlayerGUIManager(UltimateBingo ultimateBingo) {
        this.ultimateBingo = ultimateBingo;
    }

    public Inventory createPlayerGUI(Player player) {
        Inventory gameConfigInventory = Bukkit.createInventory(player, 9, ChatColor.GOLD.toString() + ChatColor.LIGHT_PURPLE + "欢迎来到终极宾果");

        gameConfigInventory.setItem(0, createItem(ultimateBingo.bingoCardMaterial, "备用宾果卡片", "胜利条件：" + ultimateBingo.fullCard.toUpperCase()));

        // Teams mode: show team selection button
        if (ultimateBingo.currentGameMode.equalsIgnoreCase("teams")) {
            ItemStack teamItem = new ItemStack(Material.RED_BANNER);
            ItemMeta teamMeta = teamItem.getItemMeta();
            if (teamMeta != null) {
                String currentTeam = ultimateBingo.bingoFunctions.getTeam(player);
                String teamDisplay;
                if (currentTeam.equalsIgnoreCase("red")) {
                    teamDisplay = ChatColor.RED + "当前队伍：红队";
                } else if (currentTeam.equalsIgnoreCase("blue")) {
                    teamDisplay = ChatColor.BLUE + "当前队伍：蓝队";
                } else if (currentTeam.equalsIgnoreCase("yellow")) {
                    teamDisplay = ChatColor.YELLOW + "当前队伍：黄队";
                } else {
                    teamDisplay = ChatColor.GRAY + "尚未选择队伍";
                }
                teamMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "选择队伍");
                teamMeta.setLore(Arrays.asList(teamDisplay, ChatColor.GRAY + "点击打开队伍选择页面"));
                teamItem.setItemMeta(teamMeta);
            }
            gameConfigInventory.setItem(2, teamItem);
        }

        // Only show the reveal cards option if this is enabled
        if (ultimateBingo.currentRevealCards) {
            gameConfigInventory.setItem(4, createItem(Material.SPYGLASS, "查看玩家卡片", "偷偷看一眼其他玩家的卡片！"));
        }

        return gameConfigInventory;
    }

    private ItemStack createItem(Material material, String displayname, String lore) {
        return new ItemBuilder(material)
                .withDisplayName(ChatColor.BLUE + displayname)
                .withLore(ChatColor.GRAY + lore).build();
    }

    /**
     * Creates the team selection GUI.
     * 3 wool columns (red/yellow/blue) showing team member names in lore.
     * No player heads to avoid Mojang API calls.
     * Back button at bottom center.
     */
    public Inventory createTeamSelectionGUI(Player player) {
        Inventory inv = Bukkit.createInventory(player, 54,
                ChatColor.GOLD.toString() + ChatColor.BOLD + "选择队伍");

        List<String> redPlayers    = getTeamPlayerNames("red");
        List<String> yellowPlayers = getTeamPlayerNames("yellow");
        List<String> bluePlayers   = getTeamPlayerNames("blue");

        // Red column - wool at 10
        inv.setItem(10, createTeamWool(Material.RED_WOOL, ChatColor.RED + "§l红队",
                redPlayers, ChatColor.GRAY + "点击加入红队"));

        // Yellow column - wool at 13 (centered between red and blue)
        inv.setItem(13, createTeamWool(Material.YELLOW_WOOL, ChatColor.YELLOW + "§l黄队",
                yellowPlayers, ChatColor.GRAY + "点击加入黄队"));

        // Blue column - wool at 16
        inv.setItem(16, createTeamWool(Material.BLUE_WOOL, ChatColor.BLUE + "§l蓝队",
                bluePlayers, ChatColor.GRAY + "点击加入蓝队"));

        // Decorative glass border
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }
        for (int i : new int[]{0,1,2,3,4,5,6,7,8, 45,46,47,48,49,50,51,52,53}) {
            inv.setItem(i, glass);
        }

        // Back button at bottom center
        ItemStack back = new ItemStack(Material.CHEST);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(ChatColor.RED + "返回菜单");
            backMeta.setLore(Arrays.asList(ChatColor.GRAY + "返回主界面"));
            back.setItemMeta(backMeta);
        }
        inv.setItem(49, back);

        return inv;
    }

    private ItemStack createTeamWool(Material wool, String title, List<String> players, String action) {
        ItemStack item = new ItemStack(wool);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(title);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY.toString() + players.size() + " 名玩家");
            if (!players.isEmpty()) {
                lore.add(ChatColor.DARK_GRAY + String.join(", ", players));
            }
            lore.add(action);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Returns player names belonging to a team.
     * Checks both playerTeamsMap (post-assignment) and manualTeamAssignments (pre-game).
     */
    private List<String> getTeamPlayerNames(String teamColor) {
        List<String> names = new ArrayList<>();
        if (ultimateBingo.bingoFunctions == null) return names;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (ultimateBingo.bingoFunctions.isActivePlayer(p)) {
                String team = ultimateBingo.bingoFunctions.getTeam(p);
                String manualTeam = ultimateBingo.bingoFunctions.getManualTeam(p.getUniqueId());
                if ((team != null && team.equalsIgnoreCase(teamColor))
                        || (manualTeam != null && manualTeam.equalsIgnoreCase(teamColor))) {
                    if (!names.contains(p.getName())) {
                        names.add(p.getName());
                    }
                }
            }
        }
        return names;
    }

    public Inventory setupPlayersBingoCardsInventory() {
        // Create a 54-slot inventory with a custom title
        Inventory inventory = null;

        // Do team cards and use wool

        if (ultimateBingo.currentGameMode.equalsIgnoreCase("teams")) {
            inventory = Bukkit.createInventory(null, 54, ChatColor.GOLD.toString() + ChatColor.LIGHT_PURPLE + "队伍宾果卡片");


            if (ultimateBingo.bingoFunctions.isRedTeamNotEmpty()) {
                ItemStack redTeam = new ItemStack(Material.RED_WOOL);
                ItemMeta redMeta = redTeam.getItemMeta();
                if (redMeta != null) {
                    redMeta.setDisplayName(ChatColor.RED + "红队");
                    redMeta.setLore(Arrays.asList(ultimateBingo.bingoFunctions.getRedTeamPlayerNames().split(", ")));
                    redTeam.setItemMeta(redMeta);

                    inventory.addItem(redTeam);
                }
            }

            if (ultimateBingo.bingoFunctions.isBlueTeamNotEmpty()) {
                ItemStack blueTeam = new ItemStack(Material.BLUE_WOOL);
                ItemMeta blueMeta = blueTeam.getItemMeta();
                if (blueMeta != null) {
                    blueMeta.setDisplayName(ChatColor.BLUE + "蓝队");
                    blueMeta.setLore(Arrays.asList(ultimateBingo.bingoFunctions.getBlueTeamPlayerNames().split(", ")));
                    blueTeam.setItemMeta(blueMeta);
                    inventory.addItem(blueTeam);
                }
            }

            if (ultimateBingo.bingoFunctions.isYellowTeamNotEmpty()) {
                ItemStack yellowTeam = new ItemStack(Material.YELLOW_WOOL);
                ItemMeta yellowMeta = yellowTeam.getItemMeta();
                if (yellowMeta != null) {
                    yellowMeta.setDisplayName(ChatColor.YELLOW + "黄队");
                    yellowMeta.setLore(Arrays.asList(ultimateBingo.bingoFunctions.getYellowTeamPlayerNames().split(", ")));
                    yellowTeam.setItemMeta(yellowMeta);
                    inventory.addItem(yellowTeam);
                }
            }

        } else {

            inventory = Bukkit.createInventory(null, 54, ChatColor.GOLD.toString() + ChatColor.LIGHT_PURPLE + "玩家宾果卡片");

            // Get all online players and populate the inventory
            for (Player player : Bukkit.getOnlinePlayers()) {

                if (ultimateBingo.bingoFunctions.canInteractWithCard(player)) {

                    if (ultimateBingo.bingoManager.bingoGUIs.containsKey(player.getUniqueId())) {  // Check if the player has a bingo card
                        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
                        SkullMeta meta = (SkullMeta) playerHead.getItemMeta();
                        meta.setOwningPlayer(player);

                        if (meta != null) {
                            meta.setDisplayName(ChatColor.GREEN + player.getName());
                            List<String> lore = new ArrayList<>();
                            lore.add(ChatColor.GRAY + "点击查看 " + player.getName() + " 的宾果卡片");
                            meta.setLore(lore);  // Set lore
                            playerHead.setItemMeta(meta);

                            int countCompleted = ultimateBingo.bingoFunctions.countCompleted(ultimateBingo.bingoManager.getBingoGUIs().get(player.getUniqueId()));
                            if (countCompleted > 0) {
                                playerHead.setAmount(countCompleted);
                            }

                            inventory.addItem(playerHead);  // Add the item to the inventory
                        }
                    }
                }
            }
        }

        // Add a 'Back to menu' chest in the last slot
        ItemStack backToMenu = new ItemStack(Material.CHEST);
        ItemMeta backToMenuMeta = backToMenu.getItemMeta();
        if (backToMenuMeta != null) {
            backToMenuMeta.setDisplayName(ChatColor.RED + "返回菜单");
            backToMenu.setItemMeta(backToMenuMeta);
        }
        inventory.setItem(53, backToMenu);  // Set the chest in the last slot

        return inventory;
    }
}
