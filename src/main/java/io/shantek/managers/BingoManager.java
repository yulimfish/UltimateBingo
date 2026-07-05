package io.shantek.managers;

import io.shantek.BingoCommand;
import io.shantek.UltimateBingo;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class BingoManager {

    Map<UUID, List<ItemStack>> playerBingoCards;
    Map<UUID, Inventory> bingoGUIs;
    Map<UUID, Inventory> previousBingoGUIs;
    private int bingoCards;
    private UltimateBingo ultimateBingo;
    public int[] slots;
    public boolean started;
    private BingoCommand bingoCommand;

    public BingoManager(UltimateBingo ultimateBingo, BingoCommand bingoCommand) {
        this.ultimateBingo = ultimateBingo;
        this.bingoCommand = bingoCommand;
    }

    // Method to set the BingoCommand later
    public void setBingoCommand(BingoCommand bingoCommand) {
        this.bingoCommand = bingoCommand;
    }

    public void createBingoCards() {
        started = true;
        playerBingoCards = new HashMap<>();
        bingoGUIs = new HashMap<>();

        // Determine difficulty level and set TOTAL_ITEMS based on it
        int difficultyLevel;
        switch (ultimateBingo.currentDifficulty.toLowerCase()) {
            case "normal":
                difficultyLevel = 2;
                TOTAL_ITEMS = 21;
                break;
            case "hard":
                difficultyLevel = 3;
                TOTAL_ITEMS = 30;
                break;
            default:
                difficultyLevel = 1; // Default to "easy"
                TOTAL_ITEMS = 14;
                break;
        }

        // Generate and shuffle materials for the card
        List<Material> availableMaterials = generateMaterials(difficultyLevel);
        Collections.shuffle(availableMaterials);

        // Get slots based on the card size
        int[] slots = determineSlotsBasedOnCardSize();

        // Distribute unique cards to each player
        for (Player player : Bukkit.getOnlinePlayers()) {

            if (ultimateBingo.bingoFunctions.isActivePlayer(player)) {
                UUID playerId = player.getUniqueId();

                // Store the string for the card type
                String cardInfo = ultimateBingo.currentUniqueCard ? "唯一" : "相同";
                cardInfo += ultimateBingo.currentFullCard ? "/满卡" : "/单行";
                cardInfo = "(" + cardInfo + ")";

                // Create a new inventory for each player
                Inventory bingoGUI = Bukkit.createInventory(null, 54, ChatColor.GREEN.toString() + ChatColor.BOLD + "宾果" + " " + ChatColor.LIGHT_PURPLE + cardInfo);

                // Populate the card inventory with selected materials
                for (int i = 0; i < slots.length && i < availableMaterials.size(); i++) {
                    Material material = availableMaterials.get(i);
                    ItemStack item = new ItemStack(material);
                    bingoGUI.setItem(slots[i], item);
                }

                // Add the Spyglass to the last slot if the feature is enabled
                if (ultimateBingo.currentRevealCards) {
                    bingoGUI.setItem(17, ultimateBingo.bingoFunctions.createSpyglass()); // Add Spyglass to slot 53 (last slot)
                }
                bingoGUIs.put(playerId, bingoGUI);

                // Store the card for each player
                List<ItemStack> cards = new ArrayList<>();
                for (int slot : slots) {
                    ItemStack item = bingoGUI.getItem(slot);
                    if (item != null) {
                        cards.add(item);
                    }
                }
                playerBingoCards.put(playerId, cards);
            }
        }
    }

    public void createGroupBingoCard() {
        started = true;

        // Determine difficulty level and set TOTAL_ITEMS based on it
        int difficultyLevel;
        switch (ultimateBingo.currentDifficulty.toLowerCase()) {
            case "normal":
                difficultyLevel = 2;
                TOTAL_ITEMS = 21;
                break;
            case "hard":
                difficultyLevel = 3;
                TOTAL_ITEMS = 30;
                break;
            default:
                difficultyLevel = 1; // Default to "easy"
                TOTAL_ITEMS = 14;
                break;
        }

        // Generate and shuffle materials for the card
        List<Material> availableMaterials = generateMaterials(difficultyLevel);
        Collections.shuffle(availableMaterials);

        // Get slots based on the card size
        int[] slots = determineSlotsBasedOnCardSize();


        // Store the string for the card type
        String cardInfo = "group";
        cardInfo += ultimateBingo.currentFullCard ? "/满卡" : "/单行";
        cardInfo = "(" + cardInfo + ")";

        // Create a new inventory for each player
        ultimateBingo.groupInventory = Bukkit.createInventory(null, 54, ChatColor.GREEN.toString() + ChatColor.BOLD + "宾果" + " " + ChatColor.LIGHT_PURPLE + cardInfo);

        // Populate the card inventory with selected materials
        for (int i = 0; i < slots.length && i < availableMaterials.size(); i++) {
            Material material = availableMaterials.get(i);
            ItemStack item = new ItemStack(material);
            ultimateBingo.groupInventory.setItem(slots[i], item);
        }

    }

    public void createTeamBingoCards() {
        started = true;

        // Determine difficulty level and set TOTAL_ITEMS based on it
        int difficultyLevel;
        switch (ultimateBingo.currentDifficulty.toLowerCase()) {
            case "normal":
                difficultyLevel = 2;
                TOTAL_ITEMS = 21;
                break;
            case "hard":
                difficultyLevel = 3;
                TOTAL_ITEMS = 30;
                break;
            default:
                difficultyLevel = 1; // Default to "easy"
                TOTAL_ITEMS = 14;
                break;
        }

        // Generate and shuffle materials for the card
        List<Material> availableMaterials = generateMaterials(difficultyLevel);
        Collections.shuffle(availableMaterials);

        // Get slots based on the card size
        int[] slots = determineSlotsBasedOnCardSize();


        // Store the string for the card type
        String cardInfo = ultimateBingo.currentUniqueCard ? "唯一" : "相同";
        cardInfo += ultimateBingo.currentFullCard ? "/满卡" : "/单行";
        cardInfo = "(" + cardInfo + ")";

        // Create a new inventory for each team
        ultimateBingo.redTeamInventory = Bukkit.createInventory(null, 54, ChatColor.RED.toString() + ChatColor.BOLD + "宾果" + " " + ChatColor.LIGHT_PURPLE + cardInfo);
        ultimateBingo.blueTeamInventory = Bukkit.createInventory(null, 54, ChatColor.BLUE.toString() + ChatColor.BOLD + "宾果" + " " + ChatColor.LIGHT_PURPLE + cardInfo);
        ultimateBingo.yellowTeamInventory = Bukkit.createInventory(null, 54, ChatColor.GOLD.toString() + ChatColor.BOLD + "宾果" + " " + ChatColor.LIGHT_PURPLE + cardInfo);

        // Populate the red team card
        for (int i = 0; i < slots.length && i < availableMaterials.size(); i++) {
            Material material = availableMaterials.get(i);
            ItemStack item = new ItemStack(material);
            ultimateBingo.redTeamInventory.setItem(slots[i], item);
        }

        // Add the Spyglass to the last slot if the feature is enabled
        if (ultimateBingo.currentRevealCards) {
            ultimateBingo.redTeamInventory.setItem(17, ultimateBingo.bingoFunctions.createSpyglass()); // Add Spyglass to slot 53 (last slot)
        }

        if (!ultimateBingo.currentUniqueCard) {
            // Cards are identical, copy this card over to Yellow and Blue
            ultimateBingo.bingoFunctions.copyInventoryContents(ultimateBingo.redTeamInventory, ultimateBingo.blueTeamInventory);
            ultimateBingo.bingoFunctions.copyInventoryContents(ultimateBingo.redTeamInventory, ultimateBingo.yellowTeamInventory);
        } else {
            // Cards are unique - shuffle the inventory and assign them

            // Populate the Yellow team card
            Collections.shuffle(availableMaterials);

            for (int i = 0; i < slots.length && i < availableMaterials.size(); i++) {
                Material material = availableMaterials.get(i);
                ItemStack item = new ItemStack(material);
                ultimateBingo.yellowTeamInventory.setItem(slots[i], item);
            }

            // Add the Spyglass to the last slot if the feature is enabled
            if (ultimateBingo.currentRevealCards) {
                ultimateBingo.yellowTeamInventory.setItem(17, ultimateBingo.bingoFunctions.createSpyglass()); // Add Spyglass to slot 53 (last slot)
            }

            Collections.shuffle(availableMaterials);
            // Populate the Blue team card
            for (int i = 0; i < slots.length && i < availableMaterials.size(); i++) {
                Material material = availableMaterials.get(i);
                ItemStack item = new ItemStack(material);
                ultimateBingo.blueTeamInventory.setItem(slots[i], item);
            }

            // Add the Spyglass to the last slot if the feature is enabled
            if (ultimateBingo.currentRevealCards) {
                ultimateBingo.blueTeamInventory.setItem(17, ultimateBingo.bingoFunctions.createSpyglass()); // Add Spyglass to slot 53 (last slot)
            }
        }

    }

    public boolean checkHasBingoCard(Player player) {
        UUID playerId = player.getUniqueId();
        if (ultimateBingo.currentGameMode.equalsIgnoreCase("group") || ultimateBingo.currentGameMode.equalsIgnoreCase("teams")) {
            return true;
        } else {
            return bingoGUIs != null && bingoGUIs.containsKey(playerId);
        }
    }

    public void createUniqueBingoCards() {
        started = true;
        playerBingoCards = new HashMap<>();
        bingoGUIs = new HashMap<>();

        // Determine difficulty level and set TOTAL_ITEMS based on it
        int difficultyLevel;
        switch (ultimateBingo.currentDifficulty.toLowerCase()) {
            case "normal":
                difficultyLevel = 2;
                TOTAL_ITEMS = 21; // Set TOTAL_ITEMS for normal difficulty
                break;
            case "hard":
                difficultyLevel = 3;
                TOTAL_ITEMS = 30; // Set TOTAL_ITEMS for hard difficulty
                break;
            default:
                difficultyLevel = 1; // Default to "easy"
                TOTAL_ITEMS = 14; // Set TOTAL_ITEMS for easy difficulty
                break;
        }

        // Generate a single set of materials for all players
        List<Material> sharedMaterials = generateMaterials(difficultyLevel);

        // Distribute unique shuffled cards to each player
        for (Player player : Bukkit.getOnlinePlayers()) {

            if (ultimateBingo.bingoFunctions.isActivePlayer(player)) {

                UUID playerId = player.getUniqueId();

                // Store the string for the card type
                String cardInfo = ultimateBingo.currentUniqueCard ? "唯一" : "相同";
                cardInfo += ultimateBingo.currentFullCard ? "/满卡" : "/单行";
                cardInfo = "(" + cardInfo + ")";

                // Create a new inventory for each player
                Inventory bingoGUI = Bukkit.createInventory(null, 54, ChatColor.GREEN.toString() + ChatColor.BOLD + "宾果" + ChatColor.BLACK + " " + ChatColor.GOLD + cardInfo);

                // Shuffle the shared materials uniquely for each player
                List<Material> playerMaterials = new ArrayList<>(sharedMaterials);
                Collections.shuffle(playerMaterials);

                List<ItemStack> cards = new ArrayList<>();
                int[] slots = determineSlotsBasedOnCardSize(); // Determine slots based on card size

                // Populate the bingo GUI with shuffled materials
                for (int i = 0; i < slots.length && i < playerMaterials.size(); i++) {
                    Material material = playerMaterials.get(i);
                    ItemStack item = new ItemStack(material);
                    bingoGUI.setItem(slots[i], item);
                    cards.add(item);
                }

                // Add the Spyglass to the last slot if the feature is enabled
                if (ultimateBingo.currentRevealCards) {
                    bingoGUI.setItem(17, ultimateBingo.bingoFunctions.createSpyglass()); // Add Spyglass to slot 53 (last slot)
                }

                playerBingoCards.put(playerId, cards);
                bingoGUIs.put(playerId, bingoGUI);

            }
        }
    }

    private int[] determineSlotsBasedOnCardSize() {
        // Define slot arrangements for different card sizes
        int[] smallSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        int[] mediumSlots = {10, 11, 12, 13, 19, 20, 21, 22, 28, 29, 30, 31, 37, 38, 39, 40};
        int[] largeSlots = {10, 11, 12, 13, 14, 19, 20, 21, 22, 23, 28, 29, 30, 31, 32, 37, 38, 39, 40, 41, 46, 47, 48, 49, 50};

        return switch (ultimateBingo.currentCardSize.toLowerCase()) {
            case "small" -> smallSlots;
            case "medium" -> mediumSlots;
            case "large" -> largeSlots;
            default -> mediumSlots; // Default to medium if something goes wrong
        };
    }

    private int TOTAL_ITEMS = 30;

    public List<Material> generateMaterials(int type) {
        Map<Integer, List<Material>> materials = ultimateBingo.getMaterialList().getMaterials();
        Random random = new Random();
        List<Material> generatedMaterials = new ArrayList<>();

        // Adjust the type if it is greater than 3 - Default to easy
        if (type > 3) {
            type = 1;
        }

        // Define the distribution of items across difficulties based on the type
        int[] distribution = switch (type) {
            case 1 -> new int[]{15, 15, 0, 0, 0};
            case 2 -> new int[]{5, 10, 10, 5, 0};
            case 3 -> new int[]{0, 5, 10, 10, 5};
            default -> new int[]{15, 15, 0, 0, 0};
        };

        // Generate materials based on the defined distribution
        for (int difficulty = 1; difficulty <= 5; difficulty++) {
            List<Material> difficultyMaterials = new ArrayList<>(materials.get(difficulty));
            int itemsToGenerate = distribution[difficulty - 1];
            for (int i = 0; i < itemsToGenerate && !difficultyMaterials.isEmpty(); i++) {
                int randomIndex = random.nextInt(difficultyMaterials.size());
                Material randomMaterial = difficultyMaterials.get(randomIndex);
                generatedMaterials.add(randomMaterial);
                difficultyMaterials.remove(randomIndex);
            }
        }

        // Ensure we always return exactly TOTAL_ITEMS materials
        while (generatedMaterials.size() < TOTAL_ITEMS) {
            List<Material> fallbackMaterials = new ArrayList<>(materials.get(1));
            fallbackMaterials.removeAll(generatedMaterials);
            if (fallbackMaterials.isEmpty()) break; // No more unique materials available
            Material randomMaterial = fallbackMaterials.get(random.nextInt(fallbackMaterials.size()));
            generatedMaterials.add(randomMaterial);
        }

        return generatedMaterials;
    }

    public void markItemAsComplete(Player player, Material completedMaterial) {

        Inventory inv = null;

        if (ultimateBingo.currentGameMode.equalsIgnoreCase("group")) {
            inv = ultimateBingo.groupInventory;
        } else if (ultimateBingo.currentGameMode.equalsIgnoreCase("teams")) {
            inv = ultimateBingo.bingoFunctions.getTeamInventory(player);
        } else {
            inv = getBingoGUIs().get(player.getUniqueId());
        }

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() == completedMaterial) {
                item.setType(ultimateBingo.tickedItemMaterial);
                ItemMeta meta = item.getItemMeta();

                if (ultimateBingo.currentGameMode.equalsIgnoreCase("group") || ultimateBingo.currentGameMode.equalsIgnoreCase("teams")) {
                    meta.setDisplayName(ChatColor.GREEN + player.getName() + "：" + ultimateBingo.bingoFunctions.getMaterialName(completedMaterial));
                } else {
                    meta.setDisplayName(ChatColor.GREEN + "已完成：" + ultimateBingo.bingoFunctions.getMaterialName(completedMaterial));
                }

                item.setItemMeta(meta);

                // Update the player's map to reflect the completion
                ultimateBingo.bingoMapManager.updatePlayerMap(player);

                // Update the player's scoreboard
                ultimateBingo.bingoScoreboardManager.updateBoard(player);

                // Top up their rockets if using the correct loadout
                ultimateBingo.bingoFunctions.topUpFirstFireworkRocketsStack(player);

                String removedUnderscore = ultimateBingo.bingoFunctions.getMaterialName(completedMaterial);
                player.sendMessage(ChatColor.GREEN + "你勾选了 " + ChatColor.GOLD + removedUnderscore + ChatColor.GREEN);

                if (ultimateBingo.currentGameMode.equals("speedrun") || ultimateBingo.currentGameMode.equals("group") || ultimateBingo.currentGameMode.equals("teams")) {
                    // Reset the player's stats
                    ultimateBingo.bingoFunctions.resetIndividualPlayer(player, false);
                }

                for (Player target : Bukkit.getOnlinePlayers()) {

                    if (ultimateBingo.bingoFunctions.isActivePlayer(target)) {

                        // PLAY FOR ALL PLAYERS
                        target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10, 5);

                        if (!target.equals(player)) { // Exclude the player who triggered the event
                            if (ultimateBingo.currentRevealCards) {
                                if (ultimateBingo.currentGameMode.equalsIgnoreCase("group")) {
                                    target.sendMessage(ChatColor.GREEN + player.getName() + ChatColor.WHITE + " 勾选了 " + ChatColor.GREEN + removedUnderscore + ChatColor.WHITE + " 从团队宾果卡上勾选了 ");
                                } else if (ultimateBingo.currentGameMode.equalsIgnoreCase("teams")) {

                                    if (ultimateBingo.bingoFunctions.getTeam(player).equalsIgnoreCase("red")) {

                                        target.sendMessage(ChatColor.RED + player.getName() + ChatColor.WHITE + " 勾选了 " + ChatColor.RED + removedUnderscore);

                                    } else if (ultimateBingo.bingoFunctions.getTeam(player).equalsIgnoreCase("blue")) {

                                        target.sendMessage(ChatColor.BLUE + player.getName() + ChatColor.WHITE + " 勾选了 " + ChatColor.BLUE + removedUnderscore);


                                    } else if (ultimateBingo.bingoFunctions.getTeam(player).equalsIgnoreCase("yellow")) {

                                        target.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.WHITE + " 勾选了 " + ChatColor.YELLOW + removedUnderscore);
                                    }

                                } else {
                                    target.sendMessage(ChatColor.GREEN + player.getName() + ChatColor.WHITE + " 勾选了 " + ChatColor.GREEN + removedUnderscore + ChatColor.WHITE + " 从他的宾果卡上勾选了 ");
                                }
                            } else {
                                target.sendMessage(ChatColor.GREEN + player.getName() + ChatColor.WHITE + " 勾选了一个宾果物品。");
                            }
                        }
                    }
                }

                // Check for bingo based on the card type and size
                String cardSize = ultimateBingo.currentCardSize;
                boolean hasBingo = false;

                // If it's a full card, we'll check the entire card instead
                if (ultimateBingo.currentFullCard) {
                    if (ultimateBingo.cardTypes.checkFullCard(player)) {
                        hasBingo = true;
                    }
                } else {
                    // Not a full card, check for traditional line bingo
                    switch (cardSize.toLowerCase()) {
                        case "small":
                            if (ultimateBingo.cardTypes.checkSmallCardBingo(player)) {
                                hasBingo = true;
                            }
                            break;
                        case "medium":
                            if (ultimateBingo.cardTypes.checkMediumCardBingo(player)) {
                                hasBingo = true;
                            }
                            break;
                        case "large":
                            if (ultimateBingo.cardTypes.checkLargeCardBingo(player)) {
                                hasBingo = true;
                            }
                            break;
                    }
                }

                if (hasBingo) {
                    // Record the game for historical leaderboard
                    long duration = System.currentTimeMillis() - ultimateBingo.gameStartTime;
                    int completed = ultimateBingo.bingoFunctions.countCompleted(inv);
                    int size = ultimateBingo.bingoManager.getSlots().length;
                    ultimateBingo.recordBoard.addGame(player.getName(), completed, duration,
                            size == 25 ? 5 : size == 16 ? 4 : 3);

                    // Disable the game
                    ultimateBingo.bingoStarted = false;

                    if (ultimateBingo.currentGameMode.equalsIgnoreCase("group") || ultimateBingo.currentGameMode.equalsIgnoreCase("teams")) {
                        // Group/Teams: everyone gets a win (cooperative)
                        for (Player target : Bukkit.getOnlinePlayers()) {
                            if (ultimateBingo.bingoFunctions.isActivePlayer(target)) {
                                ultimateBingo.getLeaderboard().addGameResult(
                                        target.getUniqueId(),
                                        cardSize,
                                        ultimateBingo.currentFullCard,
                                        ultimateBingo.currentDifficulty,
                                        ultimateBingo.currentGameMode,
                                        true
                                );
                            }
                        }
                    } else {
                        if (ultimateBingo.bingoFunctions.countActivePlayers() > 1 || ultimateBingo.countSoloGames) {
                            // Competitive: the bingo player gets a win
                            ultimateBingo.getLeaderboard().addGameResult(
                                    player.getUniqueId(),
                                    cardSize,
                                    ultimateBingo.currentFullCard,
                                    ultimateBingo.currentDifficulty,
                                    ultimateBingo.currentGameMode,
                                    true
                            );

                            // Other active players get a loss
                            for (Player target : Bukkit.getOnlinePlayers()) {
                                if (ultimateBingo.bingoFunctions.isActivePlayer(target) && !target.equals(player)) {
                                    ultimateBingo.getLeaderboard().addGameResult(
                                            target.getUniqueId(),
                                            cardSize,
                                            ultimateBingo.currentFullCard,
                                            ultimateBingo.currentDifficulty,
                                            ultimateBingo.currentGameMode,
                                            false
                                    );
                                }
                            }
                        }
                    }

                    if (ultimateBingo.currentGameMode.equalsIgnoreCase("group")) {
                        ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GOLD + player.getName() + ChatColor.GREEN + " 收集了最后一件物品！干得好，团队！");
                        for (Player target : Bukkit.getOnlinePlayers()) {
                            if (ultimateBingo.bingoFunctions.isActivePlayer(target)) {
                                target.playSound(target.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
                                target.sendTitle("宾果！",
                                        ChatColor.GREEN.toString() + ChatColor.BOLD + "呜呼！");
                            }
                        }
                    }
                    else if (ultimateBingo.currentGameMode.equalsIgnoreCase("teams")) {

                        if (ultimateBingo.bingoFunctions.getTeam(player).equalsIgnoreCase("red")) {
                            ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.RED + player.getName() + ChatColor.WHITE + " 收集了最后一件物品！" + ChatColor.RED + "红" + ChatColor.WHITE + "！");

                        } else if (ultimateBingo.bingoFunctions.getTeam(player).equalsIgnoreCase("yellow")) {
                            ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.YELLOW + player.getName() + ChatColor.WHITE + " 收集了最后一件物品！" + ChatColor.YELLOW + "黄" + ChatColor.WHITE + "！");

                        } else if (ultimateBingo.bingoFunctions.getTeam(player).equalsIgnoreCase("blue")) {
                            ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.BLUE + player.getName() + ChatColor.WHITE + " 收集了最后一件物品！" + ChatColor.BLUE + "蓝" + ChatColor.WHITE + "！");

                        } else {
                            ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GOLD + player.getName() + ChatColor.GREEN + " 收集了最后一件物品！干得好，团队！");

                        }

                        for (Player target : Bukkit.getOnlinePlayers()) {
                            if (ultimateBingo.bingoFunctions.isActivePlayer(target)) {
                                target.playSound(target.getLocation(), Sound.ENTITY_GHAST_SCREAM, 1.0f, 1.0f);
                                target.sendTitle(ultimateBingo.bingoFunctions.getTeam(player).toUpperCase() + " 宾果了！",
                                        ChatColor.GREEN.toString() + ChatColor.BOLD + "呜呼！");
                            }
                        }
                    } else {
                        ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GOLD + player.getName() + ChatColor.GREEN + " 宾果了！干得漂亮！");
                        for (Player target : Bukkit.getOnlinePlayers()) {
                            if (ultimateBingo.bingoFunctions.isActivePlayer(target)) {
                                target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 1.0f, 1.0f);
                                target.sendTitle(ChatColor.GOLD + player.getName() + ChatColor.GREEN + " 宾果了！",
                                        ChatColor.GREEN.toString() + ChatColor.BOLD + "呜呼！");
                            }
                        }
                    }

                    ultimateBingo.bingoCommand.stopBingo(player, true);
                } else {

                    // Player doesn't have bingo - take any action we want while the game is still active


                    if (ultimateBingo.currentGameMode.equalsIgnoreCase("brewdash") && ultimateBingo.bingoFunctions.countActivePlayers() > 1) {

                        // Reset potion effects on current player
                        for (PotionEffect effect : player.getActivePotionEffects()) {
                            player.removePotionEffect(effect.getType());
                        }

                        // Apply potion effect to all other players
                        if (ultimateBingo.currentDifficulty.equalsIgnoreCase("easy")) {
                            ultimateBingo.bingoFunctions.applyRandomNegativePotionToOtherPlayers(player, 20);
                        } else if (ultimateBingo.currentDifficulty.equalsIgnoreCase("normal")) {
                            ultimateBingo.bingoFunctions.applyRandomNegativePotionToOtherPlayers(player, 40);
                        } else {
                            ultimateBingo.bingoFunctions.applyRandomNegativePotionToOtherPlayers(player, 60);
                        }
                    }

                }
                break;
            }
        }
    }

    public void clearData() {
        if (bingoGUIs != null) {
            bingoGUIs.clear();
        }

        if (playerBingoCards != null) {
            playerBingoCards.clear();
        }

        // Clear any old team or group inventories
        ultimateBingo.groupInventory = null;
        ultimateBingo.redTeamInventory = null;
        ultimateBingo.blueTeamInventory = null;
        ultimateBingo.yellowTeamInventory = null;
    }

    public void setBingoCards(int amount) {
        bingoCards = amount;
    }

    public Map<UUID, Inventory> getBingoGUIs() {
        return bingoGUIs;
    }

    public Map<UUID, List<ItemStack>> getPlayerBingoCards() {
        return playerBingoCards;
    }

    public int[] getSlots() {
        return slots;
    }

    public boolean isStarted() {
        return started;
    }

    public void joinGameInProgress(Player player) {

        if (!ultimateBingo.currentGameMode.equalsIgnoreCase("group") && !ultimateBingo.currentGameMode.equalsIgnoreCase("teams")) {

            UUID playerId = player.getUniqueId();
            // Check if the player already has a Bingo card
            if (bingoGUIs.containsKey(playerId)) {
                player.sendMessage(ChatColor.YELLOW + "你已经拥有一张宾果卡片。");
                return;
            }

            if (playerBingoCards.isEmpty()) {
                player.sendMessage(ChatColor.RED + "没有可复制的宾果卡片。请等待下一轮。");
                return;
            }

            // Find the card with the fewest ticked off items
            UUID idOfLeastTickedCard = null;
            int fewestTickedItems = Integer.MAX_VALUE;
            for (Map.Entry<UUID, List<ItemStack>> entry : playerBingoCards.entrySet()) {
                int tickedItemsCount = ultimateBingo.bingoFunctions.countTickedItems(entry.getValue());
                if (tickedItemsCount < fewestTickedItems) {
                    fewestTickedItems = tickedItemsCount;
                    idOfLeastTickedCard = entry.getKey();
                }
            }

            if (idOfLeastTickedCard == null) {
                player.sendMessage(ChatColor.RED + "未找到合适的宾果卡片。");
                return;
            }

            // Clone the Bingo GUI and card list
            Inventory originalGui = bingoGUIs.get(idOfLeastTickedCard);
            Inventory clonedGui = ultimateBingo.bingoFunctions.cloneInventory(originalGui);
            List<ItemStack> clonedCardList = new ArrayList<>(playerBingoCards.get(idOfLeastTickedCard));

            // Assign the cloned GUI and card list to the new player
            bingoGUIs.put(playerId, clonedGui);
            playerBingoCards.put(playerId, clonedCardList);

        } else if (ultimateBingo.currentGameMode.equalsIgnoreCase("teams")) {
            // Pick a team and assign them to it
            ultimateBingo.bingoFunctions.assignPlayerToActiveTeam(player);

            if (ultimateBingo.bingoFunctions.getTeam(player).equalsIgnoreCase("red")) {

                ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.RED + player.getName() + ChatColor.GREEN + " 加入了 " + ChatColor.RED + " 红" + ChatColor.GREEN + "team！");

            } else if (ultimateBingo.bingoFunctions.getTeam(player).equalsIgnoreCase("blue")) {

                ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.BLUE + player.getName() + ChatColor.GREEN + " 加入了 " + ChatColor.BLUE + " 蓝" + ChatColor.GREEN + "team！");


            } else if (ultimateBingo.bingoFunctions.getTeam(player).equalsIgnoreCase("yellow")) {

                ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " 加入了 " + ChatColor.YELLOW + " 黄" + ChatColor.GREEN + "team！");

            }


        }
        if (!ultimateBingo.currentGameMode.equalsIgnoreCase("teams")) {
            player.sendMessage(ChatColor.GREEN + "你已加入宾果游戏。祝你好运！");

            ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GOLD + player.getName() + ChatColor.GREEN + " 刚刚加入了宾果！");

        }

        // Give them a bingo card
        ultimateBingo.bingoFunctions.giveBingoCard(player);

        // Give them their loadout gear
        ultimateBingo.bingoFunctions.equipLoadoutGear(player, ultimateBingo.currentLoadoutType);

        if (ultimateBingo.bingoStarted && ultimateBingo.bingoManager.checkHasBingoCard(player) && (ultimateBingo.currentGameMode.equals("speedrun") || ultimateBingo.currentGameMode.equalsIgnoreCase("teams") || ultimateBingo.currentGameMode.equals("group"))) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false, true));
        }

        // Add them to the player list
        ultimateBingo.bingoFunctions.addPlayer(player.getUniqueId());

        // Randomly teleport to a ground location in the world
        ultimateBingo.bingoFunctions.teleportToRandomGround(player);

        // Show the scoreboard
        ultimateBingo.bingoScoreboardManager.showBoard(player);


    }

    // Shuffle Mode Methods
    public void startShuffleMode() {
        if (ultimateBingo.shuffleTaskId != -1) {
            stopShuffleMode();
        }

        int intervalTicks = ultimateBingo.shuffleIntervalMinutes * 60 * 20; // Convert minutes to ticks

        ultimateBingo.shuffleTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                ultimateBingo,
                this::performShuffle,
                intervalTicks,  // Initial delay
                intervalTicks   // Repeat interval
        );
    }

    public void stopShuffleMode() {
        if (ultimateBingo.shuffleTaskId != -1) {
            Bukkit.getScheduler().cancelTask(ultimateBingo.shuffleTaskId);
            ultimateBingo.shuffleTaskId = -1;
        }
    }

    private void performShuffle() {
        // Countdown: 3, 2, 1
        Bukkit.getScheduler().runTaskLater(ultimateBingo, () -> {
            ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.YELLOW + "卡片将在 " + ChatColor.RED + "3" + ChatColor.YELLOW + "……");
            Bukkit.getServer().getOnlinePlayers().forEach(player -> {
                if (ultimateBingo.bingoFunctions.isActivePlayer(player)) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                }
            });
        }, 0L);

        Bukkit.getScheduler().runTaskLater(ultimateBingo, () -> {
            ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.YELLOW + "卡片将在 " + ChatColor.RED + "2" + ChatColor.YELLOW + "……");
            Bukkit.getServer().getOnlinePlayers().forEach(player -> {
                if (ultimateBingo.bingoFunctions.isActivePlayer(player)) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);
                }
            });
        }, 20L);

        Bukkit.getScheduler().runTaskLater(ultimateBingo, () -> {
            ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.YELLOW + "卡片将在 " + ChatColor.RED + "1" + ChatColor.YELLOW + "……");
            Bukkit.getServer().getOnlinePlayers().forEach(player -> {
                if (ultimateBingo.bingoFunctions.isActivePlayer(player)) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
                }
            });
        }, 40L);

        Bukkit.getScheduler().runTaskLater(ultimateBingo, () -> {
            shufflePlayerCards();
            ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GREEN + "" + ChatColor.BOLD + "洗牌！" + ChatColor.RESET + ChatColor.GREEN + " 卡片已重新洗牌！");
            Bukkit.getServer().getOnlinePlayers().forEach(player -> {
                if (ultimateBingo.bingoFunctions.isActivePlayer(player)) {
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                }
            });
        }, 60L);
    }

    private void shufflePlayerCards() {
        // Determine difficulty level for generating new materials
        int difficultyLevel;
        switch (ultimateBingo.currentDifficulty.toLowerCase()) {
            case "normal":
                difficultyLevel = 2;
                break;
            case "hard":
                difficultyLevel = 3;
                break;
            default:
                difficultyLevel = 1;
                break;
        }

        Random random = new Random();

        for (UUID playerId : bingoGUIs.keySet()) {
            Inventory playerGUI = bingoGUIs.get(playerId);
            List<ItemStack> playerCard = playerBingoCards.get(playerId);

            if (playerGUI == null || playerCard == null) continue;

            // Collect uncompleted item slots
            List<Integer> uncompletedSlots = new ArrayList<>();

            for (int i = 0; i < slots.length; i++) {
                int slot = slots[i];
                ItemStack item = playerGUI.getItem(slot);

                if (item != null && item.getType() != ultimateBingo.tickedItemMaterial) {
                    uncompletedSlots.add(slot);
                }
            }

            // Safety check: Need at least 3 uncompleted items (2 to shuffle + 1 to replace)
            if (uncompletedSlots.size() < 3) {
                continue; // Skip this player, not enough items to shuffle
            }

            // Step 1: Shuffle 1-2 pairs of items
            int pairsToShuffle = random.nextInt(2) + 1; // Randomly choose 1 or 2

            for (int pair = 0; pair < pairsToShuffle && uncompletedSlots.size() >= 2; pair++) {
                // Pick two random uncompleted slots
                int index1 = random.nextInt(uncompletedSlots.size());
                int slot1 = uncompletedSlots.get(index1);
                uncompletedSlots.remove(index1); // Remove so we don't pick it again

                int index2 = random.nextInt(uncompletedSlots.size());
                int slot2 = uncompletedSlots.get(index2);
                uncompletedSlots.remove(index2); // Remove so we don't pick it again

                // Swap the items in these two slots
                ItemStack item1 = playerGUI.getItem(slot1);
                ItemStack item2 = playerGUI.getItem(slot2);

                playerGUI.setItem(slot1, item2);
                playerGUI.setItem(slot2, item1);
            }

            // Step 2: Replace 1 random uncompleted item with a new material
            if (!uncompletedSlots.isEmpty()) {
                // Pick a random slot from remaining uncompleted slots
                int slotToReplace = uncompletedSlots.get(random.nextInt(uncompletedSlots.size()));

                // Get current items on the card to avoid duplicates
                Set<Material> currentMaterials = new HashSet<>();
                for (int slot : slots) {
                    ItemStack item = playerGUI.getItem(slot);
                    if (item != null && item.getType() != ultimateBingo.tickedItemMaterial) {
                        currentMaterials.add(item.getType());
                    }
                }

                // Generate a pool of new materials and find one not already on the card
                List<Material> availableMaterials = generateMaterials(difficultyLevel);
                Material newMaterial = null;

                for (Material material : availableMaterials) {
                    if (!currentMaterials.contains(material)) {
                        newMaterial = material;
                        break;
                    }
                }

                // If we found a unique material, replace the old one
                if (newMaterial != null) {
                    ItemStack newItem = new ItemStack(newMaterial);
                    playerGUI.setItem(slotToReplace, newItem);
                }
            }

            // Update the player's card list
            List<ItemStack> newCard = new ArrayList<>();
            for (int slot : slots) {
                ItemStack item = playerGUI.getItem(slot);
                if (item != null) {
                    newCard.add(item);
                }
            }
            playerBingoCards.put(playerId, newCard);

            // Update the player's map
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                ultimateBingo.bingoMapManager.updatePlayerMap(player);
                
                // If player has GUI open, update it
                if (player.getOpenInventory().getTopInventory().equals(playerGUI)) {
                    player.updateInventory();
                }
            }
        }
    }


}