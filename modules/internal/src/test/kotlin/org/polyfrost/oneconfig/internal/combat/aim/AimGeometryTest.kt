package org.polyfrost.oneconfig.internal.combat.aim

import kotlin.math.abs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AimGeometryTest {
    @Test
    fun distanceUsesAllThreeAxes() {
        val from = AimVec3(0.0, 0.0, 0.0)

        assertEquals(5.0, from.distanceTo(AimVec3(3.0, 4.0, 0.0)), 1e-6)
        assertEquals(2.0, from.distanceTo(AimVec3(0.0, 0.0, 2.0)), 1e-6)
    }

    @Test
    fun yawFollowsTheMinecraftConvention() {
        val eye = AimVec3(0.0, 0.0, 0.0)

        assertEquals(0f, AimAngles.yawTo(eye, AimVec3(0.0, 0.0, 1.0)), 1e-3f)
        assertEquals(90f, AimAngles.yawTo(eye, AimVec3(-1.0, 0.0, 0.0)), 1e-3f)
        assertEquals(-90f, AimAngles.yawTo(eye, AimVec3(1.0, 0.0, 0.0)), 1e-3f)
        assertEquals(180f, abs(AimAngles.yawTo(eye, AimVec3(0.0, 0.0, -1.0))), 1e-3f)
    }

    @Test
    fun pitchIsPositiveBelowTheEye() {
        val eye = AimVec3(0.0, 2.0, 0.0)

        assertTrue(AimAngles.pitchTo(eye, AimVec3(0.0, 1.0, 2.0)) > 0f)
        assertTrue(AimAngles.pitchTo(eye, AimVec3(0.0, 3.0, 2.0)) < 0f)
        assertEquals(0f, AimAngles.pitchTo(eye, AimVec3(0.0, 2.0, 2.0)), 1e-3f)
    }

    @Test
    fun wrappingTreatsBothSidesOfTheSeamAsAdjacent() {
        assertEquals(-170f, AimAngles.wrapDegrees(190f), 1e-3f)
        assertEquals(170f, AimAngles.wrapDegrees(-190f), 1e-3f)
        assertEquals(0f, AimAngles.wrapDegrees(Float.NaN))
        assertEquals(2f, abs(AimAngles.wrapDegrees(179f - (-179f))), 1e-3f)
    }

    @Test
    fun angleBetweenIsZeroForTheSameViewAnd180ForTheOpposite() {
        assertEquals(0f, AimAngles.angleBetween(45f, 10f, 45f, 10f), 1e-3f)
        assertEquals(180f, AimAngles.angleBetween(0f, 0f, 180f, 0f), 1e-2f)
        assertEquals(2f, AimAngles.angleBetween(179f, 0f, -179f, 0f), 1e-2f)
    }

    @Test
    fun closestPointClampsOntoTheBox() {
        val box = AimBox.around(AimVec3(0.0, 1.0, 4.0), width = 0.6, height = 1.8)
        val eye = AimVec3(0.0, 1.62, 0.0)

        val closest = box.closestPointTo(eye)
        assertEquals(3.7, closest.z, 1e-6)
        assertEquals(1.62, closest.y, 1e-6)
        assertEquals(4.0, box.center.z, 1e-6)
        assertEquals(1.9, box.center.y, 1e-6)

        val inside = AimVec3(0.0, 1.5, 4.0)
        assertEquals(inside, box.closestPointTo(inside))
    }
}
