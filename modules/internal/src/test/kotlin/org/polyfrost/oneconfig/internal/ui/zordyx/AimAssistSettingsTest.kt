package org.polyfrost.oneconfig.internal.ui.zordyx

import kotlinx.serialization.descriptors.elementNames
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Guards the AimAssist settings model: its defaults, the validation every value passes through, the
 * conditional vertical control, the list syntax the text fields document, and the serialized shape the
 * settings would be saved in.
 */
class AimAssistSettingsTest {
    @Test
    fun defaultsAreValidAndStartFromTheDocumentedValues() {
        val settings = AimAssistSettings()

        assertEquals(settings, settings.normalized(), "the defaults must already be inside their ranges")
        assertEquals(AimAssistMode.Simple, settings.mode)
        assertEquals(AimAssistTargetArea.Center, settings.targetArea)
        assertEquals(AimAssistTargetMode.Distance, settings.targetMode)
        assertTrue(settings.targetPlayers, "players are a target by default")
        assertTrue(settings.targetMobs, "hostile mobs are a target by default")
        assertFalse(settings.targetAnimals, "peaceful mobs are off by default")
        assertFalse(settings.aimVertically, "vertical aiming is off by default")
        assertFalse(settings.requireMouseDown)
        assertEquals("", settings.breakBlocksWhitelist)
        assertEquals("", settings.limitToItems)
    }

    @Test
    fun everyNumericDefaultIsInsideItsOwnRange() {
        val settings = AimAssistSettings()

        assertTrue(settings.strafeIncrease in AimAssistRanges.strafeIncrease.min..AimAssistRanges.strafeIncrease.max)
        assertTrue(settings.verticalSpeed in AimAssistRanges.verticalSpeed.min..AimAssistRanges.verticalSpeed.max)
        assertTrue(settings.horizontalSpeed in AimAssistRanges.horizontalSpeed.min..AimAssistRanges.horizontalSpeed.max)
        assertTrue(settings.maxAngle in AimAssistRanges.maxAngle.min..AimAssistRanges.maxAngle.max)
        assertTrue(settings.distance in AimAssistRanges.distance.min..AimAssistRanges.distance.max)
    }

    @Test
    fun valuesOutsideARangeAreClamped() {
        val clamped = AimAssistSettings(horizontalSpeed = 500f, maxAngle = -20f, distance = 99f).normalized()

        assertEquals(100f, clamped.horizontalSpeed)
        assertEquals(0f, clamped.maxAngle)
        assertEquals(8f, clamped.distance)
        assertEquals(
            AimAssistRanges.distance.min,
            AimAssistSettings(distance = Float.NaN).normalized().distance,
            "a value that is not a number falls back to the range's minimum",
        )
    }

    @Test
    fun valuesSnapOntoTheirStep() {
        assertEquals(4.6f, AimAssistRanges.distance.snap(4.567f))
        assertEquals(0f, AimAssistRanges.maxAngle.snap(-30f))
    }

    @Test
    fun valuesAreDisplayedWithTheirUnits() {
        assertEquals("4.5 blocks", AimAssistRanges.distance.display(4.5f))
        assertEquals("25%", AimAssistRanges.strafeIncrease.display(25f))
        assertEquals("90\u00B0", AimAssistRanges.maxAngle.display(90f))
    }

    @Test
    fun verticalSpeedOnlyAppliesWhenVerticalAimingIsOn() {
        assertEquals(0f, AimAssistSettings(aimVertically = false, verticalSpeed = 80f).effectiveVerticalSpeed)
        assertEquals(80f, AimAssistSettings(aimVertically = true, verticalSpeed = 80f).effectiveVerticalSpeed)
    }

    @Test
    fun listEntriesFollowTheDocumentedSyntax() {
        assertEquals(listOf("swords", "slot 1", "35:14"), splitAimAssistEntries("swords, slot 1, 35:14"))
        assertEquals(listOf("food"), splitAimAssistEntries("  food , ,"))
        assertEquals(listOf("hand", "blocks"), splitAimAssistEntries("hand\nblocks"))
        assertEquals(listOf("35"), splitAimAssistEntries("35, 35"), "repeated entries collapse")
        assertTrue(splitAimAssistEntries("").isEmpty())
    }

    @Test
    fun theRawListTextIsKeptAsEntered() {
        val settings = AimAssistSettings(limitToItems = "swords, slot 1 ,35:14").normalized()

        assertEquals("swords, slot 1 ,35:14", settings.limitToItems, "validating numbers must not rewrite a text field")
        assertEquals("stone, dirt", AimAssistSettings(breakBlocksWhitelist = "stone, dirt").normalized().breakBlocksWhitelist)
    }

    @Test
    fun everySettingIsInTheSerializedShape() {
        val names = AimAssistSettings.serializer().descriptor.elementNames.toSet()

        assertEquals(
            setOf(
                "mode", "targetPlayers", "targetMobs", "targetAnimals", "ignoreInvisible", "ignoreNaked",
                "ignoreBehindWalls", "requireMouseDown", "strafeIncrease", "checkBlockBreak",
                "breakBlocksWhitelist", "aimVertically", "verticalSpeed", "horizontalSpeed", "maxAngle",
                "distance", "limitToItems", "targetArea", "targetMode",
            ),
            names,
        )
    }

    @Test
    fun everyRequestedOptionIsOffered() {
        assertEquals(listOf("Simple", "Adaptive"), AimAssistMode.entries.map { it.title })
        assertEquals(listOf("Center", "Closest"), AimAssistTargetArea.entries.map { it.title })
        assertEquals(
            listOf("Distance", "Yaw", "Armor", "Threat", "Health"),
            AimAssistTargetMode.entries.map { it.title },
        )

        assertTrue(AimAssistMode.entries.all { it.description.isNotBlank() })
        assertTrue(AimAssistTargetArea.entries.all { it.description.isNotBlank() })
        assertTrue(AimAssistTargetMode.entries.all { it.description.isNotBlank() })
    }
}
