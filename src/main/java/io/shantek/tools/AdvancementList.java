package io.shantek.tools;

import io.shantek.UltimateBingo;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;

import java.util.*;

/**
 * Stores advancement pools categorized by difficulty tier (1-5).
 * Advancements are resolved from key strings at runtime.
 * Only advancements that exist on this server version are included.
 */
public class AdvancementList {

    private final UltimateBingo plugin;
    private final Map<Integer, List<Advancement>> advancements = new HashMap<>();

    public AdvancementList(UltimateBingo plugin) {
        this.plugin = plugin;
        for (int i = 1; i <= 5; i++) {
            advancements.put(i, new ArrayList<>());
        }
        loadDefaults();
    }

    public Map<Integer, List<Advancement>> getAdvancements() {
        return advancements;
    }

    /**
     * Get the Chinese display title for an advancement.
     * Uses BingoFunctions' ADVANCEMENT_CN map with official translations.
     */
    public static String getAdvancementTitle(Advancement adv, UltimateBingo plugin) {
        if (adv == null) return "未知成就";
        return plugin.getBingoFunctions().getAdvancementName(adv.getKey().toString());
    }

    /**
     * Convenience overload for BingoManager (needs plugin reference).
     */
    public String getAdvancementTitle(Advancement adv) {
        return getAdvancementTitle(adv, plugin);
    }

    private void loadDefaults() {
        // Tier 1 - Easy: early-game, stone age
        addAdvancements(1,
            "minecraft:story/root",
            "minecraft:story/mine_stone",
            "minecraft:story/upgrade_tools",
            "minecraft:story/smelt_iron",
            "minecraft:story/obtain_armor",
            "minecraft:story/iron_tools",
            "minecraft:story/deflect_arrow",
            "minecraft:adventure/root",
            "minecraft:adventure/kill_a_mob",
            "minecraft:adventure/sleep_in_bed",
            "minecraft:husbandry/root",
            "minecraft:husbandry/plant_seed",
            "minecraft:husbandry/safely_harvest_honey",
            "minecraft:husbandry/breed_an_animal",
            "minecraft:husbandry/tame_an_animal",
            "minecraft:adventure/trade"
        );

        // Tier 2 - Normal: iron age, basic nether, basic adventure
        addAdvancements(2,
            "minecraft:story/lava_bucket",
            "minecraft:story/form_obsidian",
            "minecraft:story/enter_the_nether",
            "minecraft:story/shiny_gear",
            "minecraft:story/enchant_item",
            "minecraft:nether/root",
            "minecraft:nether/brew_potion",
            "minecraft:nether/obtain_crying_obsidian",
            "minecraft:nether/distract_piglin",
            "minecraft:adventure/shoot_arrow",
            "minecraft:adventure/throw_trident",
            "minecraft:husbandry/fishy_business",
            "minecraft:husbandry/tactical_fishing",
            "minecraft:husbandry/wax_on",
            "minecraft:husbandry/make_a_sign_glow",
            "minecraft:adventure/spyglass_at_parrot"
        );

        // Tier 3 - Hard: deep nether, diamonds, end entry
        addAdvancements(3,
            "minecraft:story/cure_zombie_villager",
            "minecraft:story/follow_ender_eye",
            "minecraft:story/enter_the_end",
            "minecraft:nether/find_fortress",
            "minecraft:nether/obtain_blaze_rod",
            "minecraft:nether/return_to_sender",
            "minecraft:nether/fast_travel",
            "minecraft:nether/uneasy_alliance",
            "minecraft:nether/loot_bastion",
            "minecraft:nether/ride_strider",
            "minecraft:end/root",
            "minecraft:end/kill_dragon",
            "minecraft:end/dragon_egg",
            "minecraft:adventure/voluntary_exile",
            "minecraft:adventure/hero_of_the_village",
            "minecraft:adventure/totem_of_undying"
        );

        // Tier 4 - Extreme: end cities, wither, deep exploration
        addAdvancements(4,
            "minecraft:end/enter_end_gateway",
            "minecraft:end/find_end_city",
            "minecraft:end/elytra",
            "minecraft:end/levitate",
            "minecraft:end/respawn_dragon",
            "minecraft:nether/summon_wither",
            "minecraft:nether/create_beacon",
            "minecraft:nether/all_potions",
            "minecraft:nether/create_full_beacon",
            "minecraft:adventure/very_very_frightening",
            "minecraft:adventure/spyglass_at_ghast",
            "minecraft:adventure/spyglass_at_dragon",
            "minecraft:adventure/lightning_rod_with_villager_no_fire",
            "minecraft:husbandry/obtain_netherite_hoe",
            "minecraft:husbandry/axolotl_in_a_bucket",
            "minecraft:husbandry/kill_axolotl_target"
        );

        // Tier 5 - Impossible: completionist challenges
        addAdvancements(5,
            "minecraft:adventure/kill_all_mobs",
            "minecraft:adventure/adventuring_time",
            "minecraft:husbandry/balanced_diet",
            "minecraft:husbandry/breed_all_animals",
            "minecraft:husbandry/complete_catalogue",
            "minecraft:nether/all_effects",
            "minecraft:nether/explore_nether",
            "minecraft:adventure/fall_from_world_height",
            "minecraft:adventure/play_jukebox_in_meadows",
            "minecraft:adventure/walk_on_powder_snow_with_leather_boots"
        );
    }

    private void addAdvancements(int tier, String... keys) {
        List<Advancement> list = advancements.get(tier);
        for (String keyStr : keys) {
            try {
                NamespacedKey nk = NamespacedKey.fromString(keyStr);
                if (nk == null) continue;
                Advancement adv = Bukkit.getAdvancement(nk);
                if (adv != null) {
                    list.add(adv);
                }
            } catch (Exception ignored) {
                // Skip advancements not available in this Minecraft version
            }
        }
    }
}
