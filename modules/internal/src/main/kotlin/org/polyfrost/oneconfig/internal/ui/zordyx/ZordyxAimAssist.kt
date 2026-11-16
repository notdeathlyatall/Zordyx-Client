package org.polyfrost.oneconfig.internal.ui.zordyx

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.serialization.Serializable
import org.polyfrost.oneconfig.api.event.v1.EventManager
import org.polyfrost.oneconfig.api.event.v1.events.TickEvent
import org.polyfrost.oneconfig.internal.combat.aim.AimAnalysis
import org.polyfrost.oneconfig.internal.combat.aim.AimAnalysisEngine
import org.polyfrost.oneconfig.internal.combat.aim.AimAssistConfigStore
import org.polyfrost.oneconfig.internal.combat.aim.AimAssistFeed
import org.polyfrost.oneconfig.internal.combat.aim.AimAssistStoredConfig
import org.polyfrost.oneconfig.internal.combat.aim.AimStatus
import org.polyfrost.oneconfig.internal.combat.aim.AimWorld

/**
 * The AimAssist configuration model.
 *
 * This is what the analysis runtime reads and the whole of what the settings screen edits: nothing here
 * attacks, rotates the view or touches an entity. The model is serializable so it can be written to the
 * config store, and it validates itself so a value from anywhere (an old file, a text field, a plugin)
 * cannot leave a setting outside its range.
 */

/** How AimAssist ranks candidate targets. */
@Serializable
enum class AimAssistMode(val title: String, val description: String) {
    Simple("Simple", "Ranks targets with the selected target mode."),
    Adaptive("Adaptive", "Also weighs angular precision and target movement."),
}

/** Which point of a target is measured. */
@Serializable
enum class AimAssistTargetArea(val title: String, val description: String) {
    Center("Center", "Measure the center of the target hitbox."),
    Closest("Closest", "Measure the closest position on the target hitbox."),
}

/** How a target is picked when several are in range. */
@Serializable
enum class AimAssistTargetMode(val title: String, val description: String) {
    Distance("Distance", "Prioritize the nearest target."),
    Yaw("Yaw", "Prioritize the target that needs the smallest horizontal turn."),
    Armor("Armor", "Prioritize targets with weaker armor."),
    Threat("Threat", "Prioritize targets by how dangerous they are."),
    Health("Health", "Prioritize targets with the lowest health."),
}

/**
 * The bounds of one numeric setting.
 *
 * [step] is the increment the interface's slider moves in and the grid [snap] rounds onto, so the
 * stored value and the value on screen never disagree.
 */
data class AimAssistRange(
    val min: Float,
    val max: Float,
    val step: Float,
    private val suffix: String = "",
) {
    private val decimals: Int get() = if (step >= 1f) 0 else 1

    /** Clamps [value] into this range and snaps it onto the step grid, treating a non-finite value as [min]. */
    fun snap(value: Float): Float {
        if (!value.isFinite()) return min
        val clamped = value.coerceIn(min, max)
        if (step <= 0f) return clamped
        val steps = ((clamped - min) / step).roundToInt()
        return (min + steps * step).coerceIn(min, max)
    }

    /** How a value of this setting reads in the interface, such as `45%`, `90°` or `4.5 blocks`. */
    fun display(value: Float): String {
        val text = if (decimals <= 0) snap(value).roundToInt().toString()
        else String.format(Locale.ROOT, "%.${decimals}f", snap(value))
        return text + suffix
    }
}

/**
 * The ranges of the numeric AimAssist settings.
 *
 * Speeds are percentages of the module's own maximum turn speed, so they stay meaningful whichever way
 * a future runtime measures them; angles are degrees and distances are blocks, matching the units the
 * settings are described in. The defaults are moderate values a player can tune up or down, and are
 * all inside their own range.
 */
object AimAssistRanges {
    val strafeIncrease = AimAssistRange(0f, 100f, 1f, "%")
    val verticalSpeed = AimAssistRange(0f, 100f, 1f, "%")
    val horizontalSpeed = AimAssistRange(0f, 100f, 1f, "%")
    val maxAngle = AimAssistRange(0f, 180f, 1f, "\u00B0")
    val distance = AimAssistRange(0.5f, 8f, 0.1f, " blocks")
}

