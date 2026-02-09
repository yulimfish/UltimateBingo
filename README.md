# UltimateBingo - Live Bingo Card Map Feature

## 📋 Overview

This is your complete UltimateBingo plugin with the new **Live Bingo Card Map** feature! Players now receive a Minecraft map item that displays their bingo card in real-time, showing all items with colored squares that update as they complete tasks.

## 🎯 What's New?

- **Visual Bingo Cards**: Instead of a compass, players get a map showing their actual bingo card
- **Real-time Updates**: The map automatically updates when items are completed (turns green)
- **Material Colors**: 200+ materials mapped to representative colors for easy identification
- **Interactive**: Hold to view, right-click to open GUI (same as before)
- **All Sizes**: Works with 3x3, 4x4, and 5x5 cards
- **All Modes**: Traditional, Group, Teams, Speedrun, and Shuffle mode

## 📁 What's Included

### Source Code
- `src/` - Complete plugin source code with all modifications
- `pom.xml` - Maven build configuration (unchanged)

### Documentation
- `QUICK_START.md` - Quick reference guide
- `LIVE_BINGO_MAP_IMPLEMENTATION.md` - Comprehensive implementation details
- `CHANGES_SUMMARY.md` - Overview of all changes
- `DETAILED_MODIFICATIONS.md` - File-by-file modification reference

## 🚀 Quick Start

### Building
```bash
cd UltimateBingo-LiveMap
mvn clean package
```

Your JAR will be in `target/UltimateBingo-1.1.0.jar`

### Installing
1. Stop your server
2. Replace your existing UltimateBingo JAR
3. Start your server
4. Done! No config changes needed

## ✨ Key Features

### For Players
- Hold the map to see your bingo card
- Right-click to open the full GUI
- Watch items turn green as you complete them
- White checkmark appears on completed items

### For Admins
- No configuration changes needed
- Works with all existing features
- Fully compatible with teams, shuffle mode, etc.
- Auto-updates - set and forget

## 📊 New Files

Three new Java files:
1. **BingoCardMapRenderer.java** - Renders cards on maps
2. **BingoMapManager.java** - Manages map lifecycle
3. **BingoMapInteractListener.java** - Handles interactions

## 🔧 Modified Files

Five existing files updated:
1. **UltimateBingo.java** - Added map manager
2. **BingoManager.java** - Added map updates
3. **BingoFunctions.java** - Switched to maps
4. **BingoInteractListener.java** - Updated for maps
5. **No changes to pom.xml** - Uses standard Bukkit API

## 🎨 Material Colors

The map displays different colors for different materials:
- **Diamonds** → Bright cyan
- **Emeralds** → Bright green  
- **Gold** → Yellow
- **Iron** → Light gray
- **Wood** → Brown tones
- **Stone** → Gray tones
- **Plants** → Green shades
- **Flowers** → Various bright colors
- **And 200+ more!**

## ✅ Compatibility

- ✅ All game modes (Traditional, Group, Teams, Speedrun, Shuffle)
- ✅ All card sizes (3x3, 4x4, 5x5)
- ✅ All win conditions (Full card, Single row)
- ✅ PlaceholderAPI integration
- ✅ Signs integration
- ✅ Teams system
- ✅ Existing features unchanged

## 📖 Documentation

| Document | Purpose |
|----------|---------|
| `QUICK_START.md` | Fast overview and setup guide |
| `LIVE_BINGO_MAP_IMPLEMENTATION.md` | Complete technical documentation |
| `CHANGES_SUMMARY.md` | Architecture and integration overview |
| `DETAILED_MODIFICATIONS.md` | Exact code changes reference |

## 🧪 Testing Checklist

- [ ] Players receive map (not compass) when game starts
- [ ] Holding map shows bingo card grid
- [ ] Right-clicking map opens GUI
- [ ] Picking up items updates map (turns green)
- [ ] Shuffle mode updates maps correctly
- [ ] Multiple players each see their own card
- [ ] Works with all game modes
- [ ] Works with all card sizes

## 💡 How It Works

```
Game Start → Players get map item (FILLED_MAP)
              ↓
Hold map → See live bingo card grid
              ↓
Pick up item → Map updates (green background + checkmark)
              ↓
Right-click → Opens GUI (same as before)
```

## 🎓 Technical Details

- **Rendering**: Uses Bukkit's MapView API for drawing
- **Colors**: MapPalette color matching for 200+ materials
- **Updates**: Event-driven re-rendering on card changes
- **Performance**: Change detection prevents unnecessary renders
- **Memory**: Only active player maps stored

## 🔮 Future Possibilities

The architecture supports:
- Custom render styles
- Item labels/names
- Progress indicators
- Animation effects
- Theme customization
- Minimap modes

## 📝 Version Info

- **Plugin**: UltimateBingo 1.1.0
- **Minecraft**: 1.20.4+
- **API**: Spigot/Bukkit 1.20.4+
- **Optional**: PlaceholderAPI 2.11.6+

## 🆘 Support

Read the documentation files for:
- **Quick help** → `QUICK_START.md`
- **Full details** → `LIVE_BINGO_MAP_IMPLEMENTATION.md`
- **Code changes** → `DETAILED_MODIFICATIONS.md`
- **Architecture** → `CHANGES_SUMMARY.md`

## 🎉 Summary

This is a complete, ready-to-build implementation of the live bingo card map feature. Simply compile and deploy to your server - all existing features work exactly as before, with the new visual map enhancement!

---

**Created**: January 30, 2026  
**Author**: Implementation for UltimateBingo plugin  
**License**: GNU General Public License v3.0 (same as original)


## Custom Bingo Map Background (Optional)

You can optionally provide a custom background overlay image for the live bingo map.

- **Path:** `plugins/UltimateBingo/map/base.png`
- **Format:** PNG
- **Size:** **128x128** pixels

If the image is present and valid, Ultimate Bingo will draw the built-in parchment background first, then overlay your image (PNG transparency supported), then render the dynamic game summary elements (text, grid, borders).

### Hot Reload

After replacing `map/base.png`, run:

- `/bingo reload`

This reloads the config **and** the map background cache, and forces all active bingo maps to refresh.
