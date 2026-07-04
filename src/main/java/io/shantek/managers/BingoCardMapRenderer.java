package io.shantek.managers;

import io.shantek.UltimateBingo;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.*;

import java.util.List;

public class BingoCardMapRenderer extends MapRenderer {

    private final BingoMapManager mapManager;
    private final UltimateBingo plugin;
    private final Player owner;

    private int lastHash = 0;

    public BingoCardMapRenderer(BingoMapManager mapManager, Player owner) {
        super(true); // contextual
        this.mapManager = mapManager;
        this.plugin = mapManager.getPlugin();
        this.owner = owner;
    }

    /* ========================================================= */
    /* ===================== PUBLIC API ======================== */
    /* ========================================================= */

    public void markDirty() {
        lastHash = 0;
    }

    public void forceRedraw() {
        markDirty();
    }

    /* ========================================================= */
    /* ======================== RENDER ========================= */
    /* ========================================================= */

    @Override
    public void render(MapView map, MapCanvas canvas, Player viewer) {

        if (!viewer.getUniqueId().equals(owner.getUniqueId())) return;

        List<ItemStack> card = mapManager.getPlayerCard(owner);
        if (card == null || card.isEmpty()) {
            drawBase(canvas);
            return;
        }

        int size = inferSize(card.size());
        boolean[] completed = new boolean[size * size];

        int completedCount = 0;
        for (int i = 0; i < completed.length && i < card.size(); i++) {
            ItemStack it = card.get(i);
            if (it == null) continue;
            boolean done = mapManager.isTaskCompleted(owner, it.getType());
            completed[i] = done;
            if (done) completedCount++;
        }

        boolean fullCard = plugin.currentFullCard;
        boolean hasBingo = fullCard
                ? completedCount >= size * size
                : hasLineBingo(completed, size);

        int hash = 7;
        hash = 31 * hash + size;
        hash = 31 * hash + completedCount;
        hash = 31 * hash + (hasBingo ? 1 : 0);

        if (hash == lastHash) return;
        lastHash = hash;

        /* ------------------ BASE ------------------ */
        drawBase(canvas);

        /* ------------------ GRID ------------------ */
        GridLayout gl = GridLayout.forSize(size);

        byte complete   = MapPalette.matchColor(90, 165, 105);
        byte completedBorder = MapPalette.matchColor(50, 130, 65);
        byte incompleteBorder = MapPalette.matchColor(55, 55, 55);

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int idx = r * size + c;
                int x = gl.startX + c * (gl.cell + gl.gap);
                int y = gl.startY + r * (gl.cell + gl.gap);

                if (idx < card.size() && card.get(idx) != null) {
                    byte cellColor = completed[idx]
                            ? complete
                            : getMaterialColor(card.get(idx).getType());
                    byte cellBorder = completed[idx]
                            ? completedBorder
                            : incompleteBorder;

                    fillRect(canvas, x, y, gl.cell, gl.cell, cellColor);
                    drawOutline(canvas, x, y, gl.cell, gl.cell, cellBorder);

                    // Draw a small white checkmark dot for completed items
                    if (completed[idx]) {
                        int cx = x + gl.cell / 2;
                        int cy = y + gl.cell / 2;
                        byte white = MapPalette.matchColor(255, 255, 255);
                        for (int dy = -2; dy <= 2; dy++) {
                            set(canvas, cx + dy, cy + dy, white);
                            set(canvas, cx - dy, cy + dy, white);
                        }
                    }
                } else {
                    fillRect(canvas, x, y, gl.cell, gl.cell,
                            MapPalette.matchColor(130, 130, 130));
                    drawOutline(canvas, x, y, gl.cell, gl.cell, incompleteBorder);
                }
            }
        }

        /* ----------- PROGRESS BAR (FULL CARD) ----------- */
        if (fullCard) {
            int barX = 16;
            int barY = 118;
            int barW = 96;
            int barH = 5;

            byte bg = MapPalette.matchColor(165, 165, 165);
            byte fg = complete;
            byte frame = MapPalette.matchColor(60, 60, 60);

            drawOutline(canvas, barX, barY, barW, barH, frame);
            fillRect(canvas, barX + 1, barY + 1, barW - 2, barH - 2, bg);

            int fill = (int) ((completedCount / (double) (size * size)) * (barW - 2));
            if (fill > 0) {
                fillRect(canvas, barX + 1, barY + 1, fill, barH - 2, fg);
            }
        }

        /* ------------------ WIN ------------------ */
        if (hasBingo) {
            byte gold = MapPalette.matchColor(200, 175, 90);
            drawOutline(canvas, 0, 0, 128, 128, gold);
            drawOutline(canvas, 1, 1, 126, 126, gold);
        }
    }

    /* ========================================================= */
    /* ======================== BASE =========================== */
    /* ========================================================= */

    private void drawBase(MapCanvas canvas) {
        // Always draw the generated parchment background first
        byte[] parchment = plugin.getCachedParchmentBase();
        if (parchment != null) {
            for (int i = 0; i < parchment.length; i++) {
                canvas.setPixel(i % 128, i / 128, parchment[i]);
            }
        } else {
            // Fallback: solid parchment color
            byte color = MapPalette.matchColor(222, 204, 190);
            for (int y = 0; y < 128; y++) {
                for (int x = 0; x < 128; x++) {
                    canvas.setPixel(x, y, color);
                }
            }
        }

        // Overlay custom image on top (if present), respecting transparency mask
        byte[] overlay = plugin.getCachedOverlayBase();
        boolean[] mask = plugin.getCachedOverlayMask();
        if (overlay != null && mask != null) {
            for (int i = 0; i < overlay.length; i++) {
                if (!mask[i]) continue;
                canvas.setPixel(i % 128, i / 128, overlay[i]);
            }
        }
    }

    /* ========================================================= */
    /* ===================== HELPERS =========================== */
    /* ========================================================= */

    /**
     * Returns a MapPalette color byte for a material, grouped by category.
     * Deterministic per material, visually distinguishable on the map.
     */
    private byte getMaterialColor(Material material) {
        String name = material.name();
        int hue, sat, bri;

        // Category-based base color
        if (name.contains("WOOD") || name.contains("LOG") || name.contains("PLANKS")
                || name.contains("SAPLING") || name.contains("STICK") || name.contains("BAMBOO")) {
            hue = 30; sat = 140; bri = 160;          // warm browns
        } else if (name.contains("STONE") || name.contains("COBBLE") || name.contains("DEEPSLATE")
                || name.contains("GRANITE") || name.contains("DIORITE") || name.contains("ANDESITE")
                || name.contains("TUFF") || name.contains("BASALT") || name.contains("BLACKSTONE")) {
            hue = 0; sat = 30; bri = 130;           // neutral grays
        } else if (name.contains("SAND") || name.contains("GRAVEL") || name.contains("DIRT")
                || name.contains("MUD") || name.contains("CLAY") || name.contains("PODZOL")
                || name.contains("TERRACOTTA") || name.contains("BRICK")) {
            hue = 35; sat = 130; bri = 175;          // earthy tones
        } else if (name.contains("IRON") && !name.contains("IRON_")) {
            hue = 0; sat = 0; bri = 180;             // light gray (iron)
        } else if (name.contains("GOLD") && !name.contains("GOLD_")) {
            hue = 42; sat = 220; bri = 210;          // gold
        } else if (name.contains("DIAMOND")) {
            hue = 190; sat = 180; bri = 200;         // cyan
        } else if (name.contains("EMERALD")) {
            hue = 140; sat = 200; bri = 160;         // green
        } else if (name.contains("REDSTONE")) {
            hue = 0; sat = 200; bri = 170;           // red
        } else if (name.contains("LAPIS")) {
            hue = 225; sat = 220; bri = 140;         // deep blue
        } else if (name.contains("NETHERITE")) {
            hue = 340; sat = 30; bri = 100;          // dark purplish
        } else if (name.contains("COPPER")) {
            hue = 20; sat = 210; bri = 185;          // copper orange
        } else if (name.contains("QUARTZ") || name.contains("CALCITE") || name.contains("SNOW")) {
            hue = 50; sat = 20; bri = 230;           // white/cream
        } else if (name.contains("COAL") || name.contains("OBSIDIAN") || name.contains("BLACK")) {
            hue = 0; sat = 0; bri = 60;             // dark/black
        } else if (name.contains("WOOL") || name.contains("CARPET")) {
            hue = 50; sat = 50; bri = 210;           // soft white
        } else if (name.contains("GLASS") || name.contains("ICE") || name.contains("PANE")) {
            hue = 200; sat = 40; bri = 220;          // translucent blue
        } else if (name.contains("LEAVES") || name.contains("GRASS") || name.contains("VINE")
                || name.contains("FERN") || name.contains("MOSS") || name.contains("AZALEA")
                || name.contains("LILY") || name.contains("SEAGRASS") || name.contains("KELP")) {
            hue = 90; sat = 180; bri = 130;          // fresh green
        } else if (name.contains("MUSHROOM") || name.contains("FUNGUS") || name.contains("WART")
                || name.contains("NETHER")) {
            hue = 300; sat = 120; bri = 140;         // purple/fungus
        } else if (name.contains("PRISMARINE") || name.contains("SEA") || name.contains("OCEAN")) {
            hue = 170; sat = 160; bri = 170;         // teal
        } else if (name.contains("WARPED") || name.contains("CRIMSON") || name.contains("SHROOMLIGHT")) {
            hue = 200; sat = 160; bri = 170;         // nether blue/green
        } else if (name.contains("FLOWER") || name.contains("ROSE") || name.contains("TULIP")
                || name.contains("DAISY") || name.contains("POPPY") || name.contains("ORCHID")
                || name.contains("ALLIUM") || name.contains("BLUET") || name.contains("DANDELION")
                || name.contains("LILAC") || name.contains("PEONY") || name.contains("SUNFLOWER")
                || name.contains("SPORE") || name.contains("PINK_PETALS")) {
            hue = 320; sat = 200; bri = 210;         // pink/magenta
        } else if (name.contains("BERRY") || name.contains("APPLE") || name.contains("BEETROOT")) {
            hue = 350; sat = 220; bri = 170;         // red fruit
        } else if (name.contains("MELON") || name.contains("PUMPKIN") || name.contains("CARROT")
                || name.contains("POTATO")) {
            hue = 25; sat = 210; bri = 200;          // orange/vegetable
        } else if (name.startsWith("COOKED") || name.contains("STEAK") || name.contains("BEEF")
                || name.contains("PORK") || name.contains("MUTTON") || name.contains("CHICKEN")
                || name.contains("RABBIT")) {
            hue = 15; sat = 180; bri = 170;          // cooked meat
        } else if (name.contains("FISH") || name.contains("COD") || name.contains("SALMON")
                || name.contains("PUFFER") || name.contains("TROPICAL")) {
            hue = 200; sat = 120; bri = 190;         // fish blue
        } else if (name.contains("BOAT") || name.contains("CHEST") || name.contains("BARREL")
                || name.contains("DOOR") || name.contains("FENCE") || name.contains("TRAPDOOR")
                || name.contains("SIGN")) {
            hue = 30; sat = 150; bri = 170;          // wooden items
        } else if (name.contains("SWORD") || name.contains("AXE") || name.contains("PICKAXE")
                || name.contains("SHOVEL") || name.contains("HOE")) {
            hue = 0; sat = 0; bri = 150;             // tools gray
        } else if (name.contains("HELMET") || name.contains("CHESTPLATE") || name.contains("LEGGINGS")
                || name.contains("BOOTS") || name.contains("ARMOR")) {
            hue = 210; sat = 100; bri = 180;         // armor blue-gray
        } else if (name.contains("BOW") || name.contains("ARROW") || name.contains("CROSSBOW")) {
            hue = 30; sat = 140; bri = 170;          // bow brown
        } else if (name.contains("POTION") || name.contains("BOTTLE") || name.contains("GHAST")
                || name.contains("BLAZE") || name.contains("SPIDER") || name.contains("MAGMA")) {
            hue = 280; sat = 160; bri = 170;         // potion purple
        } else if (name.contains("GLOWSTONE") || name.contains("SHROOM") || name.contains("LANTERN")
                || name.contains("TORCH") || name.contains("CAMPFIRE") || name.contains("GLOW")) {
            hue = 50; sat = 200; bri = 210;          // glowing yellow
        } else if (name.contains("RAIL") || name.contains("MINECART")) {
            hue = 35; sat = 100; bri = 180;          // rail brown
        } else if (name.contains("ENDER") || name.contains("DRAGON") || name.contains("CHORUS")
                || name.contains("SHULKER") || name.contains("END_")) {
            hue = 270; sat = 100; bri = 170;         // ender purple
        } else if (name.contains("SLIME") || name.contains("HONEY") || name.contains("SLIMEBALL")) {
            hue = 80; sat = 200; bri = 180;          // slime green
        } else if (name.contains("DYE") || name.contains("INK")) {
            hue = 0; sat = 0; bri = 120;             // dye grayish
        } else if (name.contains("GUNPOWDER") || name.contains("TNT") || name.contains("FLINT")) {
            hue = 0; sat = 0; bri = 100;             // dark gray
        } else if (name.contains("BONE") || name.contains("SKULL") || name.contains("FOSSIL")) {
            hue = 40; sat = 30; bri = 210;           // bone white
        } else if (name.contains("SUGAR") || name.contains("CAKE") || name.contains("COOKIE")
                || name.contains("PIE") || name.contains("BREAD") || name.contains("HONEYCOMB")) {
            hue = 40; sat = 100; bri = 220;          // food yellow
        } else if (name.contains("SPONGE") || name.contains("SCUTE") || name.contains("MEMBRANE")) {
            hue = 50; sat = 80; bri = 220;           // light organic
        } else {
            // Deterministic fallback based on material name hash
            int hash = Math.abs(name.hashCode());
            hue = hash % 360;
            sat = 80 + (hash >> 8) % 120;
            bri = 140 + (hash >> 16) % 80;
        }

        // Add deterministic jitter per material (keeps same items consistently colored)
        int seed = Math.abs(name.hashCode());
        int h = (hue + (seed % 20) - 10 + 360) % 360;
        int s = Math.min(255, Math.max(40, sat + (seed >> 4) % 40 - 20));
        int v = Math.min(255, Math.max(60, bri + (seed >> 8) % 30 - 15));

        // HSV to RGB conversion for MapPalette
        float hf = h / 60f;
        float sf = s / 255f;
        float vf = v / 255f;
        int hi = (int) hf % 6;
        float f = hf - (int) hf;
        float p = vf * (1 - sf);
        float q = vf * (1 - f * sf);
        float t = vf * (1 - (1 - f) * sf);
        float rf, gf, bf;
        switch (hi) {
            case 0: rf = vf; gf = t;  bf = p;  break;
            case 1: rf = q;  gf = vf; bf = p;  break;
            case 2: rf = p;  gf = vf; bf = t;  break;
            case 3: rf = p;  gf = q;  bf = vf; break;
            case 4: rf = t;  gf = p;  bf = vf; break;
            default:rf = vf; gf = p;  bf = q;  break;
        }
        int r = Math.round(rf * 255);
        int g = Math.round(gf * 255);
        int b = Math.round(bf * 255);

        return MapPalette.matchColor(r, g, b);
    }

    private void fillRect(MapCanvas c, int x, int y, int w, int h, byte color) {
        for (int py = y; py < y + h; py++) {
            for (int px = x; px < x + w; px++) {
                if (px < 0 || py < 0 || px >= 128 || py >= 128) continue;
                c.setPixel(px, py, color);
            }
        }
    }

    private void drawOutline(MapCanvas c, int x, int y, int w, int h, byte color) {
        for (int i = 0; i < w; i++) {
            set(c, x + i, y, color);
            set(c, x + i, y + h - 1, color);
        }
        for (int i = 0; i < h; i++) {
            set(c, x, y + i, color);
            set(c, x + w - 1, y + i, color);
        }
    }

    private void set(MapCanvas c, int x, int y, byte col) {
        if (x < 0 || y < 0 || x >= 128 || y >= 128) return;
        c.setPixel(x, y, col);
    }

    private int inferSize(int count) {
        if (count <= 9) return 3;
        if (count <= 16) return 4;
        return 5;
    }

    private boolean hasLineBingo(boolean[] done, int size) {
        for (int r = 0; r < size; r++) {
            boolean ok = true;
            for (int c = 0; c < size; c++) {
                if (!done[r * size + c]) ok = false;
            }
            if (ok) return true;
        }
        for (int c = 0; c < size; c++) {
            boolean ok = true;
            for (int r = 0; r < size; r++) {
                if (!done[r * size + c]) ok = false;
            }
            if (ok) return true;
        }
        boolean d1 = true, d2 = true;
        for (int i = 0; i < size; i++) {
            d1 &= done[i * size + i];
            d2 &= done[i * size + (size - 1 - i)];
        }
        return d1 || d2;
    }

    /* ========================================================= */
    /* ==================== GRID LAYOUT ======================== */
    /* ========================================================= */

    private record GridLayout(int cell, int gap, int startX, int startY) {
        static GridLayout forSize(int size) {
            int cell, gap;
            if (size == 3) { cell = 18; gap = 6; }
            else if (size == 4) { cell = 14; gap = 5; }
            else { cell = 11; gap = 4; }

            int gridW = size * cell + (size - 1) * gap;
            int startX = (128 - gridW) / 2;
            int startY = 36;

            return new GridLayout(cell, gap, startX, startY);
        }
    }
}
