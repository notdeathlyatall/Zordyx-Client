package org.polyfrost.oneconfig.internal;

/**
 * The identity of Zordyx Client, kept in one place so no part of the code has to guess which mod it belongs to.
 * <p>
 * Zordyx Client is a fork of OneConfig and keeps talking the OneConfig API: the Java packages, the
 * {@code assets/oneconfig} resource namespace and the mod ids third-party mods depend on are all preserved. What
 * must <b>not</b> be shared is this mod's own identity, because a launcher, Fabric Loader and Minecraft itself key
 * mods off their declared ids. Two jars both claiming {@code oneconfig} are read as one mod, which is exactly why
 * the launcher could not tell Zordyx Client and OneConfig apart and why their menus fought over the same state.
 * <p>
 * {@link #MOD_ID} and {@link #PLATFORM_MOD_ID} are therefore unique to this fork, while
 * {@link #UPSTREAM_MOD_ID} and {@link #UPSTREAM_PLATFORM_MOD_ID} stay declared in the mod metadata's
 * {@code provides} list so that mods depending on OneConfig still resolve against this mod instead of failing to
 * load. See {@code bootstrap/src/main/resources/fabric.mod.json}.
 */
public final class ZordyxClientInfo {
    private ZordyxClientInfo() {
    }

    /** The id of the shipped bootstrap mod, and the identity a launcher or Mod Menu shows for this mod. */
    public static final String MOD_ID = "zordyx_client";

    /** The id of the platform module nested in the bootstrap jar (see {@code minecraft/src/main/resources}). */
    public static final String PLATFORM_MOD_ID = "zordyx_client_v1";

    /**
     * The upstream OneConfig id, declared through {@code provides} so {@code depends: oneconfig} in third-party mods
     * keeps resolving. It is also how this mod detects the real upstream jar, which cannot share a game instance
     * with this fork because both ship the same {@code org.polyfrost.oneconfig} classes.
     */
    public static final String UPSTREAM_MOD_ID = "oneconfig";

    /** The upstream platform module id, provided by this fork's platform module for the same reason. */
    public static final String UPSTREAM_PLATFORM_MOD_ID = "oneconfigv1";

    /** The id earlier versions of this mod used as a fallback namespace, still provided for compatibility. */
    public static final String LEGACY_BOOTSTRAP_MOD_ID = "oneconfigbootstrap";

    /**
     * The namespace for Minecraft resources (assets, lang, sounds and the mixin package). It stays {@code oneconfig}
     * on purpose: renaming it would break every translation key and sound id third-party mods and resource packs
     * reference.
     */
    public static final String RESOURCE_NAMESPACE = "oneconfig";

    /** The namespace for ids this mod registers itself, such as Minecraft keybind categories. */
    public static final String REGISTRY_NAMESPACE = "zordyx_client";

    /**
     * The directory this mod keeps its own files in, relative to the game directory.
     * <p>
     * Must stay in sync with the internal directory in
     * {@code org.polyfrost.oneconfig.api.config.v1.ConfigManager}, which cannot depend on this module.
     */
    public static final String DATA_DIR = "zordyx_client";

    /** The file id of this mod's own preferences, unique so it cannot collide with upstream OneConfig's. */
    public static final String PREFERENCES_ID = "zordyx-client.json";

    /** The file id of this mod's theme preferences, kept separate from the rest of the preferences. */
    public static final String THEMES_ID = "zordyx-themes.json";

    /** The preference file id used before this mod stopped sharing OneConfig's configuration namespace. */
    public static final String LEGACY_PREFERENCES_ID = "oneconfig.json";

    /** The theme file id used before this mod stopped sharing OneConfig's configuration namespace. */
    public static final String LEGACY_THEMES_ID = "themes.json";

    /** The directory custom themes are loaded from, inside {@link #DATA_DIR}. */
    public static final String THEMES_DIR = "themes";
}
