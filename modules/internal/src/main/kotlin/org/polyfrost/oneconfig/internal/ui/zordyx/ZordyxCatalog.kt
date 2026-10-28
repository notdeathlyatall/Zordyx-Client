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
    /** Whether this entry has a settings screen, which the module row then offers. */
    val hasSettings: Boolean = false,
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
    /** The AimAssist entry, the only module with a settings screen so far. */
    const val AIM_ASSIST_ID = "aim-assist"

    /**
     * The module inventory, grouped by the category row it belongs to.
     *
     * This is the single source for the Modules section: an entry here becomes one row with its own
     * toggle in that category, and it joins that category's counter and search automatically. A module
     * is one row of data, so nothing here loads, registers or runs anything in the game.
     */
    val modules: List<ModuleEntry> = listOf(
        // Combat
        ModuleEntry(
            AIM_ASSIST_ID, "AimAssist", ModuleCategory.Combat,
            "Helps you keep your aim on target by smoothly adjusting your crosshair's position.",
            hasSettings = true,
        ),
        ModuleEntry("auto-clicker", "AutoClicker", ModuleCategory.Combat, "Clicks at a steady rate while the attack button is held."),
        ModuleEntry("block-hit", "BlockHit", ModuleCategory.Combat, "Blocks and attacks in one smooth motion."),
        ModuleEntry("hit-flick", "HitFlick", ModuleCategory.Combat, "Flicks your crosshair onto the target as your hit lands."),
        ModuleEntry("hit-select", "HitSelect", ModuleCategory.Combat, "Keeps your aim on the entity you actually clicked."),
        ModuleEntry("reach", "Reach", ModuleCategory.Combat, "Extends how far your hits can land."),
        ModuleEntry("right-clicker", "RightClicker", ModuleCategory.Combat, "Right clicks at a steady rate while the button is held."),
        ModuleEntry("silent-aura", "SilentAura", ModuleCategory.Combat, "Attacks targets around you without moving your crosshair."),
        ModuleEntry("sprint", "Sprint", ModuleCategory.Combat, "Keeps you sprinting without holding the key."),
        ModuleEntry("jump-reset", "JumpReset", ModuleCategory.Combat, "Times your jumps to soften incoming knockback."),
        ModuleEntry("w-tap", "WTap", ModuleCategory.Combat, "Taps forward on hit so your knockback keeps its strength."),
        ModuleEntry("triggerbot", "Triggerbot", ModuleCategory.Combat, "Attacks whatever moves across your crosshair."),
        ModuleEntry("crystal-aura", "CrystalAura", ModuleCategory.Combat, "Places and breaks end crystals for you."),

        // Render
        ModuleEntry("anti-debuff", "AntiDebuff", ModuleCategory.Render, "Hides the debuff overlay covering your screen."),
        ModuleEntry("arrows", "Arrows", ModuleCategory.Render, "Draws the direction arrows are flying from."),
        ModuleEntry("chams", "Chams", ModuleCategory.Render, "Renders entities through walls with a flat colour."),
        ModuleEntry("esp", "ESP", ModuleCategory.Render, "Highlights nearby entities with an outline."),
        ModuleEntry("explosions", "Explosions", ModuleCategory.Render, "Shows the blast radius of an explosion before it happens."),
        ModuleEntry("fullbright", "Fullbright", ModuleCategory.Render, "Keeps caves and night-time fully lit."),
        ModuleEntry("health", "Health", ModuleCategory.Render, "Shows your health as a clean bar or number."),
        ModuleEntry("indicators", "Indicators", ModuleCategory.Render, "Marks important entities with floating labels."),
        ModuleEntry("item-esp", "ItemESP", ModuleCategory.Render, "Highlights dropped items through walls."),
        ModuleEntry("name-tags", "NameTags", ModuleCategory.Render, "Replaces name tags with clearer, scaled text."),
        ModuleEntry("projectiles", "Projectiles", ModuleCategory.Render, "Traces the path of projectiles heading your way."),
        ModuleEntry("search", "Search", ModuleCategory.Render, "Marks the blocks you are looking for."),
        ModuleEntry("spawner-finder", "SpawnerFinder", ModuleCategory.Render, "Reveals spawners hidden in the world."),
        ModuleEntry("storage-esp", "StorageESP", ModuleCategory.Render, "Highlights chests, barrels and shulkers."),
        ModuleEntry("tracers", "Tracers", ModuleCategory.Render, "Draws lines from you to nearby entities."),
        ModuleEntry("trajectories", "Trajectories", ModuleCategory.Render, "Shows where your projectiles will land."),

        // Utility
        ModuleEntry("anti-fireball", "AntiFireball", ModuleCategory.Utility, "Knocks fireballs away before they reach you."),
        ModuleEntry("auto-anchor", "AutoAnchor", ModuleCategory.Utility, "Charges and fires respawn anchors for you."),
        ModuleEntry("auto-mace", "AutoMace", ModuleCategory.Utility, "Swaps to your mace at the right moment."),
        ModuleEntry("auto-pearl", "AutoPearl", ModuleCategory.Utility, "Throws an ender pearl when you need one."),
        ModuleEntry("clutch", "Clutch", ModuleCategory.Utility, "Helps you land safely from a bad fall."),
        ModuleEntry("hit-swap", "HitSwap", ModuleCategory.Utility, "Swaps to your best weapon the moment you hit."),
        ModuleEntry("panic", "Panic", ModuleCategory.Utility, "Turns every module off at once."),
        ModuleEntry("pearl-catch", "PearlCatch", ModuleCategory.Utility, "Catches the pearls thrown to you."),
        ModuleEntry("scaffold", "Scaffold", ModuleCategory.Utility, "Places blocks under your feet as you walk."),
        ModuleEntry("shield-breaker", "ShieldBreaker", ModuleCategory.Utility, "Switches to an axe to crack a shield."),
        ModuleEntry("target-filter", "TargetFilter", ModuleCategory.Utility, "Chooses which entities count as targets."),
        ModuleEntry("velocity", "Velocity", ModuleCategory.Utility, "Reduces the knockback you take."),
        ModuleEntry("wind-charge", "WindCharge", ModuleCategory.Utility, "Uses wind charges to launch you upward."),

        // World
        ModuleEntry("anti-afk", "Anti-AFK", ModuleCategory.World, "Keeps you from being kicked while idle."),
        ModuleEntry("auto-fish", "AutoFish", ModuleCategory.World, "Reels in the moment a fish bites."),
        ModuleEntry("auto-tool", "AutoTool", ModuleCategory.World, "Picks the fastest tool for the block you are breaking."),
        ModuleEntry("block-in", "Block In", ModuleCategory.World, "Builds a quick box of blocks around you for cover."),
        ModuleEntry("chest-steal", "ChestSteal", ModuleCategory.World, "Loots a chest the moment you open it."),
        ModuleEntry("fast-place", "FastPlace", ModuleCategory.World, "Removes the delay between block placements."),
        ModuleEntry("freecam", "Freecam", ModuleCategory.World, "Lets your camera fly away from your body."),
        ModuleEntry("mlg", "MLG", ModuleCategory.World, "Places water under you to survive a fall."),
        ModuleEntry("parkour", "Parkour", ModuleCategory.World, "Jumps for you at the edge of a block."),
        ModuleEntry("safe-walk", "SafeWalk", ModuleCategory.World, "Stops you from walking off an edge."),
        ModuleEntry("xray", "XRay", ModuleCategory.World, "Shows only the ores you care about."),

        // Inventory
        ModuleEntry("armor-switch", "ArmorSwitch", ModuleCategory.Inventory, "Equips the right armour piece in one click."),
        ModuleEntry("auto-armor", "AutoArmor", ModuleCategory.Inventory, "Equips the best armour whenever you pick it up."),
        ModuleEntry("auto-hotbar", "AutoHotbar", ModuleCategory.Inventory, "Moves the item you need into your hotbar."),
        ModuleEntry("auto-totem", "AutoTotem", ModuleCategory.Inventory, "Keeps a totem in your offhand."),
        ModuleEntry("inv-cleaner", "InvCleaner", ModuleCategory.Inventory, "Drops the junk out of your inventory."),
        ModuleEntry("inventory-manager", "InventoryManager", ModuleCategory.Inventory, "Sorts and arranges your inventory for you."),
        ModuleEntry("inventory-fill", "InventoryFill", ModuleCategory.Inventory, "Fills empty slots with matching stacks."),
        ModuleEntry("refill", "Refill", ModuleCategory.Inventory, "Refills your hotbar from your inventory."),
        ModuleEntry("throw-debuff", "ThrowDebuff", ModuleCategory.Inventory, "Throws debuff potions without opening your inventory."),
        ModuleEntry("throw-pot", "ThrowPot", ModuleCategory.Inventory, "Throws healing potions in a single key press."),

        // Network
        ModuleEntry("back-track", "BackTrack", ModuleCategory.Network, "Keeps where you were a moment ago, so hits still register."),
        ModuleEntry("blink", "Blink", ModuleCategory.Network, "Holds your packets and releases them all at once."),
        ModuleEntry("fake-lag", "FakeLag", ModuleCategory.Network, "Shows other players a delayed version of you."),
        ModuleEntry("knockback-delay", "KnockbackDelay", ModuleCategory.Network, "Times your hit so the knockback arrives late."),

        // Minigames
        ModuleEntry("bed-breaker", "BedBreaker", ModuleCategory.Minigames, "Breaks beds the moment you reach them."),
        ModuleEntry("bed-plates", "BedPlates", ModuleCategory.Minigames, "Shows the protection layers on a bed."),
        ModuleEntry("murderer-finder", "MurdererFinder", ModuleCategory.Minigames, "Highlights the murderer in Murder Mystery."),
        ModuleEntry("prop-hunt", "PropHunt", ModuleCategory.Minigames, "Tells you which props are players in disguise."),
    )

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
