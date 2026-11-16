package org.polyfrost.oneconfig.internal.combat.aim

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.polyfrost.oneconfig.internal.ui.zordyx.AimAssistMode
import org.polyfrost.oneconfig.internal.ui.zordyx.AimAssistSettings
import org.polyfrost.oneconfig.internal.ui.zordyx.AimAssistTargetArea
import org.polyfrost.oneconfig.internal.ui.zordyx.AimAssistTargetMode

class AimAssistConfigStoreTest {
    @TempDir
    lateinit var directory: Path

    @Test
    fun configurationSurvivesARoundTrip() {
        val store = AimAssistConfigStore(directory.resolve("aim-assist"))
        val settings = AimAssistSettings(
            mode = AimAssistMode.Adaptive,
            targetPlayers = false,
            targetMobs = false,
            targetAnimals = true,
            ignoreInvisible = false,
            ignoreNaked = true,
            ignoreBehindWalls = true,
            requireMouseDown = true,
            strafeIncrease = 40f,
            checkBlockBreak = false,
            breakBlocksWhitelist = "minecraft:stone, 1:0",
            aimVertically = true,
            verticalSpeed = 20f,
            horizontalSpeed = 70f,
            maxAngle = 45f,
            distance = 6.5f,
            limitToItems = "swords, slot 2, 35:14",
            targetArea = AimAssistTargetArea.Closest,
            targetMode = AimAssistTargetMode.Armor,
        ).normalized()

        store.save(AimAssistStoredConfig(enabled = true, settings = settings))
        val loaded = store.load()

        assertTrue(loaded.enabled)
        assertEquals(settings, loaded.settings)
    }

    @Test
    fun aMissingFileLoadsDefaults() {
        val loaded = AimAssistConfigStore(directory.resolve("never-written")).load()

        assertFalse(loaded.enabled)
        assertEquals(AimAssistSettings(), loaded.settings)
    }

    @Test
    fun invalidValuesFallBackToDefaultsAndAreClamped() {
        val file = directory.resolve("aim-assist")
        Files.writeString(
            file,
            """
            mode=NotAMode
            targetPlayers=perhaps
            distance=999
            maxAngle=-30
            horizontalSpeed=NaN
            targetMode=Health
            unknownKey=whatever
            this line has no separator
            """.trimIndent(),
            StandardCharsets.UTF_8,
        )

        val loaded = AimAssistConfigStore(file).load()

        assertEquals(AimAssistMode.Simple, loaded.settings.mode)
        assertTrue(loaded.settings.targetPlayers)
        assertEquals(8f, loaded.settings.distance)
        assertEquals(0f, loaded.settings.maxAngle)
        assertEquals(55f, loaded.settings.horizontalSpeed)
        assertEquals(AimAssistTargetMode.Health, loaded.settings.targetMode)
        assertFalse(loaded.enabled)
    }

    @Test
    fun textEntriesKeepNewlinesAndBackslashes() {
        val store = AimAssistConfigStore(directory.resolve("aim-assist"))
        val settings = AimAssistSettings(
            breakBlocksWhitelist = "stone\ndirt\\test",
            limitToItems = "swords,\nblocks",
        )

        store.save(AimAssistStoredConfig(settings = settings))
        val loaded = store.load().settings

        assertEquals("stone\ndirt\\test", loaded.breakBlocksWhitelist)
        assertEquals("swords,\nblocks", loaded.limitToItems)
    }
}
