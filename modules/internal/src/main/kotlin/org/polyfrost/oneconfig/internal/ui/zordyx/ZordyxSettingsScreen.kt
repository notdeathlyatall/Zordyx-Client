package org.polyfrost.oneconfig.internal.ui.zordyx

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.polyfrost.oneconfig.internal.ui.navigation.graph.ChangeLogGraph
import org.polyfrost.oneconfig.internal.ui.navigation.graph.CreditsGraph
import org.polyfrost.oneconfig.internal.ui.navigation.graph.KeybindsGraph
import org.polyfrost.oneconfig.internal.ui.navigation.graph.ModsGraph
import org.polyfrost.oneconfig.internal.ui.navigation.graph.PreferencesGraph
import org.polyfrost.oneconfig.internal.ui.navigation.graph.ThemesGraph
import org.polyfrost.oneconfig.internal.ui.shell.LocalShell
import org.polyfrost.oneconfig.internal.ui.shell.PageGutter

/**
 * The Global settings subsections.
 *
 * Screens are prototype surfaces: their switches are local state and change nothing else. The
 * preference, keybind, theme, changelog and credits screens that already exist in the client stay
 * reachable from here.
 */
@Composable
fun ZordyxSettingsScreen(section: SettingsSection) {
    val navigation = LocalShell.current.navigation

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = PageGutter),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ZordyxTitle(section.title, section.description)

        when (section) {
            SettingsSection.General -> {
                ZordyxPreviewNote("Anything you change here is visual only and is not saved.")
                ZordyxLinkRow("Preferences", "Everyday options for the client.", "Open", { navigation.navigate(PreferencesGraph) }, icon = "cog")
                ZordyxLinkRow("Keybinds", "Rebind the client's shortcuts.", "Open", { navigation.navigate(KeybindsGraph) }, icon = "keyboard")
                ZordyxLinkRow("Changelog", "What changed in each release.", "Open", { navigation.navigate(ChangeLogGraph) }, icon = "refresh")
                ZordyxLinkRow("Credits", "The people and projects behind the client.", "Open", { navigation.navigate(CreditsGraph) }, icon = "star")
                PrototypeSwitch("general-opening", "Show opening animation", "Animate the interface the first time it opens.", true)
                PrototypeSwitch("general-smooth", "Smooth transitions", "Animate page changes and dialogs.", true)
            }

            SettingsSection.Gui -> {
                ZordyxPreviewNote("Anything you change here is visual only and is not saved.")
                ZordyxLinkRow("Theme", "Pick a theme and tune the accent colour.", "Open", { navigation.navigate(ThemesGraph) }, icon = "paintbrush")
                PrototypeSwitch("gui-descriptions", "Show descriptions", "Keep a short description under each entry.", true)
                PrototypeSwitch("gui-compact", "Compact rows", "Fit more entries on screen.", true)
                PrototypeSwitch("gui-motion", "Reduce motion", "Use shorter, calmer transitions.", false)
            }

            SettingsSection.Modules -> {
                ZordyxPreviewNote("Defaults for every module. These switches are placeholders in this build.")
                ZordyxLinkRow("Installed mods", "The mods and HUD elements this client provides.", "Open", { navigation.navigate(ModsGraph) }, icon = "layers")
                PrototypeSwitch("modules-descriptions", "Show module descriptions", "Keep a description under each module name.", true)
                PrototypeSwitch("modules-confirm", "Confirm before toggling", "Ask before a module is switched on.", false)
                PrototypeSwitch("modules-sort", "Sort alphabetically", "Order modules by name instead of the catalogue order.", false)
                PrototypeSwitch("modules-favourites", "Show favourites first", "Pin starred modules to the top of their category.", true)
            }

            SettingsSection.Notifications -> {
                ZordyxPreviewNote("A preview of the notification options. No notification service is running.")
                PrototypeSwitch("notifications-enabled", "Enable notifications", "Show client notifications in the corner.", true)
                PrototypeSwitch("notifications-preview", "Show previews", "Include a line of detail in each notification.", true)
                PrototypeSwitch("notifications-sound", "Play a sound", "Play a sound when a notification arrives.", false)
            }

            SettingsSection.SilentAim -> {
                ZordyxPreviewNote(
                    "Placeholder section. No aiming behaviour is implemented in this phase, and none of these rows change the game."
                )
                ZordyxInfoRow("Aim smoothing", "How gradually the view moves toward a target.", "Planned", icon = "move")
                ZordyxInfoRow("Target filter", "Which entities the client may consider.", "Planned", icon = "eye")
                ZordyxInfoRow("Field of view", "How wide an angle is considered around the crosshair.", "Planned", icon = "combat")
            }

            SettingsSection.Sound -> {
                ZordyxPreviewNote("A preview of the sound options. The sound system is not changed.")
                PrototypeSwitch("sound-enabled", "Interface sounds", "Play a sound for clicks, toggles and navigation.", true)
                PrototypeSwitch("sound-toggle", "Sound on toggle", "Play a sound when a module is switched.", true)
                PrototypeSwitch("sound-navigation", "Sound on navigation", "Play a sound when changing pages.", false)
                ZordyxInfoRow("Volume", "Loudness of interface sounds.", "100%", icon = "bell")
            }
        }
    }
}
