package io.shantek.managers;

import io.shantek.UltimateBingo;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CardTypes {

    UltimateBingo ultimateBingo;
    public CardTypes(UltimateBingo ultimateBingo){
        this.ultimateBingo = ultimateBingo;
    }

    private Inventory getPlayerInventory(Player player) {
        if (ultimateBingo.currentGameMode.equalsIgnoreCase("group")) {
            return ultimateBingo.groupInventory;
        } else if (ultimateBingo.currentGameMode.equalsIgnoreCase("teams")) {
            return ultimateBingo.bingoFunctions.getTeamInventory(player);
        } else {
            return ultimateBingo.bingoManager.getBingoGUIs().get(player.getUniqueId());
        }
    }

    private boolean isCompleted(Inventory inv, int slot) {
        if (inv == null) return false;
        ItemStack item = inv.getItem(slot);
        return item != null && item.getType() == ultimateBingo.tickedItemMaterial;
    }

    public boolean checkSmallCardBingo(Player player) {
        Inventory inv = getPlayerInventory(player);
        if (inv == null) return false;

        // Check rows
        for (int i : new int[]{10, 19, 28}) {
            if (isCompleted(inv, i) && isCompleted(inv, i+1) && isCompleted(inv, i+2)) {
                return true;
            }
        }

        // Check columns
        for (int i : new int[]{10, 11, 12}) {
            if (isCompleted(inv, i) && isCompleted(inv, i+9) && isCompleted(inv, i+18)) {
                return true;
            }
        }

        // Check diagonals
        if ((isCompleted(inv, 10) && isCompleted(inv, 20) && isCompleted(inv, 30)) ||
                (isCompleted(inv, 12) && isCompleted(inv, 20) && isCompleted(inv, 28))) {
            return true;
        }

        return false;
    }

    public boolean checkMediumCardBingo(Player player) {
        Inventory inv = getPlayerInventory(player);
        if (inv == null) return false;

        for (int i : new int[]{10, 19, 28, 37}) {
            if (isCompleted(inv, i) && isCompleted(inv, i+1) &&
                    isCompleted(inv, i+2) && isCompleted(inv, i+3)) {
                return true;
            }
        }

        for (int i : new int[]{10, 11, 12, 13}) {
            if (isCompleted(inv, i) && isCompleted(inv, i+9) &&
                    isCompleted(inv, i+18) && isCompleted(inv, i+27)) {
                return true;
            }
        }

        if ((isCompleted(inv, 10) && isCompleted(inv, 20) &&
                isCompleted(inv, 30) && isCompleted(inv, 40)) ||
                (isCompleted(inv, 13) && isCompleted(inv, 21) &&
                        isCompleted(inv, 29) && isCompleted(inv, 37))) {
            return true;
        }

        return false;
    }

    public boolean checkLargeCardBingo(Player player) {
        Inventory inv = getPlayerInventory(player);
        if (inv == null) return false;

        for (int i : new int[]{10, 19, 28, 37, 46}) {
            if (isCompleted(inv, i) && isCompleted(inv, i+1) && isCompleted(inv, i+2) &&
                    isCompleted(inv, i+3) && isCompleted(inv, i+4)) {
                return true;
            }
        }

        for (int i : new int[]{10, 11, 12, 13, 14}) {
            if (isCompleted(inv, i) && isCompleted(inv, i+9) && isCompleted(inv, i+18) &&
                    isCompleted(inv, i+27) && isCompleted(inv, i+36)) {
                return true;
            }
        }

        if ((isCompleted(inv, 10) && isCompleted(inv, 20) && isCompleted(inv, 30) &&
                isCompleted(inv, 40) && isCompleted(inv, 50)) ||
                (isCompleted(inv, 14) && isCompleted(inv, 22) && isCompleted(inv, 30) &&
                        isCompleted(inv, 38) && isCompleted(inv, 46))) {
            return true;
        }

        return false;
    }

    public boolean checkFullCard(Player player) {
        Inventory inv = getPlayerInventory(player);
        if (inv == null) return false;

        for (int i = 0; i < inv.getSize(); i++) {
            // Skip the check for slot 17 (spyglass slot)
            if (i == 17) continue;

            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != ultimateBingo.tickedItemMaterial) {
                return false;
            }
        }

        return true;
    }
}
