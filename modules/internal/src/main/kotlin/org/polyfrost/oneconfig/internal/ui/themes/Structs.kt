package org.polyfrost.oneconfig.internal.ui.themes

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import java.util.concurrent.ConcurrentHashMap
import org.polyfrost.oneconfig.api.ui.v1.theme.OneConfigTheme
import org.polyfrost.oneconfig.api.ui.v1.theme.OneConfigUITheme

/** Sets a panel background color's alpha from an opacity percentage between 0 and 100 */
fun Color.withOpacityPercent(percent: Float): Color =
    copy(alpha = (percent / 100f).coerceIn(0f, 1f))

data class UITheme(
    val id: String,
    override val name: String,

    override val pageBackground: Color,
    override val sidebarBackground: Color,
    override val chipBackground: Color,
    override val modCardBackground: Color,
    override val componentBackground: Color,
    override val popupBackground: Color,

    override val borderColor: Color,
    override val textColor: Color,
    override val textColorSecondary: Color,
    override val accentTextColor: Color,

    override val shadowColor: Color,
    override val controlThumbColor: Color,
    override val favoriteColor: Color,

    override val shadowEnabled: Boolean,

    override val backgroundShape: Shape,
    override val sideBarNavigationEntryShape: Shape,
    override val modCardShape: Shape,
    override val checkBoxShape: Shape,
    override val buttonShape: Shape,
    override val popupShape: Shape,
    override val circleShape: Shape,

    val branding: UIBranding,
    val typography: UITypography,
    val iconOverrides: Map<String, String> = emptyMap(),
) : OneConfigUITheme() {
    override val fontFamily: FontFamily get() = typography.family

    override val logoPath: String get() = brandedLogoPath(this)

    /** The description shown under the name in the theme selector, or null when the theme has none. */
    val description: String? get() = UIThemeExtras.description(id)

    /**
     * True for the Minecraft-styled themes, which use blocky shapes, the Minecraft font and Minecraft UI sounds.
     */
    val isMinecraftStyle: Boolean
        get() = id.startsWith(MINECRAFT_THEME_PREFIX) || UIThemeExtras.isMinecraftStyle(id)

    /**
     * The preview drawn on the theme's card in the selector.
     *
     * A theme ships its preview as `<id>.svg` unless it names one, so a custom theme only has to supply a
     * `preview` when it wants an image of its own.
     */
    val previewImage: String get() = UIThemeExtras.preview(id) ?: "assets/oneconfig/images/$id.svg"

    var controlTrackColor: Color = DefaultControlTrackColor
        private set

    fun withControlTrackColor(color: Color): UITheme =
        copy().also { it.controlTrackColor = color }

    // The shapes below are kept out of the constructor. PolyPlus calls the generated copy() with the
    // exact signature it was compiled against, so a new constructor parameter crashes every released
    // build of it on startup. They are looked up by theme id so they also survive that copy()
    val modIconShape: Shape get() = (extraShapes[id] ?: DefaultExtraShapes).first

    val scrollBarShape: Shape get() = (extraShapes[id] ?: DefaultExtraShapes).second

    fun withShapes(modIconShape: Shape, scrollBarShape: Shape): UITheme =
        also { extraShapes[id] = modIconShape to scrollBarShape }

    companion object {
        val DefaultControlTrackColor = Color(0xFF74777F)

        /** Ids of the Minecraft-styled built-in themes start with this. */
        const val MINECRAFT_THEME_PREFIX = "minecraft-"

        private val DefaultExtraShapes: Pair<Shape, Shape> = RoundedCornerShape(4.dp) to RoundedCornerShape(8.dp)

        private val extraShapes = ConcurrentHashMap<String, Pair<Shape, Shape>>()
    }
}

data class UIBranding(
    val logoPath: String
)

data class UITypography(
    val family: FontFamily,
)

/**
 * Theme metadata which deliberately lives outside [UITheme].
 *
 * Adding a property to that data class would change the signature of its generated `copy()`, which released
 * PolyPlus builds call directly (see [UITheme.withShapes]), so anything a theme carries beyond the rendering
 * tokens is keyed by the theme id here instead.
 */
object UIThemeExtras {
    data class Extras(
        val description: String? = null,
        val preview: String? = null,
        val accent: Color? = null,
        val custom: Boolean = false,
        val minecraft: Boolean = false,
    )

    private val extras = ConcurrentHashMap<String, Extras>()

    fun put(id: String, extras: Extras) {
        this.extras[id] = extras
    }

    fun description(id: String): String? = extras[id]?.description

    fun preview(id: String): String? = extras[id]?.preview

    fun accent(id: String): Color? = extras[id]?.accent

    fun isCustom(id: String): Boolean = extras[id]?.custom == true

    fun isMinecraftStyle(id: String): Boolean = extras[id]?.minecraft == true

    fun forget(id: String) {
        extras.remove(id)
    }
}

private fun brandedLogoPath(theme: UITheme): String = OneConfigTheme.logoOverride ?: theme.branding.logoPath
