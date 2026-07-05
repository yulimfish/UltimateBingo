package io.shantek.tools;

import io.shantek.UltimateBingo;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BingoFunctions
{
    UltimateBingo ultimateBingo;

    private static final Map<String, String> MATERIAL_CN = new HashMap<>();
    private static final Map<String, String> POTION_CN = new HashMap<>();

    static {
        // 常用材料中文名
        String[][] mats = {
            {"IRON_INGOT","铁锭"},{"GOLD_INGOT","金锭"},{"DIAMOND","钻石"},{"EMERALD","绿宝石"},{"COAL","煤炭"},
            {"REDSTONE","红石粉"},{"LAPIS_LAZULI","青金石"},{"NETHERITE_INGOT","下界合金锭"},{"COPPER_INGOT","铜锭"},
            {"OAK_PLANKS","橡木木板"},{"SPRUCE_PLANKS","云杉木板"},{"BIRCH_PLANKS","白桦木板"},{"JUNGLE_PLANKS","丛林木板"},
            {"ACACIA_PLANKS","金合欢木板"},{"DARK_OAK_PLANKS","深色橡木木板"},{"MANGROVE_PLANKS","红树木板"},
            {"CHERRY_PLANKS","樱花木板"},{"BAMBOO_PLANKS","竹板"},{"CRIMSON_PLANKS","绯红木板"},{"WARPED_PLANKS","诡异木板"},
            {"OAK_LOG","橡木原木"},{"SPRUCE_LOG","云杉原木"},{"BIRCH_LOG","白桦原木"},{"JUNGLE_LOG","丛林原木"},
            {"ACACIA_LOG","金合欢原木"},{"DARK_OAK_LOG","深色橡木原木"},{"MANGROVE_LOG","红树原木"},{"CHERRY_LOG","樱花原木"},
            {"STRIPPED_OAK_LOG","去皮橡木原木"},{"STRIPPED_SPRUCE_LOG","去皮云杉原木"},{"STRIPPED_BIRCH_LOG","去皮白桦原木"},
            {"STRIPPED_JUNGLE_LOG","去皮丛林原木"},{"STRIPPED_ACACIA_LOG","去皮金合欢原木"},{"STRIPPED_DARK_OAK_LOG","去皮深色橡木原木"},
            {"OAK_STAIRS","橡木楼梯"},{"OAK_BUTTON","橡木按钮"},{"BIRCH_DOOR","白桦木门"},{"OAK_SAPLING","橡树树苗"},
            {"BIRCH_SAPLING","白桦树苗"},{"WHITE_WOOL","白色羊毛"},{"ROTTEN_FLESH","腐肉"},{"BONE","骨头"},
            {"STRING","线"},{"FEATHER","羽毛"},{"CHICKEN","生鸡肉"},{"COOKED_CHICKEN","熟鸡肉"},{"LEATHER","皮革"},
            {"BEEF","生牛肉"},{"COOKED_BEEF","牛排"},{"PORKCHOP","生猪排"},{"COOKED_PORKCHOP","熟猪排"},{"MUTTON","生羊肉"},
            {"COOKED_MUTTON","熟羊肉"},{"RABBIT","生兔肉"},{"COOKED_RABBIT","熟兔肉"},{"COD","生鳕鱼"},{"COOKED_COD","熟鳕鱼"},
            {"SALMON","生鲑鱼"},{"COOKED_SALMON","熟鲑鱼"},{"TROPICAL_FISH","热带鱼"},{"PUFFERFISH","河豚"},
            {"SUGAR_CANE","甘蔗"},{"BARREL","木桶"},{"CHEST","箱子"},{"ACACIA_BOAT","金合欢木船"},{"SPRUCE_CHEST_BOAT","云杉木运输船"},
            {"SPRUCE_BUTTON","云杉木按钮"},{"APPLE","苹果"},{"FLINT","燧石"},{"FLINT_AND_STEEL","打火石"},{"FLOWER_POT","花盆"},
            {"BOOK","书"},{"CAULDRON","炼药锅"},{"LIGHTNING_ROD","避雷针"},{"RAIL","铁轨"},{"SNOW_BLOCK","雪块"},
            {"COCOA_BEANS","可可豆"},{"GLASS","玻璃"},{"COAL_BLOCK","煤炭块"},{"GLASS_BOTTLE","玻璃瓶"},{"WATER_BUCKET","水桶"},
            {"LAVA_BUCKET","熔岩桶"},{"BUCKET","铁桶"},{"MILK_BUCKET","奶桶"},{"POWERED_RAIL","动力铁轨"},{"DETECTOR_RAIL","探测铁轨"},
            {"MINECART","矿车"},{"CHEST_MINECART","运输矿车"},{"FURNACE_MINECART","动力矿车"},{"HOPPER_MINECART","漏斗矿车"},
            {"COMPASS","指南针"},{"CLOCK","时钟"},{"SPYGLASS","望远镜"},{"MAP","空地图"},{"FILLED_MAP","地图"},
            {"WRITABLE_BOOK","书与笔"},{"WRITTEN_BOOK","成书"},{"ITEM_FRAME","物品展示框"},{"GLOW_ITEM_FRAME","荧光物品展示框"},
            {"PAINTING","画"},{"BOWL","碗"},{"MUSHROOM_STEW","蘑菇煲"},{"RABBIT_STEW","兔肉煲"},{"BEETROOT_SOUP","甜菜汤"},
            {"PUMPKIN_PIE","南瓜派"},{"CAKE","蛋糕"},{"BREAD","面包"},{"COOKIE","曲奇"},{"MELON_SLICE","西瓜片"},
            {"SWEET_BERRIES","甜浆果"},{"GLOW_BERRIES","发光浆果"},{"EGG","鸡蛋"},{"SUGAR","糖"},{"HONEY_BOTTLE","蜂蜜瓶"},
            {"HONEYCOMB","蜜脾"},{"INK_SAC","墨囊"},{"GLOW_INK_SAC","发光墨囊"},{"BRICK","红砖"},{"CLAY_BALL","黏土球"},
            {"STONE","石头"},{"COBBLESTONE","圆石"},{"MOSSY_COBBLESTONE","苔石"},{"SMOOTH_STONE","平滑石头"},
            {"STONE_BRICKS","石砖"},{"MOSSY_STONE_BRICKS","苔石砖"},{"CRACKED_STONE_BRICKS","裂纹石砖"},{"CHISELED_STONE_BRICKS","雕纹石砖"},
            {"GRANITE","花岗岩"},{"DIORITE","闪长岩"},{"ANDESITE","安山岩"},{"DEEPSLATE","深板岩"},{"TUFF","凝灰岩"},{"CALCITE","方解石"},
            {"DRIPSTONE_BLOCK","滴水石块"},{"POINTED_DRIPSTONE","滴水石锥"},{"SAND","沙子"},{"RED_SAND","红沙"},{"GRAVEL","沙砾"},
            {"DIRT","泥土"},{"COARSE_DIRT","砂土"},{"ROOTED_DIRT","缠根泥土"},{"PODZOL","灰化土"},{"MYCELIUM","菌丝体"},
            {"GRASS_BLOCK","草方块"},{"MOSS_BLOCK","苔藓块"},{"MOSS_CARPET","覆地苔藓"},{"FARMLAND","耕地"},{"CLAY","黏土"},
            {"MUD","泥巴"},{"MUDDY_MANGROVE_ROOTS","沾泥的红树根"},{"PACKED_MUD","泥坯"},{"MUD_BRICKS","泥砖"},
            {"SANDSTONE","砂岩"},{"RED_SANDSTONE","红砂岩"},{"CUT_SANDSTONE","切制砂岩"},{"CHISELED_SANDSTONE","雕纹砂岩"},
            {"SMOOTH_SANDSTONE","平滑砂岩"},{"OBSIDIAN","黑曜石"},{"CRYING_OBSIDIAN","哭泣的黑曜石"},{"BEDROCK","基岩"},
            {"NETHERRACK","下界岩"},{"NETHER_BRICK","下界砖"},{"NETHER_BRICKS","下界砖块"},{"SOUL_SAND","灵魂沙"},
            {"SOUL_SOIL","灵魂土"},{"BASALT","玄武岩"},{"SMOOTH_BASALT","平滑玄武岩"},{"POLISHED_BASALT","磨制玄武岩"},
            {"BLACKSTONE","黑石"},{"POLISHED_BLACKSTONE","磨制黑石"},{"GILDED_BLACKSTONE","镶金黑石"},{"END_STONE","末地石"},
            {"END_STONE_BRICKS","末地石砖"},{"PRISMARINE","海晶石"},{"PRISMARINE_BRICKS","海晶石砖"},{"DARK_PRISMARINE","暗海晶石"},
            {"SEA_LANTERN","海晶灯"},{"GLOWSTONE","荧石"},{"MAGMA_BLOCK","岩浆块"},{"BONE_BLOCK","骨块"},
            {"DRIED_KELP_BLOCK","干海带块"},{"HAY_BLOCK","干草捆"},{"SPONGE","海绵"},{"WET_SPONGE","湿海绵"},
            {"TORCH","火把"},{"SOUL_TORCH","灵魂火把"},{"REDSTONE_TORCH","红石火把"},{"LANTERN","灯笼"},{"SOUL_LANTERN","灵魂灯笼"},
            {"END_ROD","末地烛"},{"GLOW_LICHEN","发光地衣"},{"SCULK","幽匿块"},{"SCULK_SENSOR","幽匿感测体"},
            {"SCULK_CATALYST","幽匿催发体"},{"SCULK_SHRIEKER","幽匿尖啸体"},{"SCULK_VEIN","幽匿脉络"},
            {"IRON_BLOCK","铁块"},{"GOLD_BLOCK","金块"},{"DIAMOND_BLOCK","钻石块"},{"EMERALD_BLOCK","绿宝石块"},
            {"REDSTONE_BLOCK","红石块"},{"LAPIS_BLOCK","青金石块"},{"COPPER_BLOCK","铜块"},{"NETHERITE_BLOCK","下界合金块"},
            {"AMETHYST_BLOCK","紫水晶块"},{"QUARTZ_BLOCK","石英块"},{"SMOOTH_QUARTZ","平滑石英块"},
            {"CHISELED_QUARTZ_BLOCK","雕纹石英块"},{"QUARTZ_PILLAR","石英柱"},{"QUARTZ_STAIRS","石英楼梯"},{"BRICKS","红砖块"},
            {"BOOKSHELF","书架"},{"CRAFTING_TABLE","工作台"},{"FURNACE","熔炉"},{"BLAST_FURNACE","高炉"},{"SMOKER","烟熏炉"},
            {"CAMPFIRE","篝火"},{"SOUL_CAMPFIRE","灵魂篝火"},{"ANVIL","铁砧"},{"CHIPPED_ANVIL","开裂的铁砧"},
            {"DAMAGED_ANVIL","损坏的铁砧"},{"LOOM","织布机"},{"SMITHING_TABLE","锻造台"},{"CARTOGRAPHY_TABLE","制图台"},
            {"FLETCHING_TABLE","制箭台"},{"GRINDSTONE","砂轮"},{"COMPOSTER","堆肥桶"},{"JUKEBOX","唱片机"},{"NOTE_BLOCK","音符盒"},
            {"DISPENSER","发射器"},{"DROPPER","投掷器"},{"HOPPER","漏斗"},{"PISTON","活塞"},{"STICKY_PISTON","黏性活塞"},
            {"OBSERVER","侦测器"},{"REPEATER","红石中继器"},{"COMPARATOR","红石比较器"},{"DAYLIGHT_DETECTOR","阳光探测器"},
            {"LECTERN","讲台"},{"TARGET","标靶"},{"TRIPWIRE_HOOK","绊线钩"},{"LEVER","拉杆"},{"STONE_BUTTON","石头按钮"},
            {"POLISHED_BLACKSTONE_BUTTON","磨制黑石按钮"},{"BEE_NEST","蜂巢"},{"BEEHIVE","蜂箱"},{"HONEY_BLOCK","蜂蜜块"},
            {"SLIME_BLOCK","黏液块"},{"MAGMA_CREAM","岩浆膏"},{"BLAZE_ROD","烈焰棒"},{"BLAZE_POWDER","烈焰粉"},
            {"GHAST_TEAR","恶魂之泪"},{"NETHER_WART","下界疣"},{"SPIDER_EYE","蜘蛛眼"},{"FERMENTED_SPIDER_EYE","发酵蛛眼"},
            {"GUNPOWDER","火药"},{"SLIME_BALL","黏液球"},{"ENDER_PEARL","末影珍珠"},{"ENDER_EYE","末影之眼"},
            {"SHULKER_SHELL","潜影壳"},{"POPPED_CHORUS_FRUIT","爆裂紫颂果"},{"CHORUS_FRUIT","紫颂果"},{"DRAGON_BREATH","龙息"},
            {"PHANTOM_MEMBRANE","幻翼膜"},{"NAUTILUS_SHELL","鹦鹉螺壳"},{"HEART_OF_THE_SEA","海洋之心"},{"TURTLE_EGG","海龟蛋"},
            {"SCUTE","鳞甲"},{"NAME_TAG","命名牌"},{"SADDLE","鞍"},{"LEAD","拴绳"},{"BUNDLE","收纳袋"},
            {"EXPERIENCE_BOTTLE","附魔之瓶"},{"END_CRYSTAL","末地水晶"},{"FIREWORK_ROCKET","烟花火箭"},{"FIREWORK_STAR","烟火之星"},
            {"TOTEM_OF_UNDYING","不死图腾"},{"ELYTRA","鞘翅"},{"SHIELD","盾牌"},{"TRIDENT","三叉戟"},{"CROSSBOW","弩"},
            {"BOW","弓"},{"ARROW","箭"},{"SPECTRAL_ARROW","光灵箭"},{"TIPPED_ARROW","药箭"},
            {"WOODEN_SWORD","木剑"},{"STONE_SWORD","石剑"},{"IRON_SWORD","铁剑"},{"GOLDEN_SWORD","金剑"},
            {"DIAMOND_SWORD","钻石剑"},{"NETHERITE_SWORD","下界合金剑"},{"WOODEN_AXE","木斧"},{"STONE_AXE","石斧"},
            {"IRON_AXE","铁斧"},{"GOLDEN_AXE","金斧"},{"DIAMOND_AXE","钻石斧"},{"NETHERITE_AXE","下界合金斧"},
            {"WOODEN_PICKAXE","木镐"},{"STONE_PICKAXE","石镐"},{"IRON_PICKAXE","铁镐"},{"GOLDEN_PICKAXE","金镐"},
            {"DIAMOND_PICKAXE","钻石镐"},{"NETHERITE_PICKAXE","下界合金镐"},{"WOODEN_SHOVEL","木锹"},{"STONE_SHOVEL","石锹"},
            {"IRON_SHOVEL","铁锹"},{"GOLDEN_SHOVEL","金锹"},{"DIAMOND_SHOVEL","钻石锹"},{"NETHERITE_SHOVEL","下界合金锹"},
            {"WOODEN_HOE","木锄"},{"STONE_HOE","石锄"},{"IRON_HOE","铁锄"},{"GOLDEN_HOE","金锄"},{"DIAMOND_HOE","钻石锄"},
            {"NETHERITE_HOE","下界合金锄"},{"LEATHER_HELMET","皮革帽子"},{"LEATHER_CHESTPLATE","皮革外套"},
            {"LEATHER_LEGGINGS","皮革裤子"},{"LEATHER_BOOTS","皮革靴子"},{"CHAINMAIL_HELMET","锁链头盔"},
            {"CHAINMAIL_CHESTPLATE","锁链胸甲"},{"CHAINMAIL_LEGGINGS","锁链护腿"},{"CHAINMAIL_BOOTS","锁链靴子"},
            {"IRON_HELMET","铁头盔"},{"IRON_CHESTPLATE","铁胸甲"},{"IRON_LEGGINGS","铁护腿"},{"IRON_BOOTS","铁靴子"},
            {"GOLDEN_HELMET","金头盔"},{"GOLDEN_CHESTPLATE","金胸甲"},{"GOLDEN_LEGGINGS","金护腿"},{"GOLDEN_BOOTS","金靴子"},
            {"DIAMOND_HELMET","钻石头盔"},{"DIAMOND_CHESTPLATE","钻石胸甲"},{"DIAMOND_LEGGINGS","钻石护腿"},{"DIAMOND_BOOTS","钻石靴子"},
            {"NETHERITE_HELMET","下界合金头盔"},{"NETHERITE_CHESTPLATE","下界合金胸甲"},{"NETHERITE_LEGGINGS","下界合金护腿"},
            {"NETHERITE_BOOTS","下界合金靴子"},{"TURTLE_HELMET","海龟壳"},{"WOLF_ARMOR","狼铠"},
            {"OAK_LEAVES","橡树树叶"},{"SPRUCE_LEAVES","云杉树叶"},{"BIRCH_LEAVES","白桦树叶"},{"JUNGLE_LEAVES","丛林树叶"},
            {"ACACIA_LEAVES","金合欢树叶"},{"DARK_OAK_LEAVES","深色橡树树叶"},{"AZALEA_LEAVES","杜鹃树叶"},
            {"FLOWERING_AZALEA_LEAVES","盛开的杜鹃树叶"},{"MANGROVE_LEAVES","红树树叶"},{"CHERRY_LEAVES","樱花树叶"},
            {"LILY_PAD","睡莲"},{"VINE","藤蔓"},{"BIG_DRIPLEAF","大型垂滴叶"},{"SMALL_DRIPLEAF","小型垂滴叶"},
            {"SEAGRASS","海草"},{"SEA_PICKLE","海泡菜"},{"KELP","海带"},{"BROWN_MUSHROOM","棕色蘑菇"},{"RED_MUSHROOM","红色蘑菇"},
            {"CRIMSON_FUNGUS","绯红菌"},{"WARPED_FUNGUS","诡异菌"},{"CRIMSON_STEM","绯红菌柄"},{"WARPED_STEM","诡异菌柄"},
            {"SHROOMLIGHT","菌光体"},{"NETHER_WART_BLOCK","下界疣块"},{"WARPED_WART_BLOCK","诡异疣块"},
            {"RED_BED","红色床"},{"ORANGE_WOOL","橙色羊毛"},{"YELLOW_WOOL","黄色羊毛"},{"RED_WOOL","红色羊毛"},
            {"BAMBOO","竹子"},{"PUMPKIN","南瓜"},{"BELL","钟"},{"CHAIN","锁链"},{"MELON","西瓜"},{"YELLOW_BED","黄色床"},
            {"POPPY","虞美人"},{"SHORT_GRASS","矮草丛"},{"BLACK_WOOL","黑色羊毛"},{"BROWN_WOOL","棕色羊毛"},
            {"GLASS_PANE","玻璃板"},{"GOLDEN_APPLE","金苹果"},{"BAMBOO_BLOCK","竹块"},{"DEAD_BUSH","枯萎的灌木"},
            {"SPORE_BLOSSOM","孢子花"},{"PINK_PETALS","粉红色花簇"},{"RED_CONCRETE","红色混凝土"},{"YELLOW_CONCRETE","黄色混凝土"},
            {"WHITE_CONCRETE","白色混凝土"},{"BLACK_CONCRETE","黑色混凝土"},{"JACK_O_LANTERN","南瓜灯"},{"STONECUTTER","切石机"},
            {"BREWING_STAND","酿造台"},{"BRUSH","刷子"},{"GREEN_CARPET","绿色地毯"},{"CACTUS","仙人掌"},
            {"TNT","TNT"},{"GOAT_HORN","山羊角"},{"BEETROOT","甜菜根"},{"AMETHYST_SHARD","紫水晶碎片"},
            {"NETHERITE_SCRAP","下界合金碎片"},{"IRON_NUGGET","铁粒"},{"GOLD_NUGGET","金粒"},{"CHARCOAL","木炭"},
            {"QUARTZ","下界石英"},{"COBBLED_DEEPSLATE","深板岩圆石"},{"POLISHED_DEEPSLATE","磨制深板岩"},
            {"CRACKED_NETHER_BRICKS","裂纹下界砖块"},{"CHISELED_NETHER_BRICKS","雕纹下界砖块"},
        };
        for (String[] m : mats) MATERIAL_CN.put(m[0], m[1]);

        // 药水效果中文名
        String[][] pots = {
            {"SPEED","迅捷"},{"SLOW","缓慢"},{"FAST_DIGGING","急迫"},{"SLOW_DIGGING","挖掘疲劳"},
            {"INCREASE_DAMAGE","力量"},{"HEAL","瞬间治疗"},{"HARM","瞬间伤害"},{"JUMP","跳跃提升"},
            {"CONFUSION","反胃"},{"REGENERATION","再生"},{"DAMAGE_RESISTANCE","抗性提升"},{"FIRE_RESISTANCE","防火"},
            {"WATER_BREATHING","水下呼吸"},{"INVISIBILITY","隐身"},{"BLINDNESS","失明"},{"NIGHT_VISION","夜视"},
            {"HUNGER","饥饿"},{"WEAKNESS","虚弱"},{"POISON","中毒"},{"WITHER","凋零"},{"HEALTH_BOOST","生命提升"},
            {"ABSORPTION","伤害吸收"},{"SATURATION","饱和"},{"GLOWING","发光"},{"LEVITATION","飘浮"},
            {"LUCK","幸运"},{"UNLUCK","霉运"},{"SLOW_FALLING","缓降"},{"CONDUIT_POWER","潮涌能量"},
            {"DOLPHINS_GRACE","海豚的恩惠"},{"BAD_OMEN","不祥之兆"},{"HERO_OF_THE_VILLAGE","村庄英雄"},{"DARKNESS","黑暗"},
        };
        for (String[] p : pots) POTION_CN.put(p[0], p[1]);
    }

    public String getMaterialName(Material material) {
        if (material == null) return "未知";
        return MATERIAL_CN.getOrDefault(material.name(), material.name().toLowerCase().replace('_', ' '));
    }

    public String getPotionName(PotionEffectType type) {
        if (type == null) return "未知";
        return POTION_CN.getOrDefault(type.getName(), type.getName().toLowerCase().replace('_', ' '));
    }

    public BingoFunctions(UltimateBingo ultimateBingo){
        this.ultimateBingo = ultimateBingo;

        this.configFile = new File(ultimateBingo.getDataFolder(), "ingameconfig.yml");
        this.config = YamlConfiguration.loadConfiguration(configFile);
        loadSignData();
    }

    private Random random = new Random();
    private HashMap<UUID, Boolean> playerMap = new HashMap<>();
    private final File configFile;
    private final FileConfiguration config;
    public final Map<String, Location> signLocations = new HashMap<>();
    public final Map<String, Location> teamSignLocations = new HashMap<>();
    private final Map<UUID, String> manualTeamAssignments = new HashMap<>();
    public Location startButtonLocation;

    //region Resetting the players

    // Reset the players state at the start and end of games
    public void resetPlayers(){
        for (Player player : Bukkit.getOnlinePlayers()){

            if (ultimateBingo.bingoFunctions.isActivePlayer(player)) {

                // Reset health to max health (20.0 is full health)
                player.setHealth(20.0);

                // Reset food level to max (20 is full hunger)
                player.setFoodLevel(20);

                // Reset saturation to max (5.0F is full saturation)
                player.setSaturation(5.0F);

                // Reset exhaustion to 0 (no exhaustion)
                player.setExhaustion(0.0F);

                // Extinguish the player
                player.setFireTicks(0);

                // Reset remaining potion effects
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    player.removePotionEffect(effect.getType());
                }

                // Clear inventory
                player.getInventory().clear();

                // Clear armor
                player.getInventory().setArmorContents(new ItemStack[4]);

                // Reset XP and levels
                player.setExp(0);
                player.setLevel(0);
            }
        }
    }

    public void resetIndividualPlayer(Player player, boolean fullReset) {

        // Reset health to max health (20.0 is full health)
        player.setHealth(20.0);

        // Reset food level to max (20 is full hunger)
        player.setFoodLevel(20);

        // Reset saturation to max (5.0F is full saturation)
        player.setSaturation(5.0F);

        // Reset exhaustion to 0 (no exhaustion)
        player.setExhaustion(0.0F);

        // Extinguish the player
        player.setFireTicks(0);

        if (fullReset) {
            // Reset remaining potion effects
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }

            // Double check they're in the correct world to avoid
            // removing any actual inventory from another world
            if (isActivePlayer(player)) {

                // Clear inventory
                player.getInventory().clear();

            }
            // Clear armor
            player.getInventory().setArmorContents(new ItemStack[4]);

            // Reset XP and levels
            player.setExp(0);
            player.setLevel(0);

        }
    }



    //endregion

    //region Bingo card functionality

    public int countCompleted(Inventory inventory) {
        int count = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == ultimateBingo.tickedItemMaterial) {
                count++;
            }
        }
        return count;
    }

    public void giveBingoCard(Player player) {
        // Remove any existing bingo card maps first
        removeBingoMaps(player);

        // Give the player a map with their bingo card
        ultimateBingo.bingoMapManager.giveBingoMap(player);
    }

    /**
     * Remove all bingo card maps from a player's inventory.
     */
    public void removeBingoMaps(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() == Material.FILLED_MAP) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName() &&
                        meta.getDisplayName().equals(ChatColor.GOLD + "宾果卡片")) {
                    player.getInventory().setItem(i, null);
                }
            }
        }
        // Also clean up the map manager's tracking
        ultimateBingo.bingoMapManager.removePlayerMap(player);
    }

    private boolean hasBingoCard(PlayerInventory inventory) {
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == Material.FILLED_MAP) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equals(ChatColor.GOLD + "宾果卡片")) {
                    return true; // Bingo card found
                }
            }
        }
        return false; // No Bingo card found
    }

    // Give all players a bingo card
    public void giveBingoCardToAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {

            if (ultimateBingo.bingoFunctions.isActivePlayer(player)) {
                giveBingoCard(player);
            }
        }
    }

    public int countTickedItems(List<ItemStack> items) {
        int count = 0;
        for (ItemStack item : items) {
            // Check if the item is not null and is specifically LIME_CONCRETE
            if (item != null && item.getType() == ultimateBingo.tickedItemMaterial) {
                count++;
            }
        }
        return count;
    }

    // Utility method to clone an inventory
    public Inventory cloneInventory(Inventory original) {

        // Store the string for the card type
        String newCardInfo = ultimateBingo.currentUniqueCard ? "唯一" : "相同";
        newCardInfo += ultimateBingo.currentFullCard ? "/满卡" : "/单行";
        newCardInfo = "(" + newCardInfo + ")";

        Inventory clone = Bukkit.createInventory(null, original.getSize(), ChatColor.GREEN.toString() + ChatColor.BOLD + "宾果" + ChatColor.BLACK + " " + ChatColor.GOLD + newCardInfo);
        for (int i = 0; i < original.getSize(); i++) {
            ItemStack originalItem = original.getItem(i);
            if (originalItem != null) {
                clone.setItem(i, new ItemStack(originalItem));
            }
        }
        return clone;
    }

    //endregion

    //region Resetting the world

    // Reset the time and weather at the start of the game
    public void resetTimeAndWeather() {
        if (ultimateBingo.multiWorldServer && ultimateBingo.bingoWorld != null) {
            // Multi-world: only affect the bingo world
            World bingoWorld = Bukkit.getWorld(ultimateBingo.bingoWorld);
            if (bingoWorld != null) {

                if (bingoWorld.getEnvironment() == World.Environment.NORMAL) {
                    try {
                        bingoWorld.setTime(0);
                    } catch (IllegalArgumentException ignored) {
                        // Custom NORMAL world without a clock, skip
                    }
                }

                bingoWorld.setStorm(false);
                bingoWorld.setThundering(false);
                bingoWorld.setWeatherDuration(0);
            }
        } else {
            // Single world: affect all worlds
            for (World world : Bukkit.getWorlds()) {

                if (world.getEnvironment() == World.Environment.NORMAL) {
                    try {
                        world.setTime(0);
                    } catch (IllegalArgumentException ignored) {
                        // Custom NORMAL world without a clock, skip
                    }
                }

                world.setStorm(false);
                world.setThundering(false);
                world.setWeatherDuration(0);
            }
        }
    }

    // Despawn all items on the ground at the start/end of the game
    public void despawnAllItems() {

        if (ultimateBingo.multiWorldServer && ultimateBingo.bingoWorld != null) {

            for (World world : Bukkit.getWorlds()) {
                if (world.getName().equalsIgnoreCase(ultimateBingo.bingoWorld)) {
                    for (Entity entity : world.getEntitiesByClass(Item.class)) {
                        entity.remove();
                    }
                }
            }

        } else {
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntitiesByClass(Item.class)) {
                    entity.remove();
                }
            }
        }
    }

    //endregion

    //region Item stacks and equipping

    // Speed run equipment for players
    public void equipLoadoutGear(Player player, int loadout) {

        if (loadout == 1) {

            //region 1st load-out - Basic starter gear

            player.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD));
            player.getInventory().addItem(new ItemStack(Material.WOODEN_AXE));
            player.getInventory().addItem(new ItemStack(Material.WOODEN_PICKAXE));
            player.getInventory().addItem(new ItemStack(Material.WOODEN_SHOVEL));
            player.getInventory().addItem(new ItemStack(Material.WOODEN_HOE));
            player.getInventory().addItem(new ItemStack(Material.CRAFTING_TABLE, 1));

            //endregion

        } else if (loadout == 2) {

            //region 2nd load-out - Boat

            // Create and set armor
            player.getInventory().setHelmet(createEnchantedArmor(Material.IRON_HELMET, new Enchantment[]{
                    Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.WATER_WORKER, Enchantment.MENDING, Enchantment.DURABILITY, Enchantment.VANISHING_CURSE, Enchantment.BINDING_CURSE
            }, new int[]{1, 1, 1, 1, 1, 1}));
            player.getInventory().setChestplate(createEnchantedArmor(Material.IRON_CHESTPLATE, new Enchantment[]{
                    Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.MENDING, Enchantment.DURABILITY, Enchantment.VANISHING_CURSE, Enchantment.BINDING_CURSE
            }, new int[]{1, 1, 1, 1, 1}));
            player.getInventory().setLeggings(createEnchantedArmor(Material.IRON_LEGGINGS, new Enchantment[]{
                    Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.MENDING, Enchantment.DURABILITY, Enchantment.VANISHING_CURSE, Enchantment.BINDING_CURSE
            }, new int[]{1, 1, 1, 1, 1}));
            player.getInventory().setBoots(createEnchantedArmor(Material.IRON_BOOTS, new Enchantment[]{
                    Enchantment.PROTECTION_FALL, Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.MENDING, Enchantment.DURABILITY, Enchantment.VANISHING_CURSE, Enchantment.BINDING_CURSE
            }, new int[]{1, 1, 1, 1, 1, 1}));

            // Equip shield
            ItemStack shield = new ItemStack(Material.SHIELD);
            player.getInventory().setItemInOffHand(shield);

            // Give player their basic tools
            player.getInventory().addItem(createEnchantedItem(Material.IRON_SWORD, new Enchantment[]{Enchantment.DAMAGE_ALL, Enchantment.KNOCKBACK, Enchantment.FIRE_ASPECT, Enchantment.LOOT_BONUS_MOBS, Enchantment.SWEEPING_EDGE}, new int[]{1, 1, 1, 1, 1}));
            player.getInventory().addItem(createEnchantedItem(Material.IRON_PICKAXE, new Enchantment[]{Enchantment.DIG_SPEED, Enchantment.LOOT_BONUS_BLOCKS, Enchantment.DURABILITY}, new int[]{1, 1, 1}));
            player.getInventory().addItem(createEnchantedItem(Material.IRON_AXE, new Enchantment[]{Enchantment.DIG_SPEED, Enchantment.DURABILITY, Enchantment.MENDING, Enchantment.SILK_TOUCH}, new int[]{1, 1, 1, 1}));
            player.getInventory().addItem(createEnchantedItem(Material.IRON_SHOVEL, new Enchantment[]{Enchantment.DIG_SPEED, Enchantment.DURABILITY, Enchantment.MENDING}, new int[]{1, 1, 1}));
            player.getInventory().addItem(createEnchantedItem(Material.IRON_HOE, new Enchantment[]{Enchantment.DIG_SPEED, Enchantment.DURABILITY, Enchantment.MENDING}, new int[]{1, 1, 1}));

            // Add additional items
            player.getInventory().addItem(new ItemStack(Material.RED_BED));
            player.getInventory().addItem(new ItemStack(Material.CRAFTING_TABLE, 1));
            player.getInventory().addItem(new ItemStack(Material.JUNGLE_BOAT, 1));

            //endregion

        } else if (loadout == 3) {

            //region 3rd load-out - Wings

            // Create and set armor
            player.getInventory().setHelmet(createEnchantedArmor(Material.NETHERITE_HELMET, new Enchantment[]{
                    Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.WATER_WORKER, Enchantment.MENDING, Enchantment.DURABILITY, Enchantment.VANISHING_CURSE, Enchantment.BINDING_CURSE
            }, new int[]{4, 1, 1, 3, 1, 1}));
            player.getInventory().setChestplate(createEnchantedElytra(new Enchantment[]{
                    Enchantment.DURABILITY, // Unbreaking
                    Enchantment.MENDING
            }, new int[]{3, 1})); // Level 3 Unbreaking, Level 1 Mending
            player.getInventory().setLeggings(createEnchantedArmor(Material.NETHERITE_LEGGINGS, new Enchantment[]{
                    Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.MENDING, Enchantment.DURABILITY, Enchantment.VANISHING_CURSE, Enchantment.BINDING_CURSE
            }, new int[]{4, 1, 3, 1, 1}));
            player.getInventory().setBoots(createEnchantedArmor(Material.NETHERITE_BOOTS, new Enchantment[]{
                    Enchantment.PROTECTION_FALL, Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.MENDING, Enchantment.DURABILITY, Enchantment.VANISHING_CURSE, Enchantment.BINDING_CURSE
            }, new int[]{4, 4, 1, 3, 1, 1}));

            // Give player their basic tools
            ItemStack fireworkStack = createFireworkRocket();
            player.getInventory().addItem(fireworkStack);

            player.getInventory().addItem(createEnchantedItem(Material.NETHERITE_SWORD, new Enchantment[]{Enchantment.DAMAGE_ALL, Enchantment.KNOCKBACK, Enchantment.FIRE_ASPECT, Enchantment.LOOT_BONUS_MOBS, Enchantment.SWEEPING_EDGE}, new int[]{5, 2, 2, 3, 3}));
            player.getInventory().addItem(createEnchantedItem(Material.NETHERITE_PICKAXE, new Enchantment[]{Enchantment.DIG_SPEED, Enchantment.LOOT_BONUS_BLOCKS, Enchantment.DURABILITY}, new int[]{5, 3, 3}));
            player.getInventory().addItem(createEnchantedItem(Material.NETHERITE_AXE, new Enchantment[]{Enchantment.DIG_SPEED, Enchantment.DURABILITY, Enchantment.MENDING, Enchantment.SILK_TOUCH}, new int[]{5, 3, 1, 1}));
            player.getInventory().addItem(createEnchantedItem(Material.NETHERITE_SHOVEL, new Enchantment[]{Enchantment.DIG_SPEED, Enchantment.DURABILITY, Enchantment.MENDING}, new int[]{5, 3, 1}));
            player.getInventory().addItem(createEnchantedItem(Material.NETHERITE_HOE, new Enchantment[]{Enchantment.DIG_SPEED, Enchantment.DURABILITY, Enchantment.MENDING}, new int[]{5, 3, 1}));

            // Add additional items
            player.getInventory().addItem(new ItemStack(Material.CRAFTING_TABLE, 1));

            //endregion

        } else if (loadout == 4) {

            //region 4th load-out - Archer

            // Create and set armor
            player.getInventory().setHelmet(createEnchantedArmor(Material.IRON_HELMET, new Enchantment[]{
                    Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.WATER_WORKER, Enchantment.MENDING, Enchantment.DURABILITY, Enchantment.VANISHING_CURSE, Enchantment.BINDING_CURSE
            }, new int[]{1, 1, 1, 1, 1, 1}));
            player.getInventory().setChestplate(createEnchantedArmor(Material.IRON_CHESTPLATE, new Enchantment[]{
                    Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.MENDING, Enchantment.DURABILITY, Enchantment.VANISHING_CURSE, Enchantment.BINDING_CURSE
            }, new int[]{1, 1, 1, 1, 1}));
            player.getInventory().setLeggings(createEnchantedArmor(Material.IRON_LEGGINGS, new Enchantment[]{
                    Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.MENDING, Enchantment.DURABILITY, Enchantment.VANISHING_CURSE, Enchantment.BINDING_CURSE
            }, new int[]{1, 1, 1, 1, 1}));
            player.getInventory().setBoots(createEnchantedArmor(Material.IRON_BOOTS, new Enchantment[]{
                    Enchantment.PROTECTION_FALL, Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.MENDING, Enchantment.DURABILITY, Enchantment.VANISHING_CURSE, Enchantment.BINDING_CURSE
            }, new int[]{1, 1, 1, 1, 1, 1}));

            // Equip shield
            ItemStack shield = new ItemStack(Material.SHIELD);
            player.getInventory().setItemInOffHand(shield);

            // Give player their basic tools
            player.getInventory().addItem(createEnchantedItem(Material.IRON_SWORD, new Enchantment[]{Enchantment.DAMAGE_ALL, Enchantment.KNOCKBACK, Enchantment.FIRE_ASPECT, Enchantment.LOOT_BONUS_MOBS, Enchantment.SWEEPING_EDGE}, new int[]{1, 1, 1, 1, 1}));
            player.getInventory().addItem(createEnchantedItem(Material.IRON_PICKAXE, new Enchantment[]{Enchantment.DIG_SPEED, Enchantment.LOOT_BONUS_BLOCKS, Enchantment.DURABILITY}, new int[]{1, 1, 1}));
            player.getInventory().addItem(createEnchantedItem(Material.IRON_AXE, new Enchantment[]{Enchantment.DIG_SPEED, Enchantment.DURABILITY, Enchantment.MENDING, Enchantment.SILK_TOUCH}, new int[]{1, 1, 1, 1}));
            player.getInventory().addItem(createEnchantedItem(Material.IRON_SHOVEL, new Enchantment[]{Enchantment.DIG_SPEED, Enchantment.DURABILITY, Enchantment.MENDING}, new int[]{1, 1, 1}));
            player.getInventory().addItem(createEnchantedItem(Material.BOW, new Enchantment[]{Enchantment.ARROW_INFINITE, Enchantment.DURABILITY, Enchantment.ARROW_DAMAGE, Enchantment.ARROW_FIRE, Enchantment.ARROW_KNOCKBACK}, new int[]{1, 3, 5, 1, 2}));


            // Add additional items
            player.getInventory().addItem(new ItemStack(Material.ORANGE_BED));
            player.getInventory().addItem(new ItemStack(Material.CRAFTING_TABLE, 1));
            player.getInventory().addItem(new ItemStack(Material.JUNGLE_CHEST_BOAT, 1));
            player.getInventory().addItem(new ItemStack(Material.ARROW, 64));
            //endregion
        }
    }

    private ItemStack createFireworkRocket() {
        ItemStack firework = new ItemStack(Material.FIREWORK_ROCKET, 64);
        FireworkMeta fireworkMeta = (FireworkMeta) firework.getItemMeta();

        if (fireworkMeta != null) {
            // Set the flight duration to level 3 (no effects needed for elytra boosting)
            fireworkMeta.setPower(3);
            firework.setItemMeta(fireworkMeta);
        }

        return firework;
    }
    private ItemStack createEnchantedElytra(Enchantment[] enchantments, int[] levels) {
        // Create an Elytra item
        ItemStack elytra = new ItemStack(Material.ELYTRA);

        // Apply enchantments
        ItemMeta meta = elytra.getItemMeta();
        for (int i = 0; i < enchantments.length; i++) {
            meta.addEnchant(enchantments[i], levels[i], true);
        }
        elytra.setItemMeta(meta);

        return elytra;
    }

    // Utility method to create enchanted armor
    private ItemStack createEnchantedArmor(Material material, Enchantment[] enchantments, int[] levels) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        for (int i = 0; i < enchantments.length; i++) {
            meta.addEnchant(enchantments[i], levels[i], true);
        }
        item.setItemMeta(meta);
        return item;
    }

    // Utility method to create an enchanted item
    private ItemStack createEnchantedItem(Material material, Enchantment[] enchantments, int[] levels) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        for (int i = 0; i < enchantments.length; i++) {
            meta.addEnchant(enchantments[i], levels[i], true);
        }
        item.setItemMeta(meta);
        return item;
    }

    // Settings items for player bingo cards
    public ItemStack createSpyglass() {
        ItemStack spyglass = new ItemStack(Material.SPYGLASS);
        ItemMeta meta = spyglass.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "查看玩家卡片");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "偷偷看一眼其他玩家的卡片！"));
        spyglass.setItemMeta(meta);
        return spyglass;
    }

    public void topUpFirstFireworkRocketsStack(Player player) {

        if (ultimateBingo.currentLoadoutType == 3) {

            PlayerInventory inventory = player.getInventory();
            ItemStack[] items = inventory.getContents();
            boolean rocketsFound = false;

            // Scan inventory for the first stack of rockets and top it up to 64 if found
            for (ItemStack item : items) {
                if (item != null && item.getType() == Material.FIREWORK_ROCKET) {
                    item.setAmount(64);  // Set the count to 64
                    rocketsFound = true;
                    break;  // Stop after finding the first stack
                }
            }

            // Player has no rockets, let's give them a stack
            if (!rocketsFound) {
                ItemStack fireworkStack = createFireworkRocket();
                player.getInventory().addItem(fireworkStack);
            }
        }
    }

    //endregion

    //region Teleporting functionality

    // Method to scatter players and ensure they face the center horizontally
    public void safeScatterPlayers(List<Player> players, Location center, int radius) {
        if (center != null) {
            World world = center.getWorld();
            Random random = new Random();

            for (Player player : players) {

                if (ultimateBingo.bingoFunctions.isActivePlayer(player)) {

                    Location safeLocation = findSafeLocation(world, center, radius, random, 20);
                    player.teleport(safeLocation);
                    setFacing(player, center);
                }
            }
        }
    }

    // Find a safe location around a given center within a specified radius
    public Location findSafeLocation(World world, Location center, int radius, Random random, int attempts) {
        for (int i = 0; i < attempts; i++) { // Attempt up to 10 times to find a safe location
            int dx = random.nextInt(radius * 2) - radius;
            int dz = random.nextInt(radius * 2) - radius;
            Location loc = center.clone().add(dx, 0, dz);
            loc = world.getHighestBlockAt(loc).getLocation().add(0, 1, 0); // Adjust to one above the highest solid block

            if (isSafeLocation(loc)) {
                return loc;
            }
        }
        return center; // Return the center if no safe location is found
    }

    // Determine if a location is safe for teleportation
    private boolean isSafeLocation(Location location) {
        Material block = location.getBlock().getType();
        Material below = location.clone().add(0, -1, 0).getBlock().getType();
        return below.isSolid() && block == Material.AIR; // Ensure solid ground and free space above
    }

    // Set the facing of the player towards the center point horizontally
    private void setFacing(Player player, Location center) {
        Location playerLoc = player.getLocation();
        double dx = center.getX() - playerLoc.getX();
        double dz = center.getZ() - playerLoc.getZ();
        float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90;
        playerLoc.setYaw(yaw);
        playerLoc.setPitch(0); // Ensure players look straight ahead, not up or down
        player.teleport(playerLoc);
    }

    //endregion

    //region Random world teleport

    /**
     * Teleports the player to a random ground location in their current world.
     * Uses Paper's async chunk loading to avoid blocking the main thread.
     * Falls back to sync loading if async is unavailable (non-Paper server).
     */
    public boolean teleportToRandomGround(Player player) {
        World world = player.getWorld();
        Random rng = new Random();

        double baseRadius = ultimateBingo.teleportRadius;
        if (baseRadius <= 0) {
            baseRadius = world.getWorldBorder().getSize() / 2.0;
            if (baseRadius <= 0) baseRadius = 5000;
        }
        if (baseRadius > 8000) baseRadius = 8000;

        Location center = world.getSpawnLocation();

        // Progressively shrink radius if no safe spot found
        for (double radius = baseRadius; radius >= 250; radius /= 2) {
            for (int attempt = 0; attempt < 8; attempt++) {
                double angle = rng.nextDouble() * 2 * Math.PI;
                double dist = radius * Math.sqrt(rng.nextDouble());
                int dx = center.getBlockX() + (int)(Math.cos(angle) * dist);
                int dz = center.getBlockZ() + (int)(Math.sin(angle) * dist);
                int cx = dx >> 4, cz = dz >> 4;

                if (world.isChunkLoaded(cx, cz)) {
                    if (teleportTo(player, world, dx, dz)) return true;
                    continue;
                }

                if (loadChunkAsync(player, world, dx, dz, cx, cz)) return true;
            }

            // Next tier — let the player know we're still trying
            if (radius > 500) {
                player.sendMessage(ChatColor.YELLOW + "远处未找到位置，正在缩小范围重试……");
            }
        }

        // Last resort: spawn
        Location spawn = world.getSpawnLocation();
        spawn = world.getHighestBlockAt(spawn).getLocation().add(0.5, 1, 0);
        player.teleport(spawn);
        player.sendMessage(ChatColor.YELLOW + "已传送至世界出生点（所有范围均未找到合适位置）");
        return true;
    }

    /**
     * Attempts to load a chunk via Paper's async API using reflection.
     * @return true if async load was initiated, false if unavailable
     */
    private boolean loadChunkAsync(Player player, World world,
                                    int dx, int dz, int cx, int cz) {
        try {
            java.lang.reflect.Method method = world.getClass()
                    .getMethod("getChunkAtAsync", int.class, int.class);
            @SuppressWarnings("unchecked")
            java.util.concurrent.CompletableFuture<org.bukkit.Chunk> future =
                    (java.util.concurrent.CompletableFuture<org.bukkit.Chunk>)
                    method.invoke(world, cx, cz);

            if (future != null) {
                future.thenAccept(chunk -> {
                    // Chunk loaded — teleport on main thread
                    Bukkit.getScheduler().runTask(ultimateBingo, () -> {
                        if (player.isOnline()) {
                            teleportTo(player, world, dx, dz);
                        }
                    });
                });
                player.sendMessage(ChatColor.YELLOW + "正在传送……");
                return true;
            }
        } catch (Exception ignored) {
            // Paper API not available, will fall back to sync
        }
        return false;
    }

    /**
     * Actually perform the teleport: check safety, pre-warm surroundings, then teleport.
     */
    private boolean teleportTo(Player player, World world, int dx, int dz) {
        // Ensure the target chunk is loaded
        int cx = dx >> 4, cz = dz >> 4;
        if (!world.isChunkLoaded(cx, cz)) {
            world.getChunkAt(cx, cz);
        }

        // Pre-warm a 3×3 chunk area around the target to avoid post-teleport freeze
        for (int ox = -1; ox <= 1; ox++) {
            for (int oz = -1; oz <= 1; oz++) {
                int pcx = cx + ox, pcz = cz + oz;
                if (!world.isChunkLoaded(pcx, pcz)) {
                    world.getChunkAt(pcx, pcz);
                }
            }
        }

        int y = world.getHighestBlockYAt(dx, dz);
        Material topBlock = world.getBlockAt(dx, y, dz).getType();
        if (topBlock == Material.WATER || topBlock == Material.LAVA) return false;

        Location loc = new Location(world, dx + 0.5, y + 1, dz + 0.5);
        if (!isSafeLocation(loc)) return false;

        loc.setYaw((float)(Math.random() * 360));
        loc.setPitch(0);
        player.teleport(loc);
        player.sendMessage(ChatColor.GREEN + "已随机传送至 X:" + dx + " Z:" + dz);
        return true;
    }

    //endregion

    //region Game timers

    public void setGameTimer() {

        // Get a reference to the plugin for us to schedule tasks
        UltimateBingo plugin = UltimateBingo.getInstance();

        if (ultimateBingo.gameTime == 0) {
            // No game timer has been set.
            setGameTimerTasks(plugin, 20, 0.25f);
            setGameTimerTasks(plugin, 40, 0.30f);
            setGameTimerTasks(plugin, 60, 0.35f);

        } else {
            // This game has a game timer, no perks will be given
            // We'll add a timer to end the game and send some warnings prior to ending
            setGameCountdownTask(plugin, ultimateBingo.gameTime);
        }

    }

    public void setGameCountdownTask(Plugin plugin, int minutes) {

        // Calculate the amount of ticks needed
        int threeMinutesLeft = (minutes - 3) * 60 * 20;
        int twoMinutesLeft = (minutes - 2) * 60 * 20;
        int oneMinuteLeft = (minutes - 1) * 60 * 20;
        int gameLength = minutes * 60 * 20;

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GREEN + "游戏将在 3 分钟后结束！");


            for (Player p : Bukkit.getOnlinePlayers()) {

                if (ultimateBingo.bingoFunctions.isActivePlayer(p)) {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);
                }
            }
        }, threeMinutesLeft);

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GREEN + "游戏将在 2 分钟后结束！");
            for (Player p : Bukkit.getOnlinePlayers()) {

                if (ultimateBingo.bingoFunctions.isActivePlayer(p)) {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);
                }
            }
        }, twoMinutesLeft);

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GREEN + "游戏将在 1 分钟后结束！");
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (ultimateBingo.bingoFunctions.isActivePlayer(p)) {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);
                }
            }
        }, oneMinuteLeft);

        // Loop for each of the last 5 seconds
        for (int i = 5; i >= 1; i--) {
            final int finalI = i;  // Create a final variable to use inside the lambda
            int delay = gameLength - (i * 20);  // Calculate delay in ticks (20 ticks per second)
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GREEN + "游戏将在 " + finalI + " 秒后结束！");
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // Play a tone sound effect at the player's location

                    if (ultimateBingo.bingoFunctions.isActivePlayer(player)) {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
                    }
                }
            }, delay);
        }


        // End the game
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {

            ultimateBingo.bingoCommand.bingoGameOver();

        }, gameLength);

    }

    public void setGameTimerTasks(Plugin plugin, int minutes, float walkSpeed) {

        // Calculate the amount of ticks needed
        int delayTicks = minutes * 60 * 20;

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.GREEN + "游戏已运行 " + minutes + " 分钟.");
            ultimateBingo.bingoFunctions.broadcastMessageToBingoPlayers(ChatColor.YELLOW + "你获得了加速增益！");
            for (Player p : Bukkit.getOnlinePlayers()) {

                if (ultimateBingo.bingoFunctions.isActivePlayer(p)) {

                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);

                    // Increse their walk speed
                    p.setWalkSpeed(walkSpeed);
                }
            }
        }, delayTicks); // Delay of 18,000 ticks, equivalent to 15 minutes

    }

    // Format duration from minutes to a readable format
    public String formatAndShowGameDuration(long durationMillis) {
        // Convert milliseconds to minutes
        long totalMinutes = durationMillis / 1000 / 60;

        // Determine the appropriate format based on the total minutes
        if (totalMinutes < 60) {
            return totalMinutes + " 分钟";
        } else {
            long hours = totalMinutes / 60;
            long minutes = totalMinutes % 60;
            return String.format("%d 小时 %d 分钟", hours, minutes);
        }
    }

    //endregion

    //region Bingo configuration functions

    public String validateOrDefault(String input, String[] validOptions, String defaultOption) {
        input = input.toLowerCase();
        for (String option : validOptions) {
            if (option.equals(input)) {
                return option;
            }
        }
        return validOptions[random.nextInt(validOptions.length)];
    }

    public int validateOrDefaultInt(int input, int range, int defaultOption) {
        try {
            if (input >= 0 && input < range) {
                return input;
            }
        } catch (NumberFormatException e) {
            // Log or handle error if necessary
        }
        return random.nextInt(range);
    }

    public boolean validateOrDefaultBoolean(String input, String[] validOptions, boolean defaultOption) {
        input = input.toLowerCase();
        if (validOptions[0].equals(input)) {
            return true;
        } else if (validOptions[1].equals(input)) {
            return false;
        }
        return random.nextBoolean();
    }

    //endregion

    //region Player notifications

    public boolean isActivePlayer(Player player) {

        boolean isActivePlayer = true;

        // Check if multi world bingo is enabled and they're in the bingo world
        if (ultimateBingo.multiWorldServer && !player.getWorld().getName().equalsIgnoreCase(ultimateBingo.bingoWorld.toLowerCase())) {
            isActivePlayer = false;
        }

        if (isActivePlayer || !ultimateBingo.multiWorldServer) {
            isActivePlayer = true;
        }

        return isActivePlayer;

    }

    /**
     * Check if a player can interact with bingo card GUIs.
     * True if they're an active player in the bingo world, OR if they're
     * reviewing a card in the hub after a game.
     */
    public boolean canInteractWithCard(Player player) {
        if (isActivePlayer(player)) return true;
        return ultimateBingo.isHubModeActive()
                && ultimateBingo.hubRegionListener != null
                && ultimateBingo.hubRegionListener.isTracked(player.getUniqueId());
    }

    public int countActivePlayers() {
        int playerCount = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Check if the player has a generated bingo card
            if (ultimateBingo.bingoManager.checkHasBingoCard(player)) {
                playerCount++;
            }
        }
        return playerCount;
    }

    public void broadcastMessageToBingoPlayers(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isActivePlayer(player)) {
                player.sendMessage(message);
            }
        }
    }



    //endregion

    //region Potion functionality

    public void applyRandomNegativePotionToOtherPlayers(Player excludedPlayer, int durationInSeconds) {
        // Define a list of negative potion effects
        List<PotionEffectType> negativePotions = Arrays.asList(
                PotionEffectType.SLOW,
                PotionEffectType.SLOW_FALLING,
                PotionEffectType.BLINDNESS,
                PotionEffectType.SLOW_DIGGING,
                PotionEffectType.HUNGER,
                PotionEffectType.POISON,
                PotionEffectType.LEVITATION

        );

        // Random instance to select a random potion
        Random random = new Random();

        // Pick a random potion effect from the list (applies the same effect to all players)
        PotionEffectType randomPotion = negativePotions.get(random.nextInt(negativePotions.size()));

        // Convert the potion name to a friendly format
        String friendlyPotionName = getPotionName(randomPotion).toUpperCase();

        // Loop through all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Exclude the passed player and only process active players
            if (!player.equals(excludedPlayer) && ultimateBingo.bingoFunctions.isActivePlayer(player)) {

                // Remove all existing potion effects from the player
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    player.removePotionEffect(effect.getType());
                }

                // Apply the selected potion effect for the given duration (in ticks, 20 ticks per second)
                player.addPotionEffect(new PotionEffect(randomPotion, durationInSeconds * 20, 0));

                // Create the subtitle message
                String subtitle = ChatColor.RED + friendlyPotionName + " 持续 " + durationInSeconds + " 秒后结束！";

                // Send the title (empty) and subtitle to the player
                player.sendTitle("", subtitle, 10, 70, 20);  // 10 ticks fade in, 70 ticks stay, 20 ticks fade out

                // Play the potion break sound to the player
                player.playSound(player.getLocation(), Sound.ENTITY_SPLASH_POTION_BREAK, 1.0f, 1.0f);

            }

        }

        // Create the subtitle message
        String subtitle = ChatColor.GREEN + friendlyPotionName + " 持续 " + durationInSeconds + " 秒后结束！";

        // Send the title (empty) and subtitle to the player
        excludedPlayer.sendTitle("", subtitle, 10, 70, 20);  // 10 ticks fade in, 70 ticks stay, 20 ticks fade out
    }

    /**
     * Clears all advancements for every active player in the bingo world.
     * Works with Multiverse per-world advancement tracking.
     */
    public void clearAllAdvancements() {
        int cleared = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isActivePlayer(player)) continue;

            try {
                Iterator<org.bukkit.advancement.Advancement> it =
                    Bukkit.getServer().advancementIterator();
                while (it.hasNext()) {
                    org.bukkit.advancement.Advancement adv = it.next();
                    org.bukkit.advancement.AdvancementProgress progress =
                        player.getAdvancementProgress(adv);
                    for (String criteria : progress.getAwardedCriteria()) {
                        progress.revokeCriteria(criteria);
                    }
                }
                cleared++;
            } catch (Exception ignored) {
                // Some advancements may fail to revoke, skip
            }
        }
        if (cleared > 0 && ultimateBingo.consoleLogs) {
            ultimateBingo.getLogger().info("已清空 " + cleared + " 名玩家的成就进度。");
        }
    }

    //endregion
    //region Player Tracking for Games

    // Method to store a UUID in the map
    public void addPlayer(UUID playerId) {
        playerMap.put(playerId, true);
    }

    // Method to clear the map
    public void clearPlayers() {
        playerMap.clear();
    }

    // Method to check if a UUID is in the map
    public boolean isPlayerInGame(UUID playerId) {
        return playerMap.containsKey(playerId);
    }

    //endregion

    //region Team functionality

    private Random teamRandom = new Random();
    private HashMap<UUID, Boolean> activePlayersMap = new HashMap<>();
    private HashMap<UUID, String> playerTeamsMap = new HashMap<>();

    // Store a reference to all online players
    // Manual team pre-assignment (from team signs)
    public void setManualTeam(UUID playerId, String team) {
        manualTeamAssignments.put(playerId, team.toLowerCase());
    }

    public String getManualTeam(UUID playerId) {
        return manualTeamAssignments.get(playerId);
    }

    public void clearManualTeams() {
        manualTeamAssignments.clear();
    }

    public void assignTeams() {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        List<Player> redTeam = new ArrayList<>();
        List<Player> yellowTeam = new ArrayList<>();
        List<Player> blueTeam = new ArrayList<>();
        List<Player> unassignedPlayers = new ArrayList<>();

        // Display initial messages
        onlinePlayers.forEach(player -> {
            boolean activePlayer = true;
            // Check if multi world bingo is enabled and they're in the bingo world
            if (ultimateBingo.multiWorldServer) {
                if (!player.getWorld().getName().equalsIgnoreCase(ultimateBingo.bingoWorld.toLowerCase())) {
                    activePlayer = false;
                }
            }

            if (activePlayer) {
                // Priority 1: Check if player manually picked a team via team sign
                String manualTeam = getManualTeam(player.getUniqueId());
                if (manualTeam != null) {
                    switch (manualTeam) {
                        case "blue":
                            blueTeam.add(player);
                            playerTeamsMap.put(player.getUniqueId(), "blue");
                            break;
                        case "red":
                            redTeam.add(player);
                            playerTeamsMap.put(player.getUniqueId(), "red");
                            break;
                        case "yellow":
                            yellowTeam.add(player);
                            playerTeamsMap.put(player.getUniqueId(), "yellow");
                            break;
                        default:
                            unassignedPlayers.add(player);
                    }
                    return; // Skip block check for this player
                }

                // Priority 2: Check the block below the player
                Location locationBelow = player.getLocation().subtract(0, 1, 0);
                Material blockStandingOn = locationBelow.getBlock().getType();
                switch (blockStandingOn) {
                    case BLUE_WOOL:
                        blueTeam.add(player);
                        playerTeamsMap.put(player.getUniqueId(), "blue");
                        break;
                    case RED_WOOL:
                        redTeam.add(player);
                        playerTeamsMap.put(player.getUniqueId(), "red");
                        break;
                    case YELLOW_WOOL:
                        yellowTeam.add(player);
                        playerTeamsMap.put(player.getUniqueId(), "yellow");
                        break;
                    default:
                        unassignedPlayers.add(player);
                }
            }
        });

        distributeUnassignedPlayers(unassignedPlayers, redTeam, yellowTeam, blueTeam);

        // Clear manual assignments after use
        clearManualTeams();

    }

    private void distributeUnassignedPlayers(List<Player> unassignedPlayers, List<Player> redTeam, List<Player> yellowTeam, List<Player> blueTeam) {
        for (Player player : unassignedPlayers) {
            int redTeamSize = redTeam.size();
            int yellowTeamSize = yellowTeam.size();
            int blueTeamSize = blueTeam.size();

            if (redTeamSize <= yellowTeamSize && redTeamSize <= blueTeamSize) {
                redTeam.add(player);
                playerTeamsMap.put(player.getUniqueId(), "red");
            } else if (yellowTeamSize <= redTeamSize && yellowTeamSize <= blueTeamSize) {
                yellowTeam.add(player);
                playerTeamsMap.put(player.getUniqueId(), "yellow");
            } else {
                blueTeam.add(player);
                playerTeamsMap.put(player.getUniqueId(), "blue");
            }
        }
    }

    // Method to assign a player to an active team
    public void assignPlayerToActiveTeam(Player player) {
        // Priority 1: manual team assignment (from GUI or sign)
        String manualTeam = getManualTeam(player.getUniqueId());
        if (manualTeam != null) {
            playerTeamsMap.put(player.getUniqueId(), manualTeam);
            String teamName = switch (manualTeam) {
                case "red" -> "红";
                case "blue" -> "蓝";
                case "yellow" -> "黄";
                default -> manualTeam;
            };
            player.sendMessage(net.md_5.bungee.api.ChatColor.GREEN + "你已被分配到 " + teamName + " 队！");
            notifyActivePlayers(player);
            return;
        }

        // Priority 2: balance among existing teams
        Map<String, Integer> teamSizes = new LinkedHashMap<>();
        teamSizes.put("red", 0);
        teamSizes.put("yellow", 0);
        teamSizes.put("blue", 0);

        playerTeamsMap.values().forEach(team -> {
            if (teamSizes.containsKey(team)) {
                teamSizes.put(team, teamSizes.get(team) + 1);
            }
        });

        teamSizes.entrySet().removeIf(entry -> entry.getValue() == 0);

        if (teamSizes.isEmpty()) {
            // No existing players in any team, info

            player.sendMessage(net.md_5.bungee.api.ChatColor.RED + "没有可加入的活跃队伍。");
            return;
        }

        String teamToJoin = teamSizes.keySet().stream()
                .min(Comparator.comparingInt(teamSizes::get))
                .orElseThrow();

        playerTeamsMap.put(player.getUniqueId(), teamToJoin);
        String teamNameA = switch (teamToJoin.toLowerCase()) {
            case "red" -> "红";
            case "blue" -> "蓝";
            case "yellow" -> "黄";
            default -> teamToJoin;
        };
        player.sendMessage(net.md_5.bungee.api.ChatColor.GREEN + "你已被分配到 " + teamNameA + " 队！");
        notifyActivePlayers(player);
    }

    // Method to get the team of a player
    public String getTeam(Player player) {
        return playerTeamsMap.getOrDefault(player.getUniqueId(), "无");
    }

    // Method to get the team inventory of a player
    public Inventory getTeamInventory(Player player) {
        String team = getTeam(player);
        return switch (team.toLowerCase()) {
            case "blue" -> ultimateBingo.blueTeamInventory;
            case "red" -> ultimateBingo.redTeamInventory;
            case "yellow" -> ultimateBingo.yellowTeamInventory;
            default -> null; // or some default inventory
        };
    }

    // Method to send a message to all active players with the list of active players and their teams
    public void notifyActivePlayers(Player playerToSend) {
        List<Player> activePlayers = new ArrayList<>();
        StringBuilder messageBuilder = new StringBuilder("游戏中的活跃玩家：\n");

        // Build the list of active players and their teams
        Bukkit.getOnlinePlayers().forEach(player -> {
            String team = playerTeamsMap.get(player.getUniqueId());
            if (team != null) {
                net.md_5.bungee.api.ChatColor color;
                switch (team.toLowerCase()) {
                    case "blue":
                        color = net.md_5.bungee.api.ChatColor.BLUE;
                        break;
                    case "red":
                        color = net.md_5.bungee.api.ChatColor.RED;
                        break;
                    case "yellow":
                        color = net.md_5.bungee.api.ChatColor.YELLOW;
                        break;
                    default:
                        color = net.md_5.bungee.api.ChatColor.WHITE;
                        break;
                }
                messageBuilder.append(color).append(player.getName()).append(net.md_5.bungee.api.ChatColor.RESET).append(", ");
                activePlayers.add(player);
            }
        });

        // Trim last comma and space
        if (messageBuilder.length() > 2) {
            messageBuilder.setLength(messageBuilder.length() - 2);
        }

        String message = messageBuilder.toString();
        playerToSend.sendMessage(message);
    }

    public boolean isRedTeamNotEmpty() {
        return playerTeamsMap.containsValue("red");
    }

    public boolean isYellowTeamNotEmpty() {
        return playerTeamsMap.containsValue("yellow");
    }

    public boolean isBlueTeamNotEmpty() {
        return playerTeamsMap.containsValue("blue");

    }

    public String getRedTeamPlayerNames() {
        return getPlayerNamesByTeam("red");
    }

    public String getYellowTeamPlayerNames() {
        return getPlayerNamesByTeam("yellow");
    }

    public String getBlueTeamPlayerNames() {
        return getPlayerNamesByTeam("blue");
    }

    private String getPlayerNamesByTeam(String teamColor) {
        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> teamColor.equals(playerTeamsMap.get(player.getUniqueId())))
                .map(Player::getName)
                .collect(Collectors.joining(", "));
    }

    public void copyInventoryContents(Inventory source, Inventory destination) {
        for (int i = 0; i < source.getSize(); i++) {
            destination.setItem(i, source.getItem(i) == null ? null : source.getItem(i).clone());
        }
    }

    public String toggleGameMode() {
        switch (ultimateBingo.gameMode.toLowerCase()) {
            case "traditional":
                ultimateBingo.gameMode = "speedrun";
                break;
            case "speedrun":
                ultimateBingo.gameMode = "brewdash";
                break;
            case "brewdash":
                ultimateBingo.gameMode = "group";
                break;
            case "group":
                ultimateBingo.gameMode = "teams";
                break;
            case "teams":
                ultimateBingo.gameMode = "shuffle";
                break;
            case "shuffle":
                ultimateBingo.gameMode = "random";
                break;
            case "random":
                ultimateBingo.gameMode = "traditional";
                break;

        }

        return ultimateBingo.gameMode;
    }

    public String toggleDifficulty() {
        switch (ultimateBingo.difficulty.toLowerCase()) {
            case "easy":
                ultimateBingo.difficulty = "normal";
                break;
            case "normal":
                ultimateBingo.difficulty = "hard";
                break;
            case "hard":
                ultimateBingo.difficulty = "random";
                break;
            case "random":
                ultimateBingo.difficulty = "easy";
                break;
        }

        return ultimateBingo.difficulty;
    }

    public String toggleCardSize() {
        switch (ultimateBingo.cardSize.toLowerCase()) {
            case "small":
                ultimateBingo.cardSize = "medium";
                break;
            case "medium":
                ultimateBingo.cardSize = "large";
                break;
            case "large":
                ultimateBingo.cardSize = "random";
                break;
            case "random":
                ultimateBingo.cardSize = "small";
                break;
        }

        return ultimateBingo.cardSize;
    }

    public int toggleLoadout() {
        switch (ultimateBingo.loadoutType) {
            case 0:
                ultimateBingo.loadoutType = 1;
                break;
            case 1:
                ultimateBingo.loadoutType = 2;
                break;
            case 2:
                ultimateBingo.loadoutType = 3;
                break;
            case 3:
                ultimateBingo.loadoutType = 4;
                break;
            case 4:
                ultimateBingo.loadoutType = 50;
                break;
            case 50:
                ultimateBingo.loadoutType = 0;
                break;

        }

        return ultimateBingo.loadoutType;
    }

    public String toggleFullCard() {

        switch (ultimateBingo.fullCard) {
            case "full card":
                ultimateBingo.fullCard = "single row";
                break;
            case "single row":
                ultimateBingo.fullCard = "random";
                break;
            case "random":
                ultimateBingo.fullCard = "full card";
                break;
        }

        return ultimateBingo.fullCard;
    }

    public String toggleUnique() {
        switch (ultimateBingo.uniqueCard) {
            case "unique":
                ultimateBingo.uniqueCard = "identical";
                break;
            case "identical":
                ultimateBingo.uniqueCard = "random";
                break;
            case "random":
                ultimateBingo.uniqueCard = "unique";
                break;
        }

        return ultimateBingo.uniqueCard;
    }

    public String toggleReveal() {
        switch (ultimateBingo.revealCards) {
            case "enabled":
                ultimateBingo.revealCards = "disabled";
                break;
            case "disabled":
                ultimateBingo.revealCards = "random";
                break;
            case "random":
                ultimateBingo.revealCards = "enabled";
                break;
        }

        return ultimateBingo.revealCards;
    }

    public int toggleTimeLimit() {
        switch (ultimateBingo.gameTime) {
            case 0:
                ultimateBingo.gameTime = 5;
                break;
            case 5:
                ultimateBingo.gameTime = 10;
                break;
            case 10:
                ultimateBingo.gameTime = 15;
                break;
            case 15:
                ultimateBingo.gameTime = 20;
                break;
            case 20:
                ultimateBingo.gameTime = 30;
                break;
            case 30:
                ultimateBingo.gameTime = 40;
                break;
            case 40:
                ultimateBingo.gameTime = 50;
                break;
            case 50:
                ultimateBingo.gameTime = 60;
                break;
            case 60:
                ultimateBingo.gameTime = 0;
                break;
        }

        return ultimateBingo.gameTime;
    }

    //endregion

    //region Sign Controls

    public void updateSetting(String setting, Player player) {

        switch (setting.toLowerCase()) {

            case "gamemode":
                updateSign(setting, toggleGameMode());
                break;
            case "difficulty":
                updateSign(setting, toggleDifficulty());
                break;
            case "cardsize":
                updateSign(setting, toggleCardSize());
                break;
            case "loadout":
                updateLoadoutSign(setting, String.valueOf(toggleLoadout()));
                break;
            case "revealcards":
                updateSign(setting, toggleReveal());
                break;
            case "wincondition":
                updateSign(setting, toggleFullCard());
                break;
            case "cardtype":
                updateSign(setting, toggleUnique());
                break;
            case "timelimit":
                updateTimeLimitSign(setting, String.valueOf(toggleTimeLimit()));
                break;
        }

        // Persist the change so it survives server reboots
        ultimateBingo.configFile.saveConfig();

    }

    public void updateLoadoutSign(String setting, String textToUpdate) {
        if (!signLocations.containsKey(setting)) return;
        Location loc = signLocations.get(setting);
        Block block = loc.getBlock();
        if (!(block.getState() instanceof Sign)) return;

        String textMode = null;
        if (textToUpdate.equals("0")) {
            textMode = "裸装";
        } else if (textToUpdate.equals("1")) {
            textMode = "新手装备";
        } else if (textToUpdate.equals("2")) {
            textMode = "船只装备";
        } else if (textToUpdate.equals("3")) {
            textMode = "飞行装备";
        } else if (textToUpdate.equals("4")) {
            textMode = "弓箭装备";
        } else if (textToUpdate.equals("50")) {
            textMode = "随机";
        }

        if (textMode != null) {
            Sign sign = (Sign) block.getState();
            sign.setLine(1, "§6" + setting.toUpperCase());
            sign.setLine(2, "§f" + textMode.toUpperCase());

            sign.setColor(DyeColor.WHITE);
            sign.setGlowingText(true);

            sign.update();
        }
    }

    public void updateSign(String setting, String textToUpdate) {
        if (!signLocations.containsKey(setting)) return;
        Location loc = signLocations.get(setting);
        Block block = loc.getBlock();
        if (!(block.getState() instanceof Sign)) return;

        if (textToUpdate != null) {
            Sign sign = (Sign) block.getState();

            if (setting.equalsIgnoreCase("WINCONDITION")) {
                sign.setLine(1, "§6" + "胜利条件");
            } else if (setting.equalsIgnoreCase("GAMEMODE")) {
                sign.setLine(1, "§6" + "游戏模式");
            } else if (setting.equalsIgnoreCase("CARDSIZE")) {
                sign.setLine(1, "§6" + "卡片大小");
            } else if (setting.equalsIgnoreCase("REVEALCARDS")) {
                sign.setLine(1, "§6" + "公开卡片");
            } else if (setting.equalsIgnoreCase("CARDTYPE")) {
                sign.setLine(1, "§6" + "卡片类型");
            } else {
                sign.setLine(1, "§6" + setting.toUpperCase());
            }
            sign.setLine(2, "§f" + textToUpdate.toUpperCase());

            sign.setGlowingText(true);
            sign.setColor(DyeColor.WHITE);
            sign.update();
        }
    }

    public void updateTimeLimitSign(String setting, String textToUpdate) {
        if (!signLocations.containsKey(setting)) return;
        Location loc = signLocations.get(setting);
        Block block = loc.getBlock();
        if (!(block.getState() instanceof Sign)) return;

        if (textToUpdate != null) {
            Sign sign = (Sign) block.getState();

            if (setting.equalsIgnoreCase("WINCONDITION")) {
                sign.setLine(1, "§6" + "胜利条件");
            } else if (setting.equalsIgnoreCase("GAMEMODE")) {
                sign.setLine(1, "§6" + "游戏模式");
            } else if (setting.equalsIgnoreCase("CARDSIZE")) {
                sign.setLine(1, "§6" + "卡片大小");
            } else if (setting.equalsIgnoreCase("REVEALCARDS")) {
                sign.setLine(1, "§6" + "公开卡片");
            } else if (setting.equalsIgnoreCase("CARDTYPE")) {
                sign.setLine(1, "§6" + "卡片类型");
            } else if (setting.equalsIgnoreCase("TIMELIMIT")) {
                sign.setLine(1, "§6" + "时间限制");
            } else {
                sign.setLine(1, "§6" + "时间限制");
            }

            if (textToUpdate.equalsIgnoreCase("0")) {
                sign.setLine(2, "§f" + "无限制");
            } else {
                sign.setLine(2, "§f" + textToUpdate.toUpperCase() + " 分钟");
            }


            sign.setGlowingText(true);
            sign.setColor(DyeColor.WHITE);
            sign.update();
        }
    }


    public String getSettingValue(String setting) {
        return switch (setting.toLowerCase()) {
            case "gamemode" -> ultimateBingo.gameMode;
            case "difficulty" -> ultimateBingo.difficulty;
            case "cardsize" -> ultimateBingo.cardSize;
            case "loadout" -> String.valueOf(ultimateBingo.loadoutType);
            case "revealcards" -> ultimateBingo.revealCards;
            case "wincondition" -> ultimateBingo.fullCard;
            case "cardtype" -> ultimateBingo.uniqueCard;
            case "timelimit" -> ultimateBingo.gameTime == 0 ? "无限制" : ultimateBingo.gameTime + " 分钟";
            default -> "";
        };
    }

    public void updateAllSigns() {
        for (Map.Entry<String, Location> entry : signLocations.entrySet()) {
            String setting = entry.getKey();
            Location loc = entry.getValue();
            Block block = loc.getBlock();

            // Skip if block is not a sign
            if (!(block.getState() instanceof Sign)) {
                continue;
            }

            if (setting.equalsIgnoreCase("loadout")) {
                updateLoadoutSign(setting, getSettingValue(setting));
            } else {
                // Get the correct setting value and update the sign
                updateSign(setting, getSettingValue(setting));
            }
        }
    }

    public String locationToString(Location loc) {
        return loc == null ? "" : loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    public void loadSignData() {
        if (!configFile.exists()) {
            saveSignData();
            return;
        }

        if (config.contains("signs")) {
            for (String key : config.getConfigurationSection("signs").getKeys(false)) {
                Location loc = parseLocation(config.getString("signs." + key));
                if (loc != null) {
                    signLocations.put(key, loc);
                }
            }
        }

        // Load button location correctly
        if (config.contains("button.startbutton")) {
            startButtonLocation = parseLocation(config.getString("button.startbutton"));
            ultimateBingo.getLogger().info("已加载开始按钮位置：" + startButtonLocation);
        } else {
            ultimateBingo.getLogger().warning("配置中未找到开始按钮！");
        }
    }

    public void reloadSignDataSilent() {
        if (!configFile.exists()) {
            return; // If the file doesn't exist, do nothing silently
        }

        // Clear current in-memory sign locations
        signLocations.clear();

        // Load updated sign locations
        if (config.contains("signs")) {
            for (String key : config.getConfigurationSection("signs").getKeys(false)) {
                Location loc = parseLocation(config.getString("signs." + key));
                if (loc != null) {
                    signLocations.put(key, loc);
                }
            }
        }

        // Load updated start button location
        startButtonLocation = config.contains("button.startbutton")
                ? parseLocation(config.getString("button.startbutton"))
                : null;
    }

    private void saveSignData() {
        for (Map.Entry<String, Location> entry : signLocations.entrySet()) {
            config.set("signs." + entry.getKey(), locationToString(entry.getValue()));
        }
        config.set("startbutton", locationToString(startButtonLocation));
        try {
            config.save(configFile);
        } catch (IOException e) {
            ultimateBingo.getLogger().warning("保存 ingameconfig.yml 失败");
        }
    }

    public void removeSign(String setting) {
        if (!signLocations.containsKey(setting)) return;

        signLocations.remove(setting);
        config.set("signs." + setting, null);
        saveSignData();
    }

    public void removeButton() {
        startButtonLocation = null;
        config.set("button.startbutton", null);
        saveSignData();
    }


    private Location parseLocation(String locString) {
        if (locString == null || locString.isEmpty()) return null;
        String[] parts = locString.split(",");
        if (parts.length != 4) return null;
        return new Location(Bukkit.getWorld(parts[0]),
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3]));
    }

    //endregion
}