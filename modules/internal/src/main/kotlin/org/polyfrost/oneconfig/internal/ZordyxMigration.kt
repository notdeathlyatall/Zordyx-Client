package org.polyfrost.oneconfig.internal

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.concurrent.atomic.AtomicBoolean
import org.apache.logging.log4j.LogManager
import org.polyfrost.oneconfig.api.config.v1.ConfigManager

/**
 * Moves this mod onto its own on-disk namespace without touching the state it used to share with OneConfig.
 *
 * Until this fork stopped sharing OneConfig's configuration namespace, its internal files lived in `oneconfig/`
 * and its preferences and themes were `oneconfig.json` and `themes.json` inside the active profile directory. Both
 * are still upstream OneConfig's own names, so writing them would have overwritten the user's OneConfig settings
 * and vice versa.
 *
 * Everything here is a **copy**: the legacy files are read and never renamed, moved or deleted, so a user who also
 * runs upstream OneConfig keeps that mod's state intact. Nothing is copied over an existing file, which makes the
 * migration idempotent and safe to run on every launch.
 */
object ZordyxMigration {
    private val LOGGER = LogManager.getLogger("OneConfig/Migration")

    /** The directory this mod used before it had a namespace of its own. */
    private val LEGACY_DATA_DIR: Path = Paths.get(ZordyxClientInfo.UPSTREAM_MOD_ID)

    private val ran = AtomicBoolean(false)

    /**
     * Copies anything this mod still needs from the legacy locations. Called once, before any config is read, so a
     * single pass over the profiles that exist at startup is enough.
     */
    @JvmStatic
    fun run() {
        if (!ran.compareAndSet(false, true)) return
        try {
            migrateDataDirectory()
            migrateOwnedConfigs()
        } catch (t: Throwable) {
            // a failed migration must never stop the game: the new namespace just starts from defaults
            LOGGER.error("Failed to migrate the pre-separation Zordyx Client state; the legacy files were left untouched", t)
        }
    }

    private fun migrateDataDirectory() {
        // resolving this also creates the new directory, so only its contents are compared
        val target = ConfigManager.internal().folder
        if (!Files.isDirectory(LEGACY_DATA_DIR)) return
        if (target.toAbsolutePath().normalize() == LEGACY_DATA_DIR.toAbsolutePath().normalize()) return
        var copied = 0
        Files.list(LEGACY_DATA_DIR).use { entries ->
            for (entry in entries) {
                val destination = target.resolve(entry.fileName.toString())
                if (!Files.exists(destination)) copied += copy(entry, destination)
            }
        }
        if (copied > 0) {
            LOGGER.info("Copied {} file(s) from {} to {}", copied, LEGACY_DATA_DIR, target)
        }
    }

    private fun migrateOwnedConfigs() {
        migrateOwnedConfigs(ConfigManager.profileDir(""))
        val profiles = ConfigManager.PROFILES_DIR
        if (!Files.isDirectory(profiles)) return
        Files.list(profiles).use { entries ->
            for (entry in entries) {
                if (!Files.isDirectory(entry) || entry.fileName.toString().startsWith(".")) continue
                migrateOwnedConfigs(entry)
            }
        }
    }

    private fun migrateOwnedConfigs(directory: Path) {
        copyIfMissing(
            directory.resolve(ZordyxClientInfo.LEGACY_PREFERENCES_ID),
            directory.resolve(ZordyxClientInfo.PREFERENCES_ID),
        )
        copyIfMissing(
            directory.resolve(ZordyxClientInfo.LEGACY_THEMES_ID),
            directory.resolve(ZordyxClientInfo.THEMES_ID),
        )
    }

    private fun copyIfMissing(legacy: Path, destination: Path) {
        if (Files.exists(destination) || !Files.isRegularFile(legacy)) return
        copy(legacy, destination)
        LOGGER.info("Migrated {} to {}", legacy, destination)
    }

    /** Returns how many files were copied, or 0 when [from] is not readable. */
    private fun copy(from: Path, to: Path): Int {
        var copied = 0
        if (Files.isDirectory(from)) {
            Files.createDirectories(to)
            Files.list(from).use { entries ->
                for (entry in entries) copied += copy(entry, to.resolve(entry.fileName.toString()))
            }
        } else if (Files.isRegularFile(from)) {
            val result = runCatching {
                to.parent?.let(Files::createDirectories)
                Files.copy(from, to, StandardCopyOption.COPY_ATTRIBUTES)
            }
            if (result.isSuccess) copied = 1
            else LOGGER.warn("Could not copy {} to {}", from, to, result.exceptionOrNull())
        }
        return copied
    }
}