/** The AimAssist settings, with defaults. */
@Serializable
data class AimAssistSettings(
    val mode: AimAssistMode = AimAssistMode.Simple,
    val targetPlayers: Boolean = true,
    val targetMobs: Boolean = true,
    val targetAnimals: Boolean = false,
    val ignoreInvisible: Boolean = true,
    val ignoreNaked: Boolean = false,
    val ignoreBehindWalls: Boolean = false,
    val requireMouseDown: Boolean = false,
    val strafeIncrease: Float = 25f,
    val checkBlockBreak: Boolean = true,
    val breakBlocksWhitelist: String = "",
    val aimVertically: Boolean = false,
    val verticalSpeed: Float = 35f,
    val horizontalSpeed: Float = 55f,
    val maxAngle: Float = 90f,
    val distance: Float = 4.5f,
    val limitToItems: String = "",
    val targetArea: AimAssistTargetArea = AimAssistTargetArea.Center,
    val targetMode: AimAssistTargetMode = AimAssistTargetMode.Distance,
) {
    /**
     * The vertical speed that applies right now.
     *
     * Vertical adjustment is off by default, so the stored speed is kept for when it is turned back on
     * but contributes nothing while [aimVertically] is false.
     */
    val effectiveVerticalSpeed: Float get() = if (aimVertically) verticalSpeed else 0f

    /** This settings object with every numeric value clamped into its range and snapped onto its step. */
    fun normalized(): AimAssistSettings = copy(
        strafeIncrease = AimAssistRanges.strafeIncrease.snap(strafeIncrease),
        verticalSpeed = AimAssistRanges.verticalSpeed.snap(verticalSpeed),
        horizontalSpeed = AimAssistRanges.horizontalSpeed.snap(horizontalSpeed),
        maxAngle = AimAssistRanges.maxAngle.snap(maxAngle),
        distance = AimAssistRanges.distance.snap(distance),
    )
}

/**
 * Splits an entered list setting into its entries.
 *
 * Entries are separated by commas or newlines, trimmed, and blanks and repeats are dropped, so
 * `swords, slot 1, 35:14` comes back as three entries whatever the spacing. Entries keep the order they
 * were written in and their text is preserved verbatim, which is why the settings screen stores the raw
 * text rather than a rewrite of it.
 *
 * The entries a list setting accepts are item display names, the generic categories `swords`,
 * `shovels`, `axes`, `pickaxes`, `blocks`, `hand` and `food`, a hotbar slot written `slot 1` to
 * `slot 9`, and legacy numeric item ids with optional metadata such as `35` or `35:14`.
 */
fun splitAimAssistEntries(raw: String): List<String> =
    raw.split(',', '\n').map { it.trim() }.filter { it.isNotEmpty() }.distinct()

/**
 * The AimAssist settings and runtime as the interface sees them.
 *
 * Settings and the module's enabled state are persisted when they change and loaded once, so they survive
 * a restart. The object also owns the analysis engine: a platform adapter sets [feed], the tick listener
 * calls [analyze] on every client tick, and the settings screen reads [analysis] for its diagnostics. The
 * engine only analyses targets; nothing here rotates the camera or changes the crosshair.
 */
object ZordyxAimAssist {
    private val store by lazy { AimAssistConfigStore(AimAssistConfigStore.defaultPath()) }
    private val engine = AimAnalysisEngine()

    @Volatile
    private var loaded = false

    @Volatile
    private var listenerRegistered = false

    private var storedSettings by mutableStateOf(AimAssistSettings())
    private var storedEnabled by mutableStateOf(false)

    /** The latest analysis pass, or null before the first world snapshot arrives. */
    var analysis by mutableStateOf<AimAnalysis?>(null)
        private set

    /** The platform adapter that supplies world snapshots. Null until a game platform installs one. */
    var feed: AimAssistFeed? = null

    val settings: AimAssistSettings
        get() {
            ensureLoaded()
            return storedSettings
        }

    val enabled: Boolean
        get() {
            ensureLoaded()
            return storedEnabled
        }

    fun update(transform: (AimAssistSettings) -> AimAssistSettings) {
        ensureLoaded()
        val updated = transform(storedSettings).normalized()
        if (updated == storedSettings) return
        storedSettings = updated
        persist()
    }

    fun setEnabled(value: Boolean) {
        ensureLoaded()
        if (storedEnabled == value) return
        storedEnabled = value
        if (!value) {
            engine.reset()
            analysis = AimAnalysis(AimStatus.Disabled)
        }
        persist()
    }

    /** Analyses [world] with the current settings and records the result for the diagnostics panel. */
    fun analyze(world: AimWorld): AimAnalysis {
        ensureLoaded()
        val result = engine.analyze(world, storedSettings, storedEnabled)
        analysis = result
        return result
    }

    /** Called by the client tick listener; does nothing until the module is enabled and a feed is installed. */
    fun tick() {
        ensureLoaded()
        if (!storedEnabled) return
        val snapshot = feed?.snapshot()
        if (snapshot == null) {
            // Leaving a world must not leave the last target on screen as if it were still live.
            engine.reset()
            if (analysis?.status != AimStatus.NoTarget) analysis = AimAnalysis(AimStatus.NoTarget)
            return
        }
        analyze(snapshot)
    }

    /** Registers the client tick listener once; repeated calls are harmless. */
    fun ensureTickListener() {
        if (listenerRegistered) return
        listenerRegistered = true
        runCatching {
            EventManager.register(TickEvent.End::class.java, Runnable { tick() })
        }.onFailure {
            listenerRegistered = false
        }
    }

    private fun ensureLoaded() {
        if (loaded) return
        synchronized(this) {
            if (loaded) return
            val stored = store.load()
            storedSettings = stored.settings
            storedEnabled = stored.enabled
            loaded = true
        }
    }

    private fun persist() {
        store.save(AimAssistStoredConfig(storedEnabled, storedSettings))
    }
}
