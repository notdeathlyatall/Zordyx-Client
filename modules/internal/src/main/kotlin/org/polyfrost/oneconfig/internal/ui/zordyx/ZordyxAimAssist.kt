package org.polyfrost.oneconfig.internal.ui.zordyx

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.serialization.Serializable

/**
 * The AimAssist configuration model.
 *
 * This is the settings a future AimAssist runtime would read, and the whole of what the settings
 * screen edits: nothing here attacks, rotates the view or touches an entity. The model is serializable
 * so it can be written to a config file once a runtime exists, and it validates itself so a value from
 * anywhere (an old file, a text field, a plugin) cannot leave a setting outside its range.
 */

/** How AimAssist moves the view. */
@Serializable
enum class AimAssistMode(val title: String, val description: String) {
    Simple("Simple", "Lightweight smooth aiming."),
    Adaptive("Adaptive", "Advanced tracking with adaptive behaviour."),
}

/** Which point of a target the view is moved towards. */
@Serializable
enum class AimAssistTargetArea(val title: String, val description: String) {
    Center("Center", "Aim at the center of the target hitbox."),
    Closest("Closest", "Aim at the closest position on the target hitbox."),
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
 * The AimAssist settings as the interface sees them.
 *
 * Process state on purpose: the settings survive switching subsections and reopening the interface, and
 * nothing is written to disk, because no AimAssist runtime exists yet to consume a saved file. [update]
 * normalizes every change so a value can never be stored out of range.
 */
object ZordyxAimAssist {
    var settings by mutableStateOf(AimAssistSettings())
        private set

    fun update(transform: (AimAssistSettings) -> AimAssistSettings) {
        settings = transform(settings).normalized()
    }
}
