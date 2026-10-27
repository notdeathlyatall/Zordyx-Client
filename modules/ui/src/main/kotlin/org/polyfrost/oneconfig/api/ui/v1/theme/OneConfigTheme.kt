package org.polyfrost.oneconfig.api.ui.v1.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import org.polyfrost.oneconfig.api.ui.v1.ExperimentalOneConfigUiApi
import org.polyfrost.oneconfig.api.ui.v1.internal.componentsBridge

@ExperimentalOneConfigUiApi
object OneConfigTheme {
    @JvmStatic
    val MINECRAFT_DARK: OneConfigUITheme by lazy { componentsBridge().minecraftDark() }

    @JvmStatic
    val MINECRAFT_LIGHT: OneConfigUITheme by lazy { componentsBridge().minecraftLight() }

    val current: OneConfigUITheme
        @Composable get() = componentsBridge().currentTheme()

    val accent: Color
        @Composable get() = componentsBridge().accent()

    @JvmStatic
    val active: OneConfigUITheme?
        get() = componentsBridge().activeTheme()

    /**
     * The themes shipped with this mod's interface.
     * <p>
     * Zordyx Dark is the default but is not listed here: it is the interface's own theme and consumers reach it
     * through [active] or [ensureLoaded].
     */
    @JvmStatic
    fun builtIn(): List<OneConfigUITheme> = listOf(MINECRAFT_DARK, MINECRAFT_LIGHT)

    @JvmStatic
    fun activate(theme: OneConfigUITheme) {
        componentsBridge().activateTheme(theme)
    }

    @JvmStatic
    fun ensureLoaded() {
        componentsBridge().ensureThemeLoaded()
    }

    @JvmStatic
    var logoOverride: String? by mutableStateOf(null)
        private set

    @JvmStatic
    fun overrideLogo(path: String?) {
        logoOverride = path?.takeIf { it.isNotBlank() }
    }
}

@ExperimentalOneConfigUiApi
@Composable
fun ProvideOneConfigTheme(content: @Composable () -> Unit) {
    componentsBridge().ProvideTheme(content)
}
