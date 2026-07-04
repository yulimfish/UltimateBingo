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

public class BingoPlayerGUIManager {
    UltimateBingo ultimateBingo;

    public BingoPlayerGUIManager(UltimateBingo ultimateBingo) {
        this.ultimateBingo = ultimateBingo;
    }

    public Inventory createPlayerGUI(Player player) {
        Inventory gameConfigInventory = Bukkit.createInventory(player, 9, ChatColor.GOLD.toString() + ChatColor.LIGHT_PURPLE + "欢迎来到终极宾果");

        gameConfigInventory.setItem(0, createItem(ultimateBingo.bingoCardMaterial, "备用宾果卡片", "胜利条件：" + ultimateBingo.fullCard.toUpperCase()));

        // Only show the reveal cards option if this is enabled
        if (ultimateBingo.currentRevealCards) { gameConfigInventory.setItem(1, createItem(Material.SPYGLASS, "查看玩家卡片", "偷偷看一眼其他玩家的卡片！"));}

        return gameConfigInventory;
    }

    private ItemStack createItem(Material material, String displayname, String lore) {
        return new ItemBuilder(material)
                .withDisplayName(ChatColor.BLUE + displayname)
                .withLore(ChatColor.GRAY + lore).build();
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
