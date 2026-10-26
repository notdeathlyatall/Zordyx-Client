package org.polyfrost.oneconfig.internal.ui.zordyx

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf

/**
 * The Zordyx section and item inventory.
 *
 * Everything the section views render comes from here, so adding a module, overlay or legit mod is a
 * data change rather than a UI change. This phase is frontend only: no entry performs any
 * Minecraft action, and the enabled flags live in [ZordyxState] for the lifetime of the process.
 */

enum class ModuleCategory(val title: String, val icon: String) {
    Combat("Combat", "combat"),
    Render("Render", "eye"),
    Utility("Utility", "settings"),
    World("World", "layers"),
    Inventory("Inventory", "text-input"),
    Network("Network", "cloud"),
    Minigames("Minigames", "lightning-01");

    companion object {
        fun byId(id: String?): ModuleCategory? = entries.firstOrNull {
            it.name.equals(id, ignoreCase = true) || it.title.equals(id, ignoreCase = true)
        }
    }
}

data class ModuleEntry(
    val id: String,
    val name: String,
    val category: ModuleCategory,
    val description: String = "",
    val icon: String? = null,
    val tags: List<String> = emptyList(),
    val enabled: Boolean = false,
)

data class OverlayEntry(
    val id: String,
    val name: String,
    val description: String,
    val icon: String = "hud",
    val enabled: Boolean = false,
)

enum class LegitTag(val label: String) {
    Hud("HUD"),
    Game("Game"),
}

enum class LegitFilter(val title: String) {
    All("All"),
    Hud("HUD"),
    Game("Game"),
    Favorites("Favorites"),
}

data class LegitModEntry(
    val id: String,
    val name: String,
    val description: String,
    val tags: Set<LegitTag>,
    val icon: String? = null,
)

object ZordyxCatalog {
    /**
     * The module inventory.
     *
     * Still empty: the per-category module list has not been supplied yet. Entries added here show up
     * in the category view, its counter, and search automatically. A module is one row of data:
     * `ModuleEntry("screenshot", "Screenshot", ModuleCategory.Utility, description = "...")`.
     */
    val modules: List<ModuleEntry> = emptyList()

    fun modulesIn(category: ModuleCategory): List<ModuleEntry> = modules.filter { it.category == category }

    fun matches(entry: ModuleEntry, query: String): Boolean {
        if (query.isBlank()) return true
        val needle = query.trim()
        return entry.name.contains(needle, ignoreCase = true) ||
            entry.description.contains(needle, ignoreCase = true) ||
            entry.tags.any { it.contains(needle, ignoreCase = true) }
    }

    val overlays: List<OverlayEntry> = listOf(
        OverlayEntry("duel-info", "Duel Info", "Your duel statistics and your opponent's, on screen.", enabled = true),
        OverlayEntry("inventory", "Inventory", "Your inventory, pinned to the HUD."),
        OverlayEntry("party-overlay", "Party Overlay", "Party members and their current status."),
        OverlayEntry("radar", "Radar", "Nearby entities plotted around you."),
        OverlayEntry("rearview", "Rearview", "A second view of what is behind you."),
        OverlayEntry("target-info", "Target Info", "Details about the entity you are looking at.", enabled = true),
        OverlayEntry("text-gui", "Text GUI", "Custom text lines rendered on the HUD."),
    )

    fun matches(entry: OverlayEntry, query: String): Boolean =
        query.isBlank() || entry.name.contains(query.trim(), ignoreCase = true) ||
            entry.description.contains(query.trim(), ignoreCase = true)

