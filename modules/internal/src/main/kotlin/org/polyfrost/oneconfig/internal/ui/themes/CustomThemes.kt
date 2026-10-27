package org.polyfrost.oneconfig.internal.ui.themes

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.util.Comparator
import org.apache.logging.log4j.LogManager
import org.polyfrost.oneconfig.api.config.v1.ConfigManager
import org.polyfrost.oneconfig.internal.ZordyxClientInfo
import org.polyfrost.oneconfig.utils.v1.JsonUtils

private val LOGGER = LogManager.getLogger("OneConfig/Themes")

private const val ASSET_PREFIX = "assets/oneconfig/"
private const val DEFAULT_LOGO = "assets/oneconfig/brand/oneconfig.svg"
private const val DEFAULT_RADIUS = 12f
private const val DEFAULT_ICON_RADIUS = 4f
private const val DEFAULT_SCROLLBAR_RADIUS = 8f
private const val DEFAULT_FONT = "poppins"
private const val MINECRAFT_FONT = "minecraft"
private const val MAX_NAME_LENGTH = 64
private const val MAX_THEMES = 64
private const val OPAQUE = 0xFF000000.toInt()

private val FONTS = listOf(DEFAULT_FONT, MINECRAFT_FONT)
private val ID_PATTERN = Regex("[a-z0-9][a-z0-9._-]{0,63}")

/**
 * Custom themes for the Zordyx Client interface.
 *
 * Themes are plain JSON files in `zordyx_client/themes` (see [directory]) so a theme needs neither code nor a
 * rebuild. Every file is validated before it is used and treated as untrusted input: only strings, numbers and
 * booleans are read, no code is ever loaded from a theme, and preview and logo paths are restricted to this mod's
 * own asset namespace so a file cannot make the interface read arbitrary paths.
 *
 * A malformed or incomplete file is reported in the log and skipped; the interface keeps using the theme it was
 * already on, and [ThemeRegistry] falls back to Zordyx Dark when the active theme goes missing. See `README.md`
 * for the file format.
 */
object CustomThemes {
    /** The directory themes are read from, next to this mod's other files. */
    fun directory(): Path = ConfigManager.internal().folder.resolve(ZordyxClientInfo.THEMES_DIR)

    /**
     * A cheap fingerprint of the theme directory (file names with their sizes and timestamps) so callers can skip
     * rescanning a directory that has not changed.
     */
    fun signature(): List<String> {
        val dir = directory()
        if (!Files.isDirectory(dir)) return emptyList()
        return runCatching {
            listThemeFiles(dir).map { file ->
                val attributes = runCatching { Files.readAttributes(file, BasicFileAttributes::class.java) }.getOrNull()
                "${file.fileName}:${attributes?.size() ?: -1}:${attributes?.lastModifiedTime()?.toMillis() ?: -1}"
            }
        }.getOrElse { t ->
            LOGGER.error("Failed to read the Zordyx Client theme directory {}", dir, t)
            emptyList()
        }
    }

    /**
     * Parses every theme file, skipping the ones that fail validation.
     *
     * The directory is created when it is missing so theme authors have an obvious place to drop files.
     */
    fun readThemes(): List<UITheme> {
        val dir = directory()
        if (!Files.isDirectory(dir)) {
            runCatching { Files.createDirectories(dir) }
                .onFailure { LOGGER.warn("Could not create the Zordyx Client theme directory {}", dir, it) }
        }
        val files = if (Files.isDirectory(dir)) listThemeFiles(dir).take(MAX_THEMES) else emptyList()
        val themes = ArrayList<UITheme>(files.size)
        val ids = HashSet<String>(files.size)
        for (file in files) {
            val theme = load(file)
            if (theme != null && ids.add(theme.id)) {
                themes.add(theme)
            }
        }
        return themes
    }

    /** Reads one theme file, reporting and discarding it when it is malformed. */
    private fun load(file: Path): UITheme? {
        var theme: UITheme? = null
        try {
            val json = Files.readString(file, StandardCharsets.UTF_8)
            val root = JsonUtils.parseOrNull(json)?.takeIf { it.isJsonObject }?.asJsonObject
            if (root == null) {
                LOGGER.error("Skipping custom theme {}: it is not a JSON object", file.fileName)
            } else {
                theme = ThemeParser(file.fileName.toString(), root).parse()
            }
        } catch (e: IllegalArgumentException) {
            LOGGER.error("Skipping custom theme {}: {}", file.fileName, e.message)
        } catch (e: Exception) {
            LOGGER.error("Skipping custom theme {}: it could not be read", file.fileName, e)
        }
        return theme
    }

    private fun listThemeFiles(dir: Path): List<Path> = Files.list(dir).use { stream ->
        stream.filter { it.fileName.toString().lowercase().endsWith(".json") }
            .sorted(Comparator.comparing { it.fileName.toString() })
            .toList()
    }
}

/**
 * Validates one theme file and builds its [UITheme].
 *
 * Every failure is an [IllegalArgumentException] whose message names the offending field, which
 * [CustomThemes.readThemes] reports together with the file it came from.
 */
private class ThemeParser(private val fileName: String, private val root: JsonObject) {
    private val id: String = requiredId()
    private val name: String = requiredName()

