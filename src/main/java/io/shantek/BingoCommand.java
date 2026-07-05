package io.shantek;

import io.shantek.managers.BingoManager;
import io.shantek.managers.InGameConfigManager;
import io.shantek.managers.PlayerStats;
import io.shantek.managers.SettingsManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class BingoCommand implements CommandExecutor {
    private final UltimateBingo ultimateBingo;
    private final SettingsManager settingsManager;
    private final BingoManager bingoManager;
    private final InGameConfigManager inGameConfigManager;
    String loadoutType = "空背包";

    private final Map<String, List<String>> settingOptions = Map.of(
            "GameMode", List.of("traditional", "speedrun", "brewdash", "group", "teams", "shuffle"),
            "Difficulty", List.of("easy", "normal", "hard"),
            "CardSize", List.of("small", "medium", "large"),
            "Loadout", List.of("裸装", "新手装备", "船只装备", "飞行装备", "弓箭装备"),
            "RevealCards", List.of("开启", "关闭"),
            "WinCondition", List.of("单行", "满卡"),
            "CardType", List.of("相同", "唯一"),
            "TimeLimit", List.of("0", "5", "10", "15", "30", "60")
    );

    public BingoCommand(UltimateBingo ultimateBingo, SettingsManager settingsManager, BingoManager bingoManager, InGameConfigManager inGameConfigManager) {
        this.ultimateBingo = ultimateBingo;
        this.settingsManager = settingsManager;
        this.bingoManager = bingoManager;
        this.inGameConfigManager = inGameConfigManager;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(ChatColor.RED + "该命令只能由玩家使用。");
            return true;
        }

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("stop") && player.hasPermission("shantek.ultimatebingo.stop")) {
                if (ultimateBingo.multiWorldServer && !player.getWorld().getName().equalsIgnoreCase(ultimateBingo.bingoWorld.toLowerCase())) {
                    player.sendMessage(ChatColor.RED + "该命令只能在宾果世界中执行。");
                } else {
                    stopBingo(player, false);
                }
                return true;
            } else if (args[0].equalsIgnoreCase("set") && player.isOp()) {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "用法：/bingo set <settingname|startbutton|hubspawn>");
                    return true;
                }

                // Handle hubspawn separately — no sign/button needed
                if (args[1].equalsIgnoreCase("hubspawn")) {
                    if (!ultimateBingo.hubConfig.exists()) {
                        player.sendMessage(ChatColor.RED + "hub.yml 不存在！请先创建它以启用大厅模式。");
                    } else if (ultimateBingo.hubWorld.isEmpty() || !player.getWorld().getName().equalsIgnoreCase(ultimateBingo.hubWorld)) {
                        player.sendMessage(ChatColor.RED + "你必须站在大厅世界中才能设置大厅出生点！");
                    } else {
                        ultimateBingo.hubConfig.saveHubSpawn(player.getLocation());
                        player.sendMessage(ChatColor.GREEN + "大厅出生点已保存成功！");
                    }
                    return true;
                }

                Block targetBlock = player.getTargetBlockExact(5);
                if (targetBlock == null || (!targetBlock.getType().name().contains("SIGN") && !targetBlock.getType().name().contains("BUTTON"))) {
                    player.sendMessage(ChatColor.RED + "你必须看着一个有效的告示牌或按钮！");
                    return true;
                }

                String settingName = args[1];
                Location targetLocation = targetBlock.getLocation();

                boolean validSign = true;
                for (Map.Entry<String, Location> entry : ultimateBingo.bingoFunctions.signLocations.entrySet()) {
                    if (targetBlock.getLocation().equals(entry.getValue())) {

                        player.sendMessage(ChatColor.YELLOW + "该告示牌已被用于 " + entry.getKey());
                        validSign = false;
                    }
                }
                if (validSign) {
                    if (settingOptions.containsKey(settingName)) {
                        ultimateBingo.inGameConfigManager.saveSignLocation(settingName, targetLocation);
                        player.sendMessage(ChatColor.GREEN + "" + settingName + " 的告示牌设置成功！");
                        ultimateBingo.inGameConfigManager.loadSignLocations();
                    } else if (settingName.equalsIgnoreCase("startbutton")) {
                        ultimateBingo.inGameConfigManager.saveButtonLocation(targetLocation);
                        player.sendMessage(ChatColor.GREEN + "开始按钮设置成功！");
                        ultimateBingo.inGameConfigManager.loadSignLocations();
                    } else if (settingName.equalsIgnoreCase("TeamRed") || settingName.equalsIgnoreCase("TeamBlue") || settingName.equalsIgnoreCase("TeamYellow")) {
                        String team = settingName.replace("Team", "").toLowerCase();
                        ultimateBingo.inGameConfigManager.saveTeamSignLocation(team, targetLocation);
                        ChatColor teamColor = switch (team) {
                            case "red" -> ChatColor.RED;
                            case "blue" -> ChatColor.BLUE;
                            case "yellow" -> ChatColor.YELLOW;
                            default -> ChatColor.WHITE;
                        };
                        String teamName = team.substring(0, 1).toUpperCase() + team.substring(1);
                        player.sendMessage(ChatColor.GREEN + "" + teamColor + teamName + ChatColor.GREEN + " 的告示牌设置成功！");
                    } else {
                        player.sendMessage(ChatColor.RED + "无效的设置名称。");
                    }
                }
                return true;

            } else if (args[0].equalsIgnoreCase("remove") && player.hasPermission("shantek.ultimatebingo.settings")) {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "用法：/bingo remove <signType|startbutton>");
                    return true;
                }

                String settingName = args[1];

                if (settingName.equalsIgnoreCase("startbutton")) {
                    if (ultimateBingo.bingoFunctions.startButtonLocation == null) {
                        player.sendMessage(ChatColor.RED + "开始按钮尚未设置。");
                    } else {
                        ultimateBingo.bingoFunctions.removeButton();
                        player.sendMessage(ChatColor.GREEN + "开始按钮已移除。");
                    }
                } else if (ultimateBingo.bingoFunctions.signLocations.containsKey(settingName)) {
                    ultimateBingo.bingoFunctions.removeSign(settingName);
                    player.sendMessage(ChatColor.GREEN + "" + settingName + " 的告示牌已移除。");
                } else if (settingName.equalsIgnoreCase("TeamRed") || settingName.equalsIgnoreCase("TeamBlue") || settingName.equalsIgnoreCase("TeamYellow")) {
                    String team = settingName.replace("Team", "").toLowerCase();
                    if (ultimateBingo.bingoFunctions.teamSignLocations.containsKey(team)) {
                        ultimateBingo.inGameConfigManager.removeTeamSign(team);
                        String teamName = team.substring(0, 1).toUpperCase() + team.substring(1);
                        player.sendMessage(ChatColor.GREEN + "" + teamName + " 的告示牌已移除。");
                    } else {
                        player.sendMessage(ChatColor.RED + "该颜色尚未设置队伍告示牌。");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "" + settingName + " 的告示牌尚未设置。");
                }
                return true;

            } else if (args[0].equalsIgnoreCase("reload") && player.hasPermission("shantek.ultimatebingo.settings")) {
                ultimateBingo.configFile.reloadConfigFile();
                ultimateBingo.hubConfig.load();
                ultimateBingo.reloadMapBackgrounds();
                player.sendMessage(ChatColor.GREEN + "宾果配置与地图背景已重载。");
                return true;
            } else if (args[0].equalsIgnoreCase("random")) {
                // Random teleport - available to all players
                ultimateBingo.bingoFunctions.teleportToRandomGround(player);
                return true;
            } else if (args[0].equalsIgnoreCase("leaderboard")) {
                if (args.length == 1) {
                    List<PlayerStats> topPlayersOverall = ultimateBingo.getLeaderboard().getTopPlayersOverall();
                    player.sendMessage(ChatColor.GREEN + "总排行榜：");
                    int rank = 1;
                    for (PlayerStats stats : topPlayersOverall) {
                        UUID playerUUID = stats.getPlayerUUID();
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
                        String playerName = offlinePlayer.getName() != null ? offlinePlayer.getName() : playerUUID.toString();
                        player.sendMessage(ChatColor.YELLOW + "#" + rank + ": " + playerName + " - " + stats.getTotalWins() + " 胜，" + stats.getTotalPlayed() + " 场");
                        rank++;
                        if (rank > 10) break;
                    }
                    if (rank == 1) player.sendMessage(ChatColor.YELLOW + "该分类下暂无玩家。");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("gui") && player.hasPermission("shantek.ultimatebingo.play")) {
                if (ultimateBingo.multiWorldServer && !player.getWorld().getName().equalsIgnoreCase(ultimateBingo.bingoWorld.toLowerCase())) {
                    player.sendMessage(ChatColor.RED + "该命令只能在宾果世界中执行。");
                } else {
                    if (ultimateBingo.bingoStarted) {
                        player.sendMessage(ChatColor.RED + "宾果游戏正在进行中。请结束游戏或使用 /bingo stop");
                    } else {
                        player.openInventory(ultimateBingo.bingoGameGUIManager.createGameGUI(player));
                    }
                }
                return true;
            } else if (args[0].equalsIgnoreCase("info")) {

                if (ultimateBingo.bingoStarted) {

                    // Work out the game time to display
                    String timeLimitString;
                    if (ultimateBingo.gameTime == 0) {
                        timeLimitString = "无时间限制";
                    } else {

                        // Calculate remaining time
                        long elapsedTime = System.currentTimeMillis() - ultimateBingo.gameStartTime;
                        long remainingTimeMillis = (long) ultimateBingo.gameTime * 60 * 1000 - elapsedTime;
                        long remainingMinutes = remainingTimeMillis / (60 * 1000);

                        // Work out how long is left and display it here if the game is active
                        timeLimitString = ultimateBingo.gameTime + " 分钟 (" + remainingMinutes + " 分钟）";

                    }

                    // This may be removed in the near future and implemented in to the bingo card?
                    player.sendMessage(ChatColor.WHITE + "宾果当前配置如下：");
                    if (ultimateBingo.currentDifficulty == null) {
                        player.sendMessage(ChatColor.GREEN + "难度：" + ChatColor.YELLOW + "未设置");
                    } else {
                        player.sendMessage(ChatColor.GREEN + "难度：" + ChatColor.YELLOW + ultimateBingo.currentDifficulty.toUpperCase());
                    }
                    player.sendMessage(ChatColor.GREEN + "卡片类型：" + ChatColor.YELLOW + ultimateBingo.currentCardSize.toUpperCase() + "/" + (ultimateBingo.currentUniqueCard ? "唯一" : "相同"));
                    player.sendMessage(ChatColor.GREEN + "游戏模式：" + ChatColor.YELLOW + ultimateBingo.currentGameMode.toUpperCase());
                    player.sendMessage(ChatColor.GREEN + "胜利条件：" + ChatColor.YELLOW + (ultimateBingo.currentFullCard ? "满卡" : "宾果"));
                    player.sendMessage(ChatColor.GREEN + "时间限制：" + ChatColor.YELLOW + (timeLimitString));
                } else {
                    player.sendMessage(ChatColor.YELLOW + "宾果当前未运行！");
                }

            } else if (args[0].equalsIgnoreCase("settings") && player.hasPermission("shantek.ultimatebingo.settings")) {
                ultimateBingo.getMaterialList().createMaterials();
                Inventory settingsGUI = settingsManager.createSettingsGUI(player);
                player.openInventory(settingsGUI);
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "你没有权限执行该操作！");
                return true;
            }
        } else {
            if (ultimateBingo.bingoStarted && ultimateBingo.bingoCardActive) {
                if (ultimateBingo.multiWorldServer && !player.getWorld().getName().equalsIgnoreCase(ultimateBingo.bingoWorld.toLowerCase())) {
                    player.sendMessage(ChatColor.RED + "该命令只能在宾果世界中执行。");
                } else {
                    if (ultimateBingo.currentGameMode.equalsIgnoreCase("teams")) {
                        // Teams mode: show team selection first, join happens on wool click
                        ultimateBingo.bingoFunctions.resetIndividualPlayer(player, true);
                        String team = ultimateBingo.bingoFunctions.getTeam(player);
                        if (team.equalsIgnoreCase("无") || team.isEmpty()) {
                            player.openInventory(ultimateBingo.bingoPlayerGUIManager.createTeamSelectionGUI(player));
                        } else {
                            player.openInventory(ultimateBingo.bingoPlayerGUIManager.createPlayerGUI(player));
                        }
                    } else if (ultimateBingo.currentGameMode.equalsIgnoreCase("group")) {
                        ultimateBingo.bingoFunctions.resetIndividualPlayer(player, true);
                        ultimateBingo.bingoManager.joinGameInProgress(player);
                        player.openInventory(ultimateBingo.bingoPlayerGUIManager.createPlayerGUI(player));
                    } else if (!ultimateBingo.bingoManager.checkHasBingoCard(player)) {
                        ultimateBingo.bingoFunctions.resetIndividualPlayer(player, true);
                        ultimateBingo.bingoManager.joinGameInProgress(player);
                        player.openInventory(ultimateBingo.bingoPlayerGUIManager.createPlayerGUI(player));
                    } else {
                        player.openInventory(ultimateBingo.bingoPlayerGUIManager.createPlayerGUI(player));
                    }
                }
            } else if (!ultimateBingo.bingoStarted) {
                // In teams mode, allow team selection even before game starts
                // Use gameMode (config value) not currentGameMode (set only on game start)
                if (ultimateBingo.gameMode.equalsIgnoreCase("teams")) {
                    player.openInventory(ultimateBingo.bingoPlayerGUIManager.createTeamSelectionGUI(player));
                } else {
                    player.sendMessage(ChatColor.RED + "宾果尚未开始！");
                }
            }
        }
        return false;
    }

