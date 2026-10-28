package org.polyfrost.oneconfig.internal;

import org.polyfrost.compose.render.PolyColor;
import org.polyfrost.oneconfig.api.config.v1.Config;
import org.polyfrost.oneconfig.api.config.v1.annotations.Color;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;
import org.polyfrost.oneconfig.api.config.v1.annotations.Text;

public class ThemeConfig extends Config {
    /**
     * The id of the active theme, such as {@code zordyx-dark}.
     * <p>
     * Configurations written before themes had ids hold a display name here, so {@code ThemeRegistry} matches a
     * theme by id and then by name, and falls back to Zordyx Dark when neither matches. Written to this mod's own
     * theme file, which no longer shares OneConfig's.
     */
    @Text(title = "Active Theme")
    public static String activeTheme = "zordyx-dark";

    @Color(title = "Accent Color", description = "The accent color of the Zordyx Client interface", icon = "paintbrush", gradient = true)
    public static PolyColor accentColor = new PolyColor(0xFF2ECC40);

    @Switch(title = "Animations", description = "Smooth, clean animations in the Zordyx Client interface", icon = "refresh")
    public static boolean animations = true;

    public ThemeConfig() {
        super(ZordyxClientInfo.THEMES_ID, "assets/oneconfig/brand/oneconfig-icon.svg", "Themes", Category.QOL);
    }

    @Override
    protected void initialize(boolean byConfigManager) {
        super.initialize(byConfigManager);
        hideIf("activeTheme", () -> true);
    }
}
