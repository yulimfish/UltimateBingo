package io.shantek.managers;

import io.shantek.UltimateBingo;
import io.shantek.tools.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SettingsManager {
    UltimateBingo ultimateBingo;

    public SettingsManager(UltimateBingo ultimateBingo){
        this.ultimateBingo = ultimateBingo;
    }

    public Inventory createSettingsGUI(Player player){
        Inventory settingsGUI = Bukkit.createInventory(player, 9, ChatColor.GOLD.toString() + ChatColor.BOLD + "宾果设置");

        ItemStack easy = new ItemBuilder(Material.LIGHT_BLUE_STAINED_GLASS)
                .withDisplayName(ChatColor.AQUA + "添加/移除简单材料").build();

        ItemStack normal = new ItemBuilder(Material.GREEN_STAINED_GLASS)
                .withDisplayName(ChatColor.GREEN + "添加/移除普通材料").build();

        ItemStack hard = new ItemBuilder(Material.YELLOW_STAINED_GLASS)
                .withDisplayName(ChatColor.YELLOW + "添加/移除困难材料").build();

        ItemStack extreme = new ItemBuilder(Material.ORANGE_STAINED_GLASS)
                .withDisplayName(ChatColor.GOLD + "添加/移除极限材料").build();

        ItemStack impossible = new ItemBuilder(Material.RED_STAINED_GLASS)
                .withDisplayName(ChatColor.RED + "添加/移除不可能材料").build();

        settingsGUI.setItem(2, easy);
        settingsGUI.setItem(3, normal);
        settingsGUI.setItem(4, hard);
        settingsGUI.setItem(5, extreme);
        settingsGUI.setItem(6, impossible);

        return settingsGUI;
    }

    public String getDifficultyDisplay(int difficulty){
        if (difficulty == 1){
            return ChatColor.AQUA + "添加/移除简单材料";
        }

        if (difficulty == 2){
            return ChatColor.GREEN + "添加/移除普通材料";
        }

        if (difficulty == 3){
            return ChatColor.YELLOW + "添加/移除困难材料";
        }

        if (difficulty == 4){
            return ChatColor.GOLD + "添加/移除极限材料";
        }

        if (difficulty == 5){
            return ChatColor.RED + "添加/移除不可能材料";
        }
        return null;
    }

    public int getDifficultyInt(String display){
        if (display.equals(ChatColor.AQUA + "添加/移除简单材料")){
            return 1;
        }

        if (display.equals(ChatColor.GREEN + "添加/移除普通材料")){
            return 2;
        }

        if (display.equals(ChatColor.YELLOW + "添加/移除困难材料")){
            return 3;
        }

        if (display.equals(ChatColor.GOLD + "添加/移除极限材料")){
            return 4;
        }

        if (display.equals(ChatColor.RED + "添加/移除不可能材料")){
            return 5;
        }
        return 0;
    }

}
