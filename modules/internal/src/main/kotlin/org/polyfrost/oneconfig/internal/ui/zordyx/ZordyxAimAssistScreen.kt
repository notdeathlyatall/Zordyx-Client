package org.polyfrost.oneconfig.internal.ui.zordyx

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import org.polyfrost.oneconfig.internal.ui.components.Chip
import org.polyfrost.oneconfig.internal.ui.components.Text
import org.polyfrost.oneconfig.internal.ui.components.settings.SliderControl
import org.polyfrost.oneconfig.internal.ui.components.settings.SwitchControl
import org.polyfrost.oneconfig.internal.ui.shell.PageGutter
import org.polyfrost.oneconfig.internal.ui.themes.Accent
import org.polyfrost.oneconfig.internal.ui.themes.LocalTheme

/**
 * The AimAssist settings screen, opened from the AimAssist row in the Combat subsection.
 *
 * Every control writes through [ZordyxAimAssist], which persists the value and feeds the analysis engine.
 * The diagnostics group shows what the engine currently sees. Analysis never moves the camera or the
 * crosshair, which the note at the top states explicitly.
 */
@Composable
fun ZordyxAimAssistSettings() {
    val settings = ZordyxAimAssist.settings

    LaunchedEffect(Unit) { ZordyxAimAssist.ensureTickListener() }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = PageGutter),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ZordyxTitle("AimAssist", "Analyzes nearby targets and reports the best one to aim at.")
        ZordyxPreviewNote(
            "Target analysis only. AimAssist selects and scores targets and reports aim error; it never moves your camera or crosshair."
        )

        AimAssistGroup(
            "Target Analysis",
            "Live diagnostics for the current candidate. The bars show angular error only and do not affect the camera.",
        ) {
            AimAssistDiagnostics(settings.maxAngle)
        }

        AimAssistGroup("Mode", "How AimAssist ranks candidate targets.") {
            AimAssistChoice(
                options = AimAssistMode.entries,
                selected = settings.mode,
                title = { it.title },
                description = { it.description },
            ) { mode -> update { it.copy(mode = mode) } }
        }

        AimAssistGroup("Target Settings", "These settings determine which entities are valid targets.") {
            AimAssistToggle("Players", "Include players as potential targets.", settings.targetPlayers) { value ->
                update { it.copy(targetPlayers = value) }
            }
            AimAssistToggle("Mobs", "Include hostile mobs as potential targets.", settings.targetMobs) { value ->
                update { it.copy(targetMobs = value) }
            }
            AimAssistToggle("Animals", "Include peaceful mobs as potential targets.", settings.targetAnimals) { value ->
                update { it.copy(targetAnimals = value) }
            }
            AimAssistToggle("Ignore Invisible", "Ignore invisible targets.", settings.ignoreInvisible) { value ->
                update { it.copy(ignoreInvisible = value) }
            }
            AimAssistToggle("Ignore Naked", "Ignore unarmored targets.", settings.ignoreNaked) { value ->
                update { it.copy(ignoreNaked = value) }
            }
            AimAssistToggle(
                "Ignore Behind Walls",
                "Ignore targets you have no line of sight to.",
                settings.ignoreBehindWalls,
            ) { value -> update { it.copy(ignoreBehindWalls = value) } }
        }

        AimAssistGroup("Behaviour", "When AimAssist is allowed to run.") {
            AimAssistToggle(
                "Require mouse down",
                "Only work while the left mouse button is held. The first target found stays focused until you release the button.",
                settings.requireMouseDown,
            ) { value -> update { it.copy(requireMouseDown = value) } }
            AimAssistToggle(
                "Check Block Break",
                "Pause AimAssist while you are breaking blocks.",
                settings.checkBlockBreak,
            ) { value -> update { it.copy(checkBlockBreak = value) } }
            if (settings.checkBlockBreak) {
                AimAssistText(
                    name = "Break Blocks Whitelist",
                    description = "Blocks you may keep breaking while AimAssist stays active. Separate entries with commas.",
                    value = settings.breakBlocksWhitelist,
                    placeholder = "minecraft:stone, 1, 16",
                ) { value -> update { it.copy(breakBlocksWhitelist = value) } }
            }
        }

        AimAssistGroup("Speed", "How quickly the view is adjusted.") {
            AimAssistSlider(
                name = "Strafe Increase",
                description = "Extra aim adjustment speed while you or your target is strafing.",
                value = settings.strafeIncrease,
                range = AimAssistRanges.strafeIncrease,
            ) { value -> update { it.copy(strafeIncrease = value) } }
            AimAssistSlider(
                name = "Horizontal Speed",
                description = "How quickly the view turns horizontally.",
                value = settings.horizontalSpeed,
                range = AimAssistRanges.horizontalSpeed,
            ) { value -> update { it.copy(horizontalSpeed = value) } }
            AimAssistToggle("Aim Vertically", "Also adjust the view up and down.", settings.aimVertically) { value ->
                update { it.copy(aimVertically = value) }
            }
            if (settings.aimVertically) {
                AimAssistSlider(
                    name = "Vertical Speed",
                    description = "How quickly the view turns vertically.",
                    value = settings.verticalSpeed,
                    range = AimAssistRanges.verticalSpeed,
                ) { value -> update { it.copy(verticalSpeed = value) } }
            }
        }

        AimAssistGroup("Range", "How far from your crosshair a target may be.") {
            AimAssistSlider(
                name = "Max Angle",
                description = "The largest allowed angle between your crosshair and a potential target.",
                value = settings.maxAngle,
                range = AimAssistRanges.maxAngle,
            ) { value -> update { it.copy(maxAngle = value) } }
            AimAssistSlider(
                name = "Distance",
                description = "The furthest a target may be from you.",
                value = settings.distance,
                range = AimAssistRanges.distance,
            ) { value -> update { it.copy(distance = value) } }
        }

        AimAssistGroup("Targeting", "Which point of a target is aimed at, and which target is picked.") {
            AimAssistChoice(
                label = "Target Area",
                options = AimAssistTargetArea.entries,
                selected = settings.targetArea,
                title = { it.title },
                description = { it.description },
            ) { area -> update { it.copy(targetArea = area) } }
            AimAssistChoice(
                label = "Target Mode",
                options = AimAssistTargetMode.entries,
                selected = settings.targetMode,
                title = { it.title },
                description = { it.description },
            ) { mode -> update { it.copy(targetMode = mode) } }
        }

        AimAssistGroup("Limit to Items", "Restrict AimAssist to these items. Leave empty to allow every item.") {
            AimAssistText(
                name = "Items",
                description = "Item display names, the categories swords, shovels, axes, pickaxes, blocks, hand and food, " +
                    "hotbar slots written slot 1 to slot 9, and legacy item ids such as 35 or 35:14. " +
                    "Separate entries with commas.",
                value = settings.limitToItems,
                placeholder = "swords, slot 1, 35:14",
            ) { value -> update { it.copy(limitToItems = value) } }
        }
    }
}

