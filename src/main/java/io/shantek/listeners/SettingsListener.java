package io.shantek.listeners;

import io.shantek.UltimateBingo;
import io.shantek.managers.BingoGameGUIManager;
import io.shantek.managers.SettingsManager;
import io.shantek.tools.ItemBuilder;
import io.shantek.tools.MaterialList;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class SettingsListener implements Listener {
    MaterialList materialList;
    private SettingsManager settingsManager;
    public BingoGameGUIManager bingoGameGUIManager;
    public UltimateBingo ultimateBingo;
    private boolean sentWarning;

    public SettingsListener(MaterialList materialList, SettingsManager settingsManager, BingoGameGUIManager bingoGameGUIManager, UltimateBingo ultimateBingo) {
        this.materialList = materialList;
        this.settingsManager = settingsManager;
        this.bingoGameGUIManager = bingoGameGUIManager;
        this.ultimateBingo = ultimateBingo;
        sentWarning = false;
    }

    Random random = new Random(); // Create a Random object

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        if (!(e.getWhoClicked() instanceof Player)) return;

        Player player = (Player) e.getWhoClicked();

        if (ultimateBingo.bingoFunctions.isActivePlayer(player)) {

            // Ensure the event was triggered in the Bingo configuration GUI
            if (e.getView().getTitle().contains("宾果配置")) {
                e.setCancelled(true);  // Prevent dragging items

                int slot = e.getRawSlot();
                // Ensure clicks are within the inventory size
                if (slot >= 0 && slot < 9) {
                    switch (slot) {
                        case 0:
                            bingoGameGUIManager.toggleGameMode(player);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                            break;
                        case 1:
                            bingoGameGUIManager.toggleDifficulty(player);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                            break;
                        case 2:
                            bingoGameGUIManager.toggleCardSize(player);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                            break;
                        case 3:
                            bingoGameGUIManager.toggleCardType(player);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                            break;
                        case 4:
                            bingoGameGUIManager.toggleWinCondition(player);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                            break;
                        case 5:
                            bingoGameGUIManager.toggleRevealCards(player);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                            break;
                        case 6:
                            bingoGameGUIManager.toggleGameTime(player);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                            break;
                        case 7:
                            bingoGameGUIManager.toggleLoadout(player);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                            break;
                        case 8:

                            // Set all the game config ready to play
                            ultimateBingo.bingoGameGUIManager.setGameConfiguration();

                            ultimateBingo.bingoSpawnLocation = player.getLocation();
                            ultimateBingo.bingoCommand.startBingo(player);

                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                            player.closeInventory();
                            break;
                        default:
                            // This case handles any undefined slots, no action is taken
                            break;
                    }
                }

                ultimateBingo.configFile.saveConfig();

            } else if (e.getView().getTitle().equals(ChatColor.GOLD.toString() + ChatColor.BOLD + "宾果设置")) {
                e.setCancelled(true);

                if (e.getCurrentItem().getItemMeta() != null) {
                    createItemSettings(player, settingsManager.getDifficultyInt(e.getCurrentItem().getItemMeta().getDisplayName()));
                }

            } else {

                boolean isEasyDifficulty = ChatColor.translateAlternateColorCodes('&'
                        , e.getView().getTitle()).equals(settingsManager.getDifficultyDisplay(1));

                boolean isNormalDifficulty = ChatColor.translateAlternateColorCodes('&'
                        , e.getView().getTitle()).equals(settingsManager.getDifficultyDisplay(2));

                boolean isHardDifficulty = ChatColor.translateAlternateColorCodes('&'
                        , e.getView().getTitle()).equals(settingsManager.getDifficultyDisplay(3));

                boolean isExtremeDifficulty = ChatColor.translateAlternateColorCodes('&'
                        , e.getView().getTitle()).equals(settingsManager.getDifficultyDisplay(4));

                boolean isImpossibleDifficulty = ChatColor.translateAlternateColorCodes('&'
                        , e.getView().getTitle()).equals(settingsManager.getDifficultyDisplay(5));

                if (e.getClickedInventory() == e.getView().getTopInventory() && e.getCurrentItem() != null) {
                    if (isEasyDifficulty || isNormalDifficulty || isHardDifficulty || isExtremeDifficulty || isImpossibleDifficulty) {
                        e.setCancelled(true);

                        ItemStack clickedItem = e.getCurrentItem();

                        boolean itemRemoved = false;

                        if (e.getClick().isLeftClick()) {

                            if (isEasyDifficulty) {

                                if (materialList.easy.size() <= 15) {
                                    player.sendMessage(ChatColor.RED + "该分类至少需要保留 15 个物品。");
                                } else {
                                    itemRemoved = true;
                                    materialList.removeItem(clickedItem.getType(), 1);
                                    createItemSettings(player, 1);
                                }
                            } else if (isNormalDifficulty) {
                                if (materialList.normal.size() <= 15) {
                                    player.sendMessage(ChatColor.RED + "该分类至少需要保留 15 个物品。");
                                } else {
                                    itemRemoved = true;
                                    materialList.removeItem(clickedItem.getType(), 2);
                                    createItemSettings(player, 2);
                                }
                            } else if (isHardDifficulty) {
                                if (materialList.hard.size() <= 10) {
                                    player.sendMessage(ChatColor.RED + "该分类至少需要保留 10 个物品。");
                                } else {
                                    itemRemoved = true;
                                    materialList.removeItem(clickedItem.getType(), 3);
                                    createItemSettings(player, 3);
                                }
                            } else if (isExtremeDifficulty) {
                                if (materialList.extreme.size() <= 10) {
                                    player.sendMessage(ChatColor.RED + "该分类至少需要保留 10 个物品。");
                                } else {
                                    itemRemoved = true;
                                    materialList.removeItem(clickedItem.getType(), 4);
                                    createItemSettings(player, 4);
                                }
                            } else if (isImpossibleDifficulty) {
                                if (materialList.impossible.size() <= 5) {
                                    player.sendMessage(ChatColor.RED + "该分类至少需要保留 5 个物品。");
                                } else {
                                    itemRemoved = true;
                                    materialList.removeItem(clickedItem.getType(), 5);
                                    createItemSettings(player, 5);
                                }
                            }
                            if (itemRemoved) {
                                player.sendMessage(ChatColor.GREEN + "你将 "
                                        + ChatColor.GOLD + ultimateBingo.bingoFunctions.getMaterialName(clickedItem.getType()) + ChatColor.GREEN + " 从宾果物品中移除了");
                            }
                        }
                    }
                }

                Inventory clickedInv = e.getClickedInventory();
                if (clickedInv != null && clickedInv.getType() == InventoryType.PLAYER) {

                    if (isEasyDifficulty || isNormalDifficulty || isHardDifficulty || isExtremeDifficulty || isImpossibleDifficulty) {
                        e.setCancelled(true);
                        ItemStack clickedItem = e.getCurrentItem();

                        if (clickedItem != null) {
                            Material material = clickedItem.getType();

                            if (settingsManager.getDifficultyInt(e.getView().getTitle()) != 0) {
                                if (!materialList.getMaterials().get(settingsManager.getDifficultyInt(e.getView().getTitle())).contains(material)) {

                                    materialList.add(material, settingsManager.getDifficultyInt(e.getView().getTitle()));
                                    player.sendMessage(ChatColor.GREEN + "你将 " + ChatColor.GOLD
                                            + ultimateBingo.bingoFunctions.getMaterialName(material) + ChatColor.GREEN + " 添加到了宾果物品中！");
                                    createItemSettings(player, settingsManager.getDifficultyInt(e.getView().getTitle()));

                                    materialList.saveMaterialsToFile();

                                } else {
                                    player.sendMessage(ChatColor.RED + ultimateBingo.bingoFunctions.getMaterialName(material) + " 已存在于该难度中！");
                                }

                            } else {
                                player.sendMessage(ChatColor.RED + "发生错误，请重试。");
                            }
                        }
                    }
                }
            }
        }
    }

    public void createItemSettings(Player player, int difficulty){
        Inventory bingoItems = Bukkit.createInventory(player, 54, settingsManager.getDifficultyDisplay(difficulty));

        for (Material material : materialList.getMaterials().get(difficulty)){
            ItemStack item = new ItemBuilder(material).withLore(ChatColor.LIGHT_PURPLE + "左键点击移除").build();
            bingoItems.addItem(item);
        }
        if (bingoItems.getItem(53) != null && !sentWarning){
            player.sendMessage(ChatColor.RED + "你已达到 GUI 中可见物品数量上限！" +
                    "物品仍会被添加，但如果不移除其他物品，你将无法在设置界面看到它。" +
                    "未来可能会加入分页功能。此消息每次重启后只会发送一次。" );
            sentWarning = true;
        }

        player.openInventory(bingoItems);
    }
}