    val legitMods: List<LegitModEntry> = listOf(
        LegitModEntry("armorstatus", "ArmorStatus", "Durability and armour pieces as HUD elements.", setOf(LegitTag.Hud)),
        LegitModEntry("block-overlay", "Block Overlay", "Outlines the block you are looking at.", setOf(LegitTag.Game)),
        LegitModEntry("blockhit-animation", "Blockhit Animation", "Restores the classic block-hit swing.", setOf(LegitTag.Game)),
        LegitModEntry("clear-water", "Clear Water", "Removes the underwater fog.", setOf(LegitTag.Game)),
        LegitModEntry("clock", "Clock", "A clock showing the in-game or real time.", setOf(LegitTag.Hud)),
        LegitModEntry("compass", "Compass", "A compass HUD element pointing to your spawn.", setOf(LegitTag.Hud)),
        LegitModEntry("coords", "Coords", "Your coordinates on the HUD.", setOf(LegitTag.Hud)),
        LegitModEntry("fps", "FPS", "Frames per second, drawn on the HUD.", setOf(LegitTag.Hud)),
        LegitModEntry("freelook", "FreeLook", "Look around without turning your character.", setOf(LegitTag.Game)),
        LegitModEntry("hit-color", "Hit Color", "Tints the damage indicator on hit.", setOf(LegitTag.Game)),
        LegitModEntry("inventory-blur", "Inventory Blur", "Blurs the world behind open inventories.", setOf(LegitTag.Game)),
        LegitModEntry("keystrokes", "Keystrokes", "Shows your pressed movement keys.", setOf(LegitTag.Hud)),
        LegitModEntry("mousedelayfix", "MouseDelayFix", "Fixes the vanilla mouse delay.", setOf(LegitTag.Game)),
        LegitModEntry("noclickdelay", "NoClickDelay", "Removes the vanilla click delay.", setOf(LegitTag.Game)),
        LegitModEntry("nohurtcam", "NoHurtCam", "Stops the camera shaking when you take damage.", setOf(LegitTag.Game)),
        LegitModEntry("nojumpdelay", "NoJumpDelay", "Removes the vanilla jump delay.", setOf(LegitTag.Game)),
        LegitModEntry("potion-status", "Potion Status", "Active potion effects and their timers.", setOf(LegitTag.Hud)),
        LegitModEntry("reach-display", "Reach Display", "Shows the distance of your last hit.", setOf(LegitTag.Hud)),
        LegitModEntry("scoreboard", "Scoreboard", "Moves the scoreboard where you want it.", setOf(LegitTag.Hud)),
        LegitModEntry("time-changer", "Time Changer", "Sets a fixed client-side time of day.", setOf(LegitTag.Game)),
        LegitModEntry("weather", "Weather", "Sets a fixed client-side weather state.", setOf(LegitTag.Game)),
    )

    fun matches(entry: LegitModEntry, query: String): Boolean =
        query.isBlank() || entry.name.contains(query.trim(), ignoreCase = true) ||
            entry.description.contains(query.trim(), ignoreCase = true)
}

enum class SettingsSection(val title: String, val description: String) {
    General("General", "Everyday behaviour of the client."),
    Gui("GUI", "How the interface looks and moves."),
    Modules("Modules", "Defaults shared by every module."),
    Notifications("Notifications", "What the client tells you about, and how."),
    SilentAim("Silent Aim", "Aiming options for the client."),
    Sound("Sound", "Interface sounds and their volume.");

    companion object {
        fun byId(id: String?): SettingsSection? = entries.firstOrNull {
            it.name.equals(id, ignoreCase = true) || it.title.equals(id, ignoreCase = true)
        }
    }
}

/**
 * Frontend-only state.
 *
 * Toggles change what this screen shows and nothing else: no module is loaded, and nothing is written
 * to the game or to disk. It is process state on purpose so a preview build cannot leave settings
 * behind that a real build would later pick up.
 */
object ZordyxState {
    private val enabled = mutableStateMapOf<String, Boolean>()
    private val favorites = mutableStateMapOf<String, Boolean>()
    private val switches = mutableStateMapOf<String, Boolean>()

    fun isEnabled(id: String, default: Boolean = false): Boolean = enabled[id] ?: default

    fun setEnabled(id: String, value: Boolean) {
        enabled[id] = value
    }

    fun isFavorite(id: String): Boolean = favorites[id] == true

    fun toggleFavorite(id: String) {
        favorites[id] = !isFavorite(id)
    }

    /** A remembered visual preference for the prototype settings screens. */
    fun switch(key: String, default: Boolean): Boolean = switches[key] ?: default

    fun setSwitch(key: String, value: Boolean) {
        switches[key] = value
    }
}

/**
 * Session-only macro list.
 *
 * Macros are display entries; nothing here is registered, bound or executed.
 */
object ZordyxMacros {
    data class Macro(val id: String, val name: String, val command: String, val keybind: String)

    val entries = mutableStateListOf(
        Macro("welcome", "Welcome Message", "/msg &eZordyx &7hello", "Unbound"),
        Macro("gg", "Say GG", "/say gg", "G"),
    )

    fun create(name: String, command: String) {
        val id = name.trim().lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
        if (id.isEmpty() || entries.any { it.id == id }) return
        entries += Macro(id, name.trim(), command.trim(), "Unbound")
    }
}