private fun update(transform: (AimAssistSettings) -> AimAssistSettings) = ZordyxAimAssist.update(transform)

/**
 * The live target-analysis readout.
 *
 * It only displays what the engine last returned. When no platform feed is installed yet the panel says so
 * instead of inventing a target, so the page never suggests an analysis that did not happen.
 */
@Composable
private fun AimAssistDiagnostics(maxAngle: Float) {
    val theme = LocalTheme.current
    val enabled = ZordyxAimAssist.enabled
    val analysis = ZordyxAimAssist.analysis
    val candidate = analysis?.target

    DiagnosticRow("Status", if (enabled) analysis?.status?.label ?: "Waiting for target data" else "Disabled")
    DiagnosticRow(
        "Candidates",
        "${analysis?.eligible ?: 0} eligible of ${analysis?.considered ?: 0} considered",
    )
    if (candidate != null) {
        DiagnosticRow("Target", "${candidate.target.name} (${candidate.target.kind.label})")
        DiagnosticRow("Distance", String.format(Locale.ROOT, "%.2f blocks", candidate.distance))
        DiagnosticRow("Angle", String.format(Locale.ROOT, "%.1f\u00B0", candidate.angle))
        DiagnosticRow(
            "Analysis speed",
            "${analysis.horizontalSpeed.roundToInt()}% horizontal, ${analysis.verticalSpeed.roundToInt()}% vertical",
        )
        AimErrorBar("Horizontal error", candidate.yawDelta, maxAngle)
        AimErrorBar("Vertical error", candidate.pitchDelta, maxAngle)
    } else {
        DiagnosticRow("Target", "None")
        DiagnosticRow("Analysis speed", "0% horizontal, 0% vertical")
    }

    if (analysis == null) {
        Text(
            "No world snapshot yet. The engine starts analysing when a platform feed supplies nearby entities.",
            color = theme.textColorSecondary,
            fontSize = 11.sp,
            lineHeight = 15.sp,
        )
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    val theme = LocalTheme.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = theme.textColorSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, color = theme.textColor, fontSize = 12.sp)
    }
}