//region Start and stop the game

    public void startBingo(Player commandPlayer) {
        UltimateBingo plugin = UltimateBingo.getInstance();

        if (ultimateBingo.bingoStarted) {

            commandPlayer.closeInventory();
            commandPlayer.sendMessage(ChatColor.RED + "宾果已经在运行中！");

        } else {

            // Clear the player list
            ultimateBingo.bingoFunctions.clearPlayers();

            // Clear any data prior to the new game
            bingoManager.clearData();

            // Set the time that the game started
            ultimateBingo.gameStartTime = System.currentTimeMillis();

            // Clean up and reset game environment
            ultimateBingo.bingoFunctions.despawnAllItems();
            ultimateBingo.bingoStarted = true;
            ultimateBingo.bingoFunctions.resetPlayers();
            ultimateBingo.bingoFunctions.resetTimeAndWeather();

            // Enable keepInventory in the bingo world
            commandPlayer.getWorld().setGameRule(GameRule.KEEP_INVENTORY, true);

            // Clear all player advancements for a fresh start
            ultimateBingo.bingoFunctions.clearAllAdvancements();

            // Configure game based on card size
            String cardSize = ultimateBingo.currentCardSize;
            switch (cardSize) {
                case "small":
                    ultimateBingo.bingoManager.slots = new int[]{10, 11, 12, 19, 20, 21, 28, 29, 30};
                    ultimateBingo.bingoManager.setBingoCards(9);
                    break;
                case "medium":
                    ultimateBingo.bingoManager.slots = new int[]{10, 11, 12, 13, 19, 20, 21, 22, 28, 29, 30, 31, 37, 38, 39, 40};
                    ultimateBingo.bingoManager.setBingoCards(16);
                    break;
                case "large":
                    ultimateBingo.bingoManager.slots = new int[]{10, 11, 12, 13, 14, 19, 20, 21, 22, 23, 28, 29, 30, 31, 32, 37, 38, 39, 40, 41, 46, 47, 48, 49, 50};
                    ultimateBingo.bingoManager.setBingoCards(25);
                    break;
            }
            ultimateBingo.getMaterialList().createMaterials();

            if (ultimateBingo.currentGameMode.equalsIgnoreCase("group")) {
                ultimateBingo.bingoManager.createGroupBingoCard();
            } else if (ultimateBingo.currentGameMode.equalsIgnoreCase("teams")) {
                ultimateBingo.bingoManager.createTeamBingoCards();
                ultimateBingo.bingoFunctions.assignTeams();
            } else if (ultimateBingo.currentUniqueCard) {
                ultimateBingo.bingoManager.createUniqueBingoCards();
            } else {
                ultimateBingo.bingoManager.createBingoCards();
            }

            // Set game strings for countdown
            String cardType;
            if (ultimateBingo.currentGameMode.equalsIgnoreCase("group")) {
                // Always print shared for a group game
                cardType = "共享";
            } else {
                cardType = ultimateBingo.currentUniqueCard ? "唯一" : "相同";
            }
            String bingoType = ultimateBingo.currentFullCard ? "满卡" : "单行";
            String revealType = ultimateBingo.currentRevealCards ? "ENABLED" : "DISABLED";

            if (ultimateBingo.currentLoadoutType == 0) {
                loadoutType = "裸装";
            } else if (ultimateBingo.currentLoadoutType == 1) {
                loadoutType = "新手装备";
            } else if (ultimateBingo.currentLoadoutType == 2) {
                loadoutType = "船只装备";
            } else if (ultimateBingo.currentLoadoutType == 3) {
                loadoutType = "飞行装备";
            } else if (ultimateBingo.currentLoadoutType == 4) {
                loadoutType = "弓箭装备";
            }

            // Store a reference to all online players
            List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());

            // Display initial messages — stagger players to avoid concurrent chunk-gen
            for (int pi = 0; pi < onlinePlayers.size(); pi++) {
                Player player = onlinePlayers.get(pi);
                final int staggerTicks = pi * 10; // each player delayed 0.5s more

                boolean activePlayer = true;

                // Check if multi world bingo is enabled and they're in the bingo world
                if (ultimateBingo.multiWorldServer && !player.getWorld().getName().equalsIgnoreCase(ultimateBingo.bingoWorld.toLowerCase())) {
                    activePlayer = false;

                }

                if (activePlayer) {

                    // Freeze players - use both walk speed and strong slowness + jump boost removal
                    player.setWalkSpeed(0);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 400, 255, false, false, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 400, 128, false, false, false));

                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        player.sendTitle(ChatColor.YELLOW + cardType, ChatColor.WHITE + ultimateBingo.currentCardSize.toUpperCase() + ", " + ultimateBingo.currentDifficulty.toUpperCase(), 10, 40, 10);
                    }, 20 + staggerTicks);

                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        player.sendTitle(ChatColor.YELLOW + bingoType, ChatColor.WHITE + "公开模式 " + revealType, 10, 40, 10);
                    }, 80 + staggerTicks);

                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        player.sendTitle(ChatColor.YELLOW + ultimateBingo.currentGameMode.toUpperCase(), ChatColor.WHITE + loadoutType.toUpperCase(), 10, 40, 10);
                    }, 140 + staggerTicks);

                    // Countdown with chimes, with bold and colorful text
                    for (int i = 3; i > 0; i--) {
                        final int count = i;


                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                            player.sendTitle(ChatColor.GREEN + "" + ChatColor.BOLD + String.valueOf(count), "", 10, 20, 10);
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);
                            // Teleport to random ground when "3" shows so players are in position by GO
                            if (count == 3) {
                                ultimateBingo.bingoFunctions.teleportToRandomGround(player);
                                ultimateBingo.bingoScoreboardManager.showBoard(player);
                            }
                        }, 200 + staggerTicks + 30 * (3 - count)); // Countdown starts at 5 seconds

                    }
                    // Final "开始！" message and chime, bold and green
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {

                        if (ultimateBingo.currentGameMode.equalsIgnoreCase("teams") && ultimateBingo.bingoFunctions.getTeam(player).equalsIgnoreCase("yellow")) {
                            player.sendTitle(ChatColor.YELLOW + "" + ChatColor.BOLD + "黄队冲！", "", 10, 20, 10);
                        } else if (ultimateBingo.currentGameMode.equalsIgnoreCase("teams") && ultimateBingo.bingoFunctions.getTeam(player).equalsIgnoreCase("red")) {
                            player.sendTitle(ChatColor.RED + "" + ChatColor.BOLD + "红队冲！", "", 10, 20, 10);
                        } else if (ultimateBingo.currentGameMode.equalsIgnoreCase("teams") && ultimateBingo.bingoFunctions.getTeam(player).equalsIgnoreCase("blue")) {
                            player.sendTitle(ChatColor.BLUE + "" + ChatColor.BOLD + "蓝队冲！", "", 10, 20, 10);
                        } else {
                            player.sendTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "开始！", "", 10, 20, 10);
                        }

                        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);

                        // Unfreeze players
                        player.removePotionEffect(PotionEffectType.SLOW);
                        player.removePotionEffect(PotionEffectType.JUMP);
                        player.setWalkSpeed(0.2f); // Default walk speed

                        if (ultimateBingo.currentGameMode.equalsIgnoreCase("teams")) {
                            ultimateBingo.bingoFunctions.notifyActivePlayers(player);
                        }
                    }, 290 + staggerTicks); // 1.5 seconds after "1"

                }
            }

            // Set the game timer for ending the game and game perks if enabled
            ultimateBingo.bingoFunctions.setGameTimer();

            // Delayed broadcast with the win condition
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {

                String timeLimitString;
                if (ultimateBingo.gameTime == 0) {
                    timeLimitString = "时间限制：无限制";
                } else {
                    timeLimitString = "时间限制：" + ultimateBingo.gameTime + " 分钟";
                }

                if (ultimateBingo.currentGameMode.equalsIgnoreCase("traditional")) {

                    ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GREEN + "传统宾果 - 收集物品并在卡片上勾选！");

                    if (ultimateBingo.currentFullCard) {
                        ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GREEN + "集满整张卡即可获胜！" + timeLimitString);
                    } else {
                        ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GREEN + "连成一行即可获胜！" + timeLimitString);
                    }
                } else if (ultimateBingo.currentGameMode.equalsIgnoreCase("speedrun")) {

                    ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GREEN + "速跑模式 - 每勾选一项都会重置饥饿值与生命值！");

                    if (ultimateBingo.currentFullCard) {
                        ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GREEN + "集满整张卡即可获胜！" + timeLimitString);
                    } else {
                        ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GREEN + "连成一行即可获胜！" + timeLimitString);
                    }
                } else if (ultimateBingo.currentGameMode.equalsIgnoreCase("group")) {

                    ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GREEN + "团队模式 - 齐心协力完成宾果！");

                    if (ultimateBingo.currentFullCard) {
                        ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GREEN + "集满整张卡即可获胜！" + timeLimitString);
                    } else {
                        ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GREEN + "连成一行即可获胜！" + timeLimitString);
                    }
                } else if (ultimateBingo.currentGameMode.equalsIgnoreCase("teams")) {

                    ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GREEN + "队伍模式 - 齐心协力完成宾果！");

                    if (ultimateBingo.currentFullCard) {
                        ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GREEN + "集满整张卡即可获胜！" + timeLimitString);
                    } else {
                        ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GREEN + "连成一行即可获胜！" + timeLimitString);
                    }
                } else if (ultimateBingo.currentGameMode.equalsIgnoreCase("brewdash")) {

                    ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GREEN + "药水冲刺 - 每勾选一项都会对其他玩家施放随机药水！");

                    if (ultimateBingo.currentFullCard) {
                        ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GREEN + "集满整张卡即可获胜！" + timeLimitString);
                    } else {
                        ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GREEN + "连成一行即可获胜！" + timeLimitString);
                    }
                } else if (ultimateBingo.currentGameMode.equalsIgnoreCase("shuffle")) {

                    ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GREEN + "洗牌模式 - 每 " + ultimateBingo.shuffleIntervalMinutes + " 分钟洗牌一次！");

                    if (ultimateBingo.currentFullCard) {
                        ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GREEN + "集满整张卡即可获胜！" + timeLimitString);
                    } else {
                        ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GREEN + "连成一行即可获胜！" + timeLimitString);
                    }

                    // Start the shuffle scheduler
                    ultimateBingo.bingoManager.startShuffleMode();
                }

            }, 350);

            // Game still active? If so, let's start it
            ultimateBingo.playedSinceReboot = true;

            // Players are randomly teleported later in the delayed card-giving task,
            // so no need to scatter here (avoids redundant chunk loading)


            // Handle player teleportation and give bingo cards after the countdown
            for (int pi2 = 0; pi2 < onlinePlayers.size(); pi2++) {
                Player p2 = onlinePlayers.get(pi2);
                final int sTicks = pi2 * 10;

                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {

                    if (ultimateBingo.bingoFunctions.isActivePlayer(p2)) {
                        ultimateBingo.bingoFunctions.giveBingoCard(p2);
                        ultimateBingo.bingoCardActive = true;

                        // Equip the player loadout inventory
                        if (ultimateBingo.currentLoadoutType > 0) {
                            ultimateBingo.bingoFunctions.equipLoadoutGear(p2, ultimateBingo.currentLoadoutType);
                        }

                        // Also give them night vision
                        if (ultimateBingo.currentGameMode.equals("speedrun") || ultimateBingo.currentGameMode.equals("group") || ultimateBingo.currentGameMode.equalsIgnoreCase("teams")) {
                            p2.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false, true));
                        }

                        // Add them to the player list
                        ultimateBingo.bingoFunctions.addPlayer(p2.getUniqueId());
                    }
                }, 310 + sTicks);

            }

        }
    }

    public void bingoGameOver() {
        // Timer expired - treat as a completed game (no winner)
        // Use a dummy player reference since this is triggered by the timer
        // We pass gameCompleted=true so players get the game duration message
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (ultimateBingo.bingoFunctions.isActivePlayer(player)) {
                stopBingo(player, true);
                return;
            }
        }
        // Fallback if no active players found, still clean up
        ultimateBingo.bingoStarted = false;
        ultimateBingo.bingoCardActive = false;
        ultimateBingo.bingoButtonActive = true;
        Bukkit.getScheduler().cancelTasks(ultimateBingo);
    }

    public void stopBingo(Player sender, boolean gameCompleted) {

        if (!ultimateBingo.bingoStarted && !gameCompleted) {
            sender.sendMessage(ChatColor.RED + "宾果尚未开始！使用 /bingo start 开始");
            return;
        }

        // Cancel any tasks that are currently scheduled
        Bukkit.getScheduler().cancelTasks(ultimateBingo);

        // Stop shuffle mode if active
        ultimateBingo.bingoManager.stopShuffleMode();

        // Hide all scoreboards
        ultimateBingo.bingoScoreboardManager.hideAllBoards();

        ultimateBingo.bingoCardActive = false;
        ultimateBingo.bingoStarted = false;

        if (!gameCompleted) {
            sender.sendMessage(ChatColor.RED + "宾果已停止！");
        } else {
            // Show how long the game ran for
            Bukkit.getScheduler().runTaskLater(ultimateBingo, () -> {
                long duration = System.currentTimeMillis() - ultimateBingo.gameStartTime;
                String gameDuration = ultimateBingo.bingoFunctions.formatAndShowGameDuration(duration);
                ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GREEN + "游戏时长：" + gameDuration);
            }, 80L);
        }

        // Unfreeze all players (run even if stopped mid-countdown)
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        onlinePlayers.forEach(player -> {
            player.removePotionEffect(PotionEffectType.SLOW);
            player.removePotionEffect(PotionEffectType.JUMP);
            player.setWalkSpeed(0.2f);
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        });

        // Get all online players for reset/teleport
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());

        // If hub mode is active, teleport players back to hub spawn
        if (ultimateBingo.isHubModeActive() && ultimateBingo.hubSpawnLocation != null) {
            List<Player> activeBingoPlayers = new ArrayList<>();
            for (Player p : players) {
                if (ultimateBingo.bingoFunctions.isActivePlayer(p)) {
                    activeBingoPlayers.add(p);
                    p.teleport(ultimateBingo.hubSpawnLocation);
                }
            }

            ultimateBingo.bingoFunctions.despawnAllItems();

            Bukkit.getScheduler().runTaskLater(ultimateBingo, () -> {
                for (Player p : activeBingoPlayers) {
                    if (p.isOnline()) {
                        ultimateBingo.bingoFunctions.giveBingoCard(p);
                        ultimateBingo.hubRegionListener.markPlayerInRegion(p.getUniqueId());
                    }
                }
                ultimateBingo.bingoSpawnLocation = null;
                ultimateBingo.bingoButtonActive = true;
            }, 40L);

        } else {
            ultimateBingo.bingoFunctions.safeScatterPlayers(players, ultimateBingo.bingoSpawnLocation, 5);

            Bukkit.getScheduler().runTaskLater(ultimateBingo, () -> {
                ultimateBingo.bingoFunctions.resetPlayers();
                ultimateBingo.bingoFunctions.despawnAllItems();

                Bukkit.getScheduler().runTaskLater(ultimateBingo, () -> {
                    if (ultimateBingo.currentGameMode.equalsIgnoreCase("group") || ultimateBingo.currentGameMode.equalsIgnoreCase("teams")) {
                        ultimateBingo.bingoFunctions.giveBingoCardToAllPlayers();
                    } else if (!bingoManager.getBingoGUIs().isEmpty()) {
                        ultimateBingo.bingoFunctions.giveBingoCardToAllPlayers();
                    }

                    ultimateBingo.bingoSpawnLocation = null;
                    ultimateBingo.bingoButtonActive = true;
                }, 40L);

            }, 40L);
        }
    }

    //endregion

    //region Opening bingo cards

    public void openBingo(Player sender) {
        if (ultimateBingo.currentGameMode.equalsIgnoreCase("group") && ultimateBingo.groupInventory != null) {
            sender.openInventory(ultimateBingo.groupInventory);

        } else if (ultimateBingo.currentGameMode.equalsIgnoreCase("teams")) {

            Inventory teamInventory = ultimateBingo.bingoFunctions.getTeamInventory(sender);

            if (teamInventory != null) {
                sender.openInventory(teamInventory);
            } else {
                sender.sendMessage(ChatColor.RED + "未找到队伍背包。你是否在一场进行中的游戏中？");

            }


        } else if (bingoManager.getBingoGUIs() != null && !bingoManager.getBingoGUIs().isEmpty()) {
            if (bingoManager.getBingoGUIs().containsKey(sender.getUniqueId())) {
                sender.openInventory(bingoManager.getBingoGUIs().get(sender.getUniqueId()));
            } else {
                if (sender.hasPermission("shantek.ultimatebingo.start")) {
                    sender.sendMessage(ChatColor.RED + "你错过了加入宾果的机会！想创建新游戏请使用 /bingo start");
                }

                if (!sender.hasPermission("shantek.ultimatebingo.start")) {
                    sender.sendMessage(ChatColor.RED + "你错过了加入宾果的机会！");
                }
            }
        } else {
            if (sender.hasPermission("shantek.ultimatebingo.start")) {
                sender.sendMessage(ChatColor.RED + "宾果尚未开始！使用 /bingo start 开始");
            }

            if (!sender.hasPermission("shantek.ultimatebingo.start")) {
                sender.sendMessage(ChatColor.RED + "宾果尚未开始！");
            }

        }
    }



    public void openBingoOtherPlayer(Player sender, Player otherPlayer) {
        // Playing the reveal mode, all good to allow this functionality
        if (ultimateBingo.bingoManager.getBingoGUIs() != null && !ultimateBingo.bingoManager.getBingoGUIs().isEmpty()) {
            if (ultimateBingo.bingoManager.getBingoGUIs().containsKey(otherPlayer.getUniqueId())) {
                sender.openInventory(ultimateBingo.bingoManager.getBingoGUIs().get(otherPlayer.getUniqueId()));
            } else {
                // Couldn't find that players name in the list
                sender.sendMessage(ChatColor.RED + "未找到 " + otherPlayer.getName());

            }
        } else {

            sender.sendMessage(ChatColor.RED + "宾果尚未开始！");


        }

    }

    public void openBingoTeamCard(Player sender, Inventory inventory) {

        if (inventory == null) {
            sender.sendMessage(ChatColor.RED + "未找到队伍背包。你是否在一场进行中的游戏中？");
        } else {
            // Close their existing inventory
            sender.closeInventory();

            // Open the desired team inventory
            sender.openInventory(inventory);
        }
    }

    //endregion
}