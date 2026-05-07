package io.shantek.managers;

import io.shantek.UltimateBingo;
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

        byte incomplete = MapPalette.matchColor(130, 130, 130);
        byte complete = MapPalette.matchColor(90, 165, 105);
        byte border = MapPalette.matchColor(55, 55, 55);

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int idx = r * size + c;
                int x = gl.startX + c * (gl.cell + gl.gap);
                int y = gl.startY + r * (gl.cell + gl.gap);

                fillRect(canvas, x, y, gl.cell, gl.cell,
                        completed[idx] ? complete : incomplete);

                drawOutline(canvas, x, y, gl.cell, gl.cell, border);
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

        byte[] overlay = plugin.getCachedOverlayBase();
        boolean[] mask = plugin.getCachedOverlayMask();

        if (overlay != null && mask != null) {
            for (int i = 0; i < overlay.length; i++) {
                if (!mask[i]) continue;
                canvas.setPixel(i % 128, i / 128, overlay[i]);
            }
            return;
        }

        byte parchment = MapPalette.matchColor(222, 204, 190);
        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 128; x++) {
                canvas.setPixel(x, y, parchment);
            }
        }
    }

    /* ========================================================= */
    /* ===================== HELPERS =========================== */
    /* ========================================================= */

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
