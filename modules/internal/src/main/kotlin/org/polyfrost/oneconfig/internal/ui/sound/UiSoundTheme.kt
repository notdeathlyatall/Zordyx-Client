package org.polyfrost.oneconfig.internal.ui.sound

import org.polyfrost.oneconfig.internal.ui.themes.ThemeRegistry
import org.polyfrost.oneconfig.internal.ui.themes.UITheme

enum class UiSoundTheme {
    MODERN,
    MINECRAFT;

    companion object {
        fun current(): UiSoundTheme = of(ThemeRegistry.activeTheme)

        fun of(theme: UITheme?): UiSoundTheme =
            if (theme?.isMinecraftStyle == true) MINECRAFT else MODERN
    }
}