    fun parse(): UITheme {
        val shape = RoundedCornerShape(shapeRadius("cornerRadius", DEFAULT_RADIUS))
        val minecraft = isMinecraftFont()
        val theme = UITheme(
            id, name,

            requiredColor("pageBackground"),
            requiredColor("sidebarBackground"),
            color("chipBackground") ?: requiredColor("componentBackground"),
            requiredColor("panelBackground"),
            requiredColor("componentBackground"),
            requiredColor("popupBackground"),

            requiredColor("border"),
            requiredColor("primaryText"),
            requiredColor("secondaryText"),
            color("accentText") ?: Color.White,

            color("shadow") ?: Color.Black,
            color("controlThumb") ?: Color.White,
            color("favorite") ?: Color(0xFFE8B84B),

            boolean("shadowEnabled") ?: true,

            shape,
            RoundedCornerShape(shapeRadius("cornerRadius", DEFAULT_RADIUS) / 2f),
            shape,
            RoundedCornerShape(shapeRadius("cornerRadius", DEFAULT_RADIUS) / 4f),
            shape,
            shape,
            RoundedCornerShape(shapeRadius("cornerRadius", DEFAULT_RADIUS) / 2f),

            UIBranding(assetPath("logo") ?: DEFAULT_LOGO),
            if (minecraft) MinecraftDark.typography else ZordyxDark.typography,
            if (minecraft) MinecraftDark.iconOverrides else emptyMap(),
        ).withShapes(
            RoundedCornerShape(shapeRadius("iconShapeRadius", DEFAULT_ICON_RADIUS)),
            RoundedCornerShape(shapeRadius("scrollbarRadius", DEFAULT_SCROLLBAR_RADIUS)),
        )
        color("controlTrack")?.let { theme.withControlTrackColor(it) }
        UIThemeExtras.put(
            id,
            UIThemeExtras.Extras(
                description = string("description")?.takeIf { it.isNotBlank() },
                preview = assetPath("preview"),
                accent = color("accent"),
                custom = true,
                minecraft = minecraft,
            ),
        )
        LOGGER.info("Loaded custom theme '{}' ({}) from {}", name, id, fileName)
        return theme
    }

    private fun requiredId(): String {
        val value = string("id") ?: fail("has no \"id\"")
        if (!ID_PATTERN.matches(value)) fail("\"id\" must match ${ID_PATTERN.pattern} but was '$value'")
        return value
    }

    private fun requiredName(): String {
        val value = string("name") ?: fail("has no \"name\"")
        if (value.isBlank() || value.length > MAX_NAME_LENGTH) {
            fail("\"name\" must be 1 to $MAX_NAME_LENGTH characters")
        }
        return value
    }

    private fun shapeRadius(key: String, fallback: Float): Float {
        val value = number(key) ?: fallback
        if (value < 0f) fail("\"$key\" cannot be negative")
        return value
    }

    private fun isMinecraftFont(): Boolean {
        val value = string("font") ?: DEFAULT_FONT
        if (!FONTS.contains(value.lowercase())) fail("\"font\" must be one of $FONTS but was '$value'")
        return value.equals(MINECRAFT_FONT, ignoreCase = true)
    }

    private fun requiredColor(key: String): Color = color(key) ?: fail("has no \"$key\" colour")

    private fun color(key: String): Color? {
        val element = primitive(key) ?: return null
        if (!element.isJsonPrimitive || !element.asJsonPrimitive.isString) {
            fail("\"$key\" must be a hex colour string")
        }
        val raw = element.asString.trim().removePrefix("#")
        if ((raw.length != 6 && raw.length != 8) || raw.any { it.digitToIntOrNull(16) == null }) {
            fail("\"$key\" must be #RRGGBB or #AARRGGBB but was '${element.asString}'")
        }
        val argb = if (raw.length == 6) OPAQUE or raw.toInt(16) else raw.toLong(16).toInt()
        return Color(argb)
    }

    private fun string(key: String): String? {
        val element = primitive(key) ?: return null
        if (!element.isJsonPrimitive || !element.asJsonPrimitive.isString) {
            fail("\"$key\" must be a string")
        }
        return element.asString
    }

    private fun boolean(key: String): Boolean? {
        val element = primitive(key) ?: return null
        if (!element.isJsonPrimitive || !element.asJsonPrimitive.isBoolean) {
            fail("\"$key\" must be true or false")
        }
        return element.asBoolean
    }

    private fun number(key: String): Float? {
        val element = primitive(key) ?: return null
        if (!element.isJsonPrimitive || !element.asJsonPrimitive.isNumber) {
            fail("\"$key\" must be a number")
        }
        return element.asFloat
    }

    /**
     * Only classpath resource paths inside this mod's own asset namespace are accepted, so a theme file cannot
     * point the renderer at an arbitrary path.
     */
    private fun assetPath(key: String): String? {
        val value = string(key)?.trim()?.takeIf { it.isNotBlank() } ?: return null
        if (!value.startsWith(ASSET_PREFIX) || value.contains("..") || '\\' in value) {
            fail("\"$key\" must be a resource path under $ASSET_PREFIX")
        }
        return value
    }

    private fun primitive(key: String): JsonElement? {
        val element = root.get(key) ?: return null
        return element.takeUnless { it.isJsonNull }
    }

    private fun fail(reason: String): Nothing = throw IllegalArgumentException(reason)
}
