package org.polyfrost.oneconfig.internal.ui.themes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import org.apache.logging.log4j.LogManager
import org.polyfrost.compose.render.PolyColor
import org.polyfrost.oneconfig.api.config.v1.ConfigManager
import org.polyfrost.oneconfig.api.platform.v1.Platform
import org.polyfrost.oneconfig.internal.ThemeConfig
import org.polyfrost.oneconfig.internal.ZordyxClientInfo
import org.polyfrost.oneconfig.internal.ui.api.settings.BuiltinVisualizers
import org.polyfrost.oneconfig.internal.ui.sound.UiSoundEvent
import org.polyfrost.oneconfig.internal.ui.sound.UiSoundTheme
import org.polyfrost.oneconfig.internal.ui.sound.UiSounds

object ThemeRegistry {
    private val LOGGER = LogManager.getLogger("OneConfig/Themes")

    /** Every theme the selector offers: the built-in set followed by the custom themes that loaded cleanly. */
    internal val registry = mutableStateListOf<UITheme>()

    internal var activeTheme by mutableStateOf<UITheme?>(null)

    /** Fingerprint of the custom theme directory as of the last scan, so an unchanged directory is not rescanned. */
    private var customThemesSignature: List<String> = emptyList()

    init {
        register(ZordyxDark)
        register(MinecraftDark)
        register(MinecraftLight)
        loadCustomThemes()

        activeTheme = ZordyxDark
        syncNotificationTheme(ZordyxDark)

        BuiltinVisualizers.register()

        ConfigManager.addProfileChangeListener {
            Platform.screen().runOnUiThread { loadFromConfig() }
        }
    }

    /**
     * Called whenever the interface is composed, which also picks up theme files added while the game is running.
     */
    fun init() {
        if (CustomThemes.signature() != customThemesSignature) loadCustomThemes()
    }

    /** Registers a theme, ignoring an id that is already taken so a theme cannot silently replace another. */
    fun register(theme: UITheme) {
        if (registry.any { it.id == theme.id }) {
            LOGGER.error("Ignoring theme '{}' ({}): a theme with that id is already registered", theme.name, theme.id)
            return
        }
        registry += theme
    }

    fun activate(theme: UITheme) {
        val previous = activeTheme
        activeTheme = theme
        // the id is stored rather than the name so two themes may share a display name
        ThemeConfig.activeTheme = theme.id
        UIThemeExtras.accent(theme.id)?.let { accent ->
            ThemeConfig.accentColor = PolyColor(accent.toArgb())
        }
        updateAccent()
        syncNotificationTheme(theme)
        saveThemeToConfig()
        if (previous != null) {
            if (UiSoundTheme.of(previous) != UiSoundTheme.of(theme)) {
                UiSounds.onSoundThemeChanged()
            } else {
                UiSounds.play(UiSoundEvent.CLICK)
            }
        }
    }

    /**
     * Applies the configured theme.
     *
     * A configuration naming a theme that no longer exists (a removed preset, or a custom theme file that was
     * deleted) falls back to Zordyx Dark instead of failing, and the fallback is written back so the selector and
     * the configuration agree.
     */
    fun loadFromConfig() {
        updateAccent()
        try {
            val configured = ThemeConfig.activeTheme
            val theme = registry.find { it.id == configured }
                ?: registry.find { it.name == configured }
                ?: ZordyxDark
            if (theme.id != configured) {
                LOGGER.warn("Theme '{}' is not available, using '{}' instead", configured, theme.name)
            }
            activeTheme = theme
            ThemeConfig.activeTheme = theme.id
            UIThemeExtras.accent(theme.id)?.let { accent ->
                ThemeConfig.accentColor = PolyColor(accent.toArgb())
            }
            updateAccent()
            syncNotificationTheme(theme)
        } catch (t: Throwable) {
            LOGGER.error("Failed to load the configured theme, falling back to {}", ZordyxDark.name, t)
            activeTheme = ZordyxDark
            syncNotificationTheme(ZordyxDark)
        }
    }

    /**
     * Reads the custom theme directory, adding new themes and dropping the ones whose files are gone.
     *
     * If the active theme was one of the dropped themes the interface moves to Zordyx Dark rather than keeping a
     * theme that is no longer there.
     */
    private fun loadCustomThemes() {
        customThemesSignature = CustomThemes.signature()
        val loaded = try {
            CustomThemes.readThemes()
        } catch (t: Throwable) {
            LOGGER.error("Failed to load custom themes from {}", CustomThemes.directory(), t)
            emptyList()
        }

        val loadedIds = loaded.map { it.id }.toHashSet()
        val removed = registry.filter { it.id !in loadedIds && UIThemeExtras.isCustom(it.id) }
        if (removed.isNotEmpty()) {
            registry.removeAll(removed)
            removed.forEach { UIThemeExtras.forget(it.id) }
            LOGGER.warn("Unloaded {} custom theme(s) whose files are gone: {}", removed.size, removed.map { it.id })
        }
        loaded.forEach { theme ->
            // a theme file that changed is reloaded so its edits show up
            registry.removeAll { it.id == theme.id }
            registry += theme
        }
        if (loaded.isNotEmpty() || removed.isNotEmpty()) {
            LOGGER.info("Zordyx Client has {} theme(s) available", registry.size)
        }

        if (activeTheme?.let { UIThemeExtras.isCustom(it.id) && it.id !in loadedIds } == true) {
            activate(ZordyxDark)
        }
    }

    private fun saveThemeToConfig() {
        try {
            ConfigManager.active().save(ZordyxClientInfo.THEMES_ID)
        } catch (_: Exception) {
        }
    }
}