/** A read-only bar showing how far the selected target is from the crosshair. */
@Composable
private fun AimErrorBar(label: String, value: Float, maxAngle: Float) {
    val theme = LocalTheme.current
    val fraction = (abs(value) / maxAngle.coerceAtLeast(1f)).coerceIn(0f, 1f)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = theme.textColorSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f))
            Text(
                String.format(Locale.ROOT, "%+.1f\u00B0", value),
                color = theme.textColorSecondary,
                fontSize = 11.sp,
            )
        }
        Box(
            Modifier.fillMaxWidth().height(4.dp)
                .background(theme.chipBackground, CircleShape),
        ) {
            Box(
                Modifier.fillMaxWidth(fraction).height(4.dp)
                    .background(Accent, CircleShape),
            )
        }
    }
}

/** One titled group of AimAssist settings, drawn with the same card used elsewhere on the interface. */
@Composable
private fun AimAssistGroup(
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val theme = LocalTheme.current
    Column(
        Modifier.fillMaxWidth()
            .background(theme.modCardBackground, theme.sideBarNavigationEntryShape)
            .border(1.dp, theme.borderColor, theme.sideBarNavigationEntryShape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = theme.textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(description, color = theme.textColorSecondary, fontSize = 12.sp, lineHeight = 16.sp)
        }
        content()
    }
}

/** A boolean setting, using the module row the rest of the interface already toggles with. */
@Composable
private fun AimAssistToggle(
    name: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ZordyxToggleRow(
        name = name,
        description = description,
        enabled = checked,
        onToggle = { onCheckedChange(!checked) },
        trailing = { SwitchControl(checked) { onCheckedChange(!checked) } },
    )
}

/** A numeric setting: the project's slider, with the value shown beside the label. */
@Composable
private fun AimAssistSlider(
    name: String,
    description: String,
    value: Float,
    range: AimAssistRange,
    onValueChange: (Float) -> Unit,
) {
    val theme = LocalTheme.current
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                name,
                color = theme.textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            Text(range.display(value), color = theme.textColorSecondary, fontSize = 12.sp)
        }
        Text(description, color = theme.textColorSecondary, fontSize = 11.sp, lineHeight = 15.sp)
        SliderControl(
            value = value,
            onValueChange = { onValueChange(range.snap(it)) },
            min = range.min,
            max = range.max,
            step = range.step,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** A selection control: the options as chips, with the chosen option's description underneath. */
@Composable
private fun <T> AimAssistChoice(
    options: List<T>,
    selected: T,
    title: (T) -> String,
    description: (T) -> String,
    label: String? = null,
    onSelect: (T) -> Unit,
) {
    val theme = LocalTheme.current
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (label != null) {
            Text(label, color = theme.textColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(options) { option ->
                Chip(
                    label = title(option),
                    selected = option == selected,
                    onClick = { if (option != selected) onSelect(option) },
                )
            }
        }
        Text(description(selected), color = theme.textColorSecondary, fontSize = 11.sp, lineHeight = 15.sp)
    }
}

/** A text or list setting, using the interface's field component so its styling and clear button match. */
@Composable
private fun AimAssistText(
    name: String,
    description: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    val theme = LocalTheme.current
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(name, color = theme.textColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Text(description, color = theme.textColorSecondary, fontSize = 11.sp, lineHeight = 15.sp)
        ZordyxSearchField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            leadingIcon = "text-input",
        )
    }
}
