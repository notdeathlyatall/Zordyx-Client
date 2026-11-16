package org.polyfrost.oneconfig.internal.combat.aim

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import org.polyfrost.oneconfig.api.config.v1.ConfigManager
import org.polyfrost.oneconfig.internal.ui.zordyx.AimAssistMode
import org.polyfrost.oneconfig.internal.ui.zordyx.AimAssistSettings
import org.polyfrost.oneconfig.internal.ui.zordyx.AimAssistTargetArea
import org.polyfrost.oneconfig.internal.ui.zordyx.AimAssistTargetMode
import org.slf4j.LoggerFactory

/** Everything AimAssist keeps between sessions. */
data class AimAssistStoredConfig(
    val enabled: Boolean = false,
    val settings: AimAssistSettings = AimAssistSettings(),
)

/**
 * Reads and writes the AimAssist configuration.
 *
 * The format is the same plain `key=value` file the rest of the client's internal stores use, so it needs
 * no extra dependency and a hand-edited or older file still loads: unknown keys are ignored, missing keys
 * keep their default, and every numeric value is normalized into its configured range.
 */
class AimAssistConfigStore(private val file: Path) {
    private val logger = LoggerFactory.getLogger("OneConfig/AimAssistConfig")

    fun load(): AimAssistStoredConfig {
        return try {
            if (!Files.isRegularFile(file)) return AimAssistStoredConfig()
            decode(Files.readString(file, StandardCharsets.UTF_8))
        } catch (e: Exception) {
            logger.error("Failed to load AimAssist configuration", e)
            AimAssistStoredConfig()
        }
    }

    fun save(config: AimAssistStoredConfig) {
        try {
            file.parent?.let { Files.createDirectories(it) }
            Files.writeString(
                file,
                encode(config),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE,
            )
        } catch (e: Exception) {
            logger.error("Failed to save AimAssist configuration", e)
        }
    }

    companion object {
        const val FILE_NAME = "aim-assist"

        fun defaultPath(): Path = ConfigManager.internal().folder.resolve(FILE_NAME)

        fun encode(config: AimAssistStoredConfig): String {
            val settings = config.settings.normalized()
            val values = linkedMapOf(
                "version" to "1",
                "enabled" to config.enabled.toString(),
                "mode" to settings.mode.name,
                "targetPlayers" to settings.targetPlayers.toString(),
                "targetMobs" to settings.targetMobs.toString(),
                "targetAnimals" to settings.targetAnimals.toString(),
                "ignoreInvisible" to settings.ignoreInvisible.toString(),
                "ignoreNaked" to settings.ignoreNaked.toString(),
                "ignoreBehindWalls" to settings.ignoreBehindWalls.toString(),
                "requireMouseDown" to settings.requireMouseDown.toString(),
                "strafeIncrease" to settings.strafeIncrease.toString(),
                "checkBlockBreak" to settings.checkBlockBreak.toString(),
                "breakBlocksWhitelist" to settings.breakBlocksWhitelist,
                "aimVertically" to settings.aimVertically.toString(),
                "verticalSpeed" to settings.verticalSpeed.toString(),
                "horizontalSpeed" to settings.horizontalSpeed.toString(),
                "maxAngle" to settings.maxAngle.toString(),
                "distance" to settings.distance.toString(),
                "limitToItems" to settings.limitToItems,
                "targetArea" to settings.targetArea.name,
                "targetMode" to settings.targetMode.name,
            )
            return values.entries.joinToString("\n", postfix = "\n") { (key, value) ->
                "$key=${escape(value)}"
            }
        }

        fun decode(raw: String): AimAssistStoredConfig {
            val values = HashMap<String, String>()
            raw.lineSequence().forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEach
                val separator = trimmed.indexOf('=')
                if (separator <= 0) return@forEach
                values[trimmed.substring(0, separator)] = unescape(trimmed.substring(separator + 1))
            }

            val defaults = AimAssistSettings()
            val settings = AimAssistSettings(
                mode = enumValue(values["mode"], defaults.mode),
                targetPlayers = boolean(values["targetPlayers"], defaults.targetPlayers),
                targetMobs = boolean(values["targetMobs"], defaults.targetMobs),
                targetAnimals = boolean(values["targetAnimals"], defaults.targetAnimals),
                ignoreInvisible = boolean(values["ignoreInvisible"], defaults.ignoreInvisible),
                ignoreNaked = boolean(values["ignoreNaked"], defaults.ignoreNaked),
                ignoreBehindWalls = boolean(values["ignoreBehindWalls"], defaults.ignoreBehindWalls),
                requireMouseDown = boolean(values["requireMouseDown"], defaults.requireMouseDown),
                strafeIncrease = float(values["strafeIncrease"], defaults.strafeIncrease),
                checkBlockBreak = boolean(values["checkBlockBreak"], defaults.checkBlockBreak),
                breakBlocksWhitelist = values["breakBlocksWhitelist"] ?: defaults.breakBlocksWhitelist,
                aimVertically = boolean(values["aimVertically"], defaults.aimVertically),
                verticalSpeed = float(values["verticalSpeed"], defaults.verticalSpeed),
                horizontalSpeed = float(values["horizontalSpeed"], defaults.horizontalSpeed),
                maxAngle = float(values["maxAngle"], defaults.maxAngle),
                distance = float(values["distance"], defaults.distance),
                limitToItems = values["limitToItems"] ?: defaults.limitToItems,
                targetArea = enumValue(values["targetArea"], defaults.targetArea),
                targetMode = enumValue(values["targetMode"], defaults.targetMode),
            ).normalized()

            return AimAssistStoredConfig(
                enabled = boolean(values["enabled"], false),
                settings = settings,
            )
        }

        private fun boolean(raw: String?, fallback: Boolean): Boolean =
            raw?.toBooleanStrictOrNull() ?: fallback

        private fun float(raw: String?, fallback: Float): Float =
            raw?.toFloatOrNull()?.takeIf { it.isFinite() } ?: fallback

        private inline fun <reified T : Enum<T>> enumValue(raw: String?, fallback: T): T {
            if (raw == null) return fallback
            return enumValues<T>().firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: fallback
        }

        private fun escape(value: String): String = buildString(value.length) {
            value.forEach { char ->
                when (char) {
                    '\\' -> append("\\\\")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    else -> append(char)
                }
            }
        }

        private fun unescape(value: String): String = buildString(value.length) {
            var escaped = false
            value.forEach { char ->
                if (escaped) {
                    append(
                        when (char) {
                            'n' -> '\n'
                            'r' -> '\r'
                            else -> char
                        },
                    )
                    escaped = false
                } else if (char == '\\') {
                    escaped = true
                } else {
                    append(char)
                }
            }
            if (escaped) append('\\')
        }
    }
}
