package io.shantek.managers;

import io.shantek.UltimateBingo;
import io.shantek.tools.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
     * Creates the team selection GUI with 3 wool columns and player heads.
     * Layout:
     *   Red  : wool@10, heads@19,28,37
     *   Yellow: wool@12, heads@21,30,39
     *   Blue : wool@14, heads@23,32,41
     *   Back button: @49
     */
    public Inventory createTeamSelectionGUI(Player player) {
        Inventory inv = Bukkit.createInventory(player, 54,
                ChatColor.GOLD.toString() + ChatColor.BOLD + "选择队伍");

        List<String> redPlayers    = getTeamPlayerNames("red");
        List<String> yellowPlayers = getTeamPlayerNames("yellow");
        List<String> bluePlayers   = getTeamPlayerNames("blue");

        // Red column
        inv.setItem(10, createTeamWool(Material.RED_WOOL, ChatColor.RED + "§l红队",
                redPlayers.size() + " 名玩家", ChatColor.GRAY + "点击加入红队"));
        fillPlayerHeads(inv, redPlayers, new int[]{19, 28, 37}, ChatColor.RED);

        // Yellow column
        inv.setItem(12, createTeamWool(Material.YELLOW_WOOL, ChatColor.YELLOW + "§l黄队",
                yellowPlayers.size() + " 名玩家", ChatColor.GRAY + "点击加入黄队"));
        fillPlayerHeads(inv, yellowPlayers, new int[]{21, 30, 39}, ChatColor.YELLOW);

        // Blue column
        inv.setItem(14, createTeamWool(Material.BLUE_WOOL, ChatColor.BLUE + "§l蓝队",
                bluePlayers.size() + " 名玩家", ChatColor.GRAY + "点击加入蓝队"));
        fillPlayerHeads(inv, bluePlayers, new int[]{23, 32, 41}, ChatColor.BLUE);

        // Decorative glass border top and bottom
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }
        for (int i : new int[]{0,1,2,3,4,5,6,7,8, 45,46,47,48,50,51,52,53}) {
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

    private ItemStack createTeamWool(Material wool, String title, String count, String action) {
        ItemStack item = new ItemStack(wool);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(title);
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + count,
                    action
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private List<String> getTeamPlayerNames(String teamColor) {
        List<String> names = new ArrayList<>();
        if (ultimateBingo.bingoFunctions == null) return names;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (ultimateBingo.bingoFunctions.isActivePlayer(p)) {
                String team = ultimateBingo.bingoFunctions.getTeam(p);
                if (team != null && team.equalsIgnoreCase(teamColor)) {
                    names.add(p.getName());
                }
            }
        }
        return names;
    }

    private void fillPlayerHeads(Inventory inv, List<String> playerNames, int[] slots, ChatColor color) {
        for (int i = 0; i < slots.length && i < playerNames.size(); i++) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerNames.get(i));
                meta.setOwningPlayer(offlinePlayer);
                meta.setDisplayName(color + playerNames.get(i));
                head.setItemMeta(meta);
            }
            inv.setItem(slots[i], head);
        }
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
