package io.shantek.managers;

import io.shantek.UltimateBingo;
import io.shantek.tools.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class BingoGameGUIManager {
    UltimateBingo ultimateBingo;

    private Map<String, String[]> optionsMap;

    public BingoGameGUIManager(UltimateBingo ultimateBingo) {
        this.ultimateBingo = ultimateBingo;

        optionsMap = new HashMap<>();
        optionsMap.put("difficulty", new String[]{"easy", "normal", "hard"});
        optionsMap.put("cardSize", new String[]{"small", "medium", "large"});
        optionsMap.put("gameMode", new String[]{"speedrun", "traditional", "brewdash", "group", "teams", "shuffle"});
        optionsMap.put("uniqueCard", new String[]{"unique", "identical"});
        optionsMap.put("fullCard", new String[]{"full card", "single row"});
        optionsMap.put("revealCards", new String[]{"enabled", "disabled"});
    }


    public Inventory createGameGUI(Player player) {
        Inventory gameConfigInventory = Bukkit.createInventory(player, 9, ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "宾果配置");
        gameConfigInventory.setItem(0, createItem(setGUIIcon("gamemode"), "游戏模式", ultimateBingo.gameMode));
        gameConfigInventory.setItem(1, createItem(setGUIIcon("difficulty"), "难度", ultimateBingo.difficulty));
        gameConfigInventory.setItem(2, createItem(setGUIIcon("cardsize"), "卡片大小", ultimateBingo.cardSize));
        if (!ultimateBingo.gameMode.equalsIgnoreCase("group")) {
            gameConfigInventory.setItem(3, createItem(setGUIIcon("uniqueCard"), "卡片类型", ultimateBingo.uniqueCard.toUpperCase()));
        } else {
            gameConfigInventory.setItem(3,null);
        }
        gameConfigInventory.setItem(4, createItem(setGUIIcon("wincondition"), "胜利条件", ultimateBingo.fullCard.toUpperCase()));
        gameConfigInventory.setItem(5, createItem(setGUIIcon("reveal"), "公开卡片", ultimateBingo.revealCards.toUpperCase()));

        // Work out the game time to display
        String gameTimeString;
        if (ultimateBingo.gameTime == 0) {
            gameTimeString = "无时间限制";
        } else {
            gameTimeString = ultimateBingo.gameTime + " 分钟";
        }
        gameConfigInventory.setItem(6, createItem(Material.CLOCK, "时间限制", gameTimeString));

        // Work out the game loadout to give the player
        String gameLoadoutString = "裸装";
        if (ultimateBingo.loadoutType == 1) {
            gameLoadoutString = "基础装备";
        } else if (ultimateBingo.loadoutType == 2) {
            gameLoadoutString = "船只装备";
        } else if (ultimateBingo.loadoutType == 3) {
            gameLoadoutString = "飞行装备";
        } else if (ultimateBingo.loadoutType == 4) {
            gameLoadoutString = "弓箭装备";
        } else if (ultimateBingo.loadoutType == 50) {
            gameLoadoutString = "随机装备";
        }
        gameConfigInventory.setItem(7, createItem(setGUIIcon("loadout"), "玩家装备", gameLoadoutString));
        gameConfigInventory.setItem(8, createStartGameItem());

        return gameConfigInventory;
    }

    private ItemStack createItem(Material material, String prefix, String currentValue) {
        String displayValue = translateValueForDisplay(prefix, currentValue);
        return new ItemBuilder(material)
                .withDisplayName(ChatColor.BLUE + prefix + "：" + displayValue.toUpperCase())
                .withLore(ChatColor.GRAY + "点击切换 " + prefix.toLowerCase()).build();
    }

    private String translateValueForDisplay(String prefix, String currentValue) {
        String value = currentValue.toLowerCase();
        return switch (prefix) {
            case "游戏模式" -> switch (value) {
                case "traditional" -> "传统";
                case "speedrun" -> "速跑";
                case "brewdash" -> "药水冲刺";
                case "group" -> "团队";
                case "teams" -> "队伍";
                case "shuffle" -> "洗牌";
                case "random" -> "随机";
                default -> currentValue;
            };
            case "难度" -> switch (value) {
                case "easy" -> "简单";
                case "normal" -> "普通";
                case "hard" -> "困难";
                case "random" -> "随机";
                default -> currentValue;
            };
            case "卡片大小" -> switch (value) {
                case "small" -> "小";
                case "medium" -> "中";
                case "large" -> "大";
                case "random" -> "随机";
                default -> currentValue;
            };
            case "卡片类型" -> switch (value) {
                case "unique" -> "唯一";
                case "identical" -> "相同";
                case "random" -> "随机";
                default -> currentValue;
            };
            case "胜利条件" -> switch (value) {
                case "full card" -> "满卡";
                case "single row" -> "单行";
                case "random" -> "随机";
                default -> currentValue;
            };
            case "公开卡片" -> switch (value) {
                case "enabled" -> "开启";
                case "disabled" -> "关闭";
                case "random" -> "随机";
                default -> currentValue;
            };
            default -> currentValue;
        };
    }

    private ItemStack createStartGameItem() {
        return new ItemBuilder(Material.ENDER_PEARL)
                .withDisplayName(ChatColor.GREEN + "开始游戏")
                .withLore(ChatColor.GRAY + "点击开始游戏").build();
    }

    //region Toggle and update the GUI

    public void toggleGameMode(Player player) {
        ultimateBingo.bingoFunctions.toggleGameMode();
        updateGUI(player);
    }

    public void toggleDifficulty(Player player) {
        ultimateBingo.bingoFunctions.toggleDifficulty();
        updateGUI(player);
    }

    public void toggleCardSize(Player player) {
        ultimateBingo.bingoFunctions.toggleCardSize();
        updateGUI(player);
    }

    public void toggleGameTime(Player player) {
        ultimateBingo.bingoFunctions.toggleTimeLimit();
        updateGUI(player);
    }

    public void toggleLoadout(Player player) {
        ultimateBingo.bingoFunctions.toggleLoadout();
        updateGUI(player);
    }

    public void toggleCardType(Player player) {
        ultimateBingo.bingoFunctions.toggleUnique();
        updateGUI(player);
    }

    public void toggleWinCondition(Player player) {
        ultimateBingo.bingoFunctions.toggleFullCard();
        updateGUI(player);
    }

    public void toggleRevealCards(Player player) {
        ultimateBingo.bingoFunctions.toggleReveal();
        updateGUI(player);
    }

    private void updateGUI(Player player) {
        Inventory currentInventory = player.getOpenInventory().getTopInventory();
        ItemStack startGameItem = currentInventory.getItem(8);  // This is the "开始游戏" item in slot 8

        if (startGameItem != null && startGameItem.hasItemMeta() && startGameItem.getItemMeta().hasDisplayName() &&
                ChatColor.stripColor(startGameItem.getItemMeta().getDisplayName()).equals("开始游戏")) {
            // The inventory is confirmed to be the Bingo Configuration GUI
            // Update existing inventory directly
            currentInventory.setItem(0, createItem(setGUIIcon("gamemode"), "游戏模式", ultimateBingo.gameMode));
            currentInventory.setItem(1, createItem(setGUIIcon("difficulty"), "难度", ultimateBingo.difficulty));
            currentInventory.setItem(2, createItem(setGUIIcon("cardsize"), "卡片大小", ultimateBingo.cardSize));
            if (!ultimateBingo.gameMode.equalsIgnoreCase("group")) {
                currentInventory.setItem(3, createItem(setGUIIcon("uniqueCard"), "卡片类型", ultimateBingo.uniqueCard.toUpperCase()));
            } else {
                currentInventory.setItem(3,null);
            }
            currentInventory.setItem(4, createItem(setGUIIcon("wincondition"), "胜利条件", ultimateBingo.fullCard.toUpperCase()));
            currentInventory.setItem(5, createItem(setGUIIcon("reveal"), "公开卡片", ultimateBingo.revealCards.toUpperCase()));

            // Work out the game time to display
            String gameTimeString;
            if (ultimateBingo.gameTime == 0) {
                gameTimeString = "无时间限制";
            } else {
                gameTimeString = ultimateBingo.gameTime + " 分钟";
            }
            currentInventory.setItem(6, createItem(Material.CLOCK, "时间限制", gameTimeString));

            // Work out the game loadout to give the player
            String gameLoadoutString = "裸装";
            if (ultimateBingo.loadoutType == 1) {
                gameLoadoutString = "基础装备";
            } else if (ultimateBingo.loadoutType == 2) {
                gameLoadoutString = "船只装备";
            } else if (ultimateBingo.loadoutType == 3) {
                gameLoadoutString = "飞行装备";
            } else if (ultimateBingo.loadoutType == 4) {
                gameLoadoutString = "弓箭装备";
            } else if (ultimateBingo.loadoutType == 50) {
                gameLoadoutString = "随机装备";
            }


            currentInventory.setItem(7, createItem(setGUIIcon("loadout"), "玩家装备", gameLoadoutString));

            // Update all the game config signs if they exist
            ultimateBingo.bingoFunctions.updateAllSigns();

        } else {
            // If not viewing the Bingo configuration, open it
            player.openInventory(createGameGUI(player));
        }
    }

    //endregion

    public void setGameConfiguration() {

        ultimateBingo.currentLoadoutType = ultimateBingo.bingoFunctions.validateOrDefaultInt(ultimateBingo.loadoutType, 5, 0);
        ultimateBingo.currentDifficulty = ultimateBingo.bingoFunctions.validateOrDefault(ultimateBingo.difficulty, optionsMap.get("difficulty"), "normal");
        ultimateBingo.currentCardSize = ultimateBingo.bingoFunctions.validateOrDefault(ultimateBingo.cardSize, optionsMap.get("cardSize"), "medium");
        ultimateBingo.currentGameMode = ultimateBingo.bingoFunctions.validateOrDefault(ultimateBingo.gameMode, optionsMap.get("gameMode"), "traditional");
        ultimateBingo.currentUniqueCard = ultimateBingo.bingoFunctions.validateOrDefaultBoolean(ultimateBingo.uniqueCard, optionsMap.get("uniqueCard"), true);
        ultimateBingo.currentFullCard = ultimateBingo.bingoFunctions.validateOrDefaultBoolean(ultimateBingo.fullCard, optionsMap.get("fullCard"), true);
        ultimateBingo.currentRevealCards = ultimateBingo.bingoFunctions.validateOrDefaultBoolean(ultimateBingo.revealCards, optionsMap.get("revealCards"), true);

    }

    private Material setGUIIcon(String type) {

        Material materialToDisplay = Material.AIR;

        if (type.equalsIgnoreCase("loadout")) {
            return switch (ultimateBingo.loadoutType) {
                case 1 -> materialToDisplay = Material.WOODEN_PICKAXE; // Starter kit
                case 2 -> materialToDisplay = Material.OAK_BOAT; // Boat kit
                case 3 -> materialToDisplay = Material.FIREWORK_ROCKET; // Rocket kit
                case 4 -> materialToDisplay = Material.BOW; // Rocket kit
                case 50 -> materialToDisplay = Material.SHULKER_BOX; // Random kit
                default -> materialToDisplay = Material.CRAFTING_TABLE; // Naked kit
            };

        } else if (type.equalsIgnoreCase("difficulty")) {
            return switch (ultimateBingo.difficulty) {
                case "easy" -> materialToDisplay = Material.COPPER_INGOT;
                case "hard" -> materialToDisplay = Material.NETHERITE_INGOT;
                case "random" -> materialToDisplay = Material.DIAMOND;
                default -> materialToDisplay = Material.IRON_INGOT;

            };
        } else if (type.equalsIgnoreCase("cardsize")) {
            return switch (ultimateBingo.cardSize) {
                case "small" -> materialToDisplay = Material.PAPER;
                case "medium" -> materialToDisplay = Material.BOOK;
                case "random" -> materialToDisplay = Material.SUGAR_CANE;
                default -> materialToDisplay = Material.WRITABLE_BOOK;

            };

        } else if (type.equalsIgnoreCase("gamemode")) {
            if (ultimateBingo.gameMode.equalsIgnoreCase("speedrun")) {
                materialToDisplay = Material.DIAMOND_BOOTS;
            } else if (ultimateBingo.gameMode.equalsIgnoreCase("group")) {
                materialToDisplay = Material.ENDER_CHEST;
            } else if (ultimateBingo.gameMode.equalsIgnoreCase("teams")) {
                materialToDisplay = Material.RED_WOOL;
            } else if (ultimateBingo.gameMode.equalsIgnoreCase("brewdash")) {
                materialToDisplay = Material.POTION;
            } else if (ultimateBingo.gameMode.equalsIgnoreCase("shuffle")) {
                materialToDisplay = Material.DROPPER;
            } else if (ultimateBingo.gameMode.equalsIgnoreCase("random")) {
                materialToDisplay = Material.LADDER;
            } else {
                materialToDisplay = Material.FURNACE;
            }
        } else if (type.equalsIgnoreCase("uniquecard")) {
            if (ultimateBingo.uniqueCard.equalsIgnoreCase("unique")) {
                materialToDisplay = Material.FILLED_MAP;
            } else if (ultimateBingo.uniqueCard.equalsIgnoreCase("random")) {
                materialToDisplay = Material.SUGAR;
            } else {
                materialToDisplay = Material.MAP;
            }
        } else if (type.equalsIgnoreCase("wincondition")) {
            if (ultimateBingo.fullCard.equalsIgnoreCase("full card")) {
                materialToDisplay = ultimateBingo.tickedItemMaterial;
            } else if (ultimateBingo.fullCard.equalsIgnoreCase("random")) {
                materialToDisplay = Material.BEACON;
            } else {
                materialToDisplay = Material.BLAZE_ROD;
            }
        } else if (type.equalsIgnoreCase("reveal")) {
            if (ultimateBingo.revealCards.equalsIgnoreCase("enabled")) {
                materialToDisplay = Material.SPYGLASS;
            } else if (ultimateBingo.revealCards.equalsIgnoreCase("random")) {
                materialToDisplay = Material.MINECART;
            } else {
                materialToDisplay = Material.BLACK_CONCRETE;
            }
        }


        return materialToDisplay;


    }
}