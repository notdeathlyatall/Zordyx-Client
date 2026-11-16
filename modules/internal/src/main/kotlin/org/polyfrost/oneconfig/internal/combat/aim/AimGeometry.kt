package org.polyfrost.oneconfig.internal.combat.aim

import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

/** An immutable point or velocity in world space. */
data class AimVec3(val x: Double, val y: Double, val z: Double) {
    operator fun plus(other: AimVec3): AimVec3 = AimVec3(x + other.x, y + other.y, z + other.z)

    operator fun minus(other: AimVec3): AimVec3 = AimVec3(x - other.x, y - other.y, z - other.z)

    operator fun times(scale: Double): AimVec3 = AimVec3(x * scale, y * scale, z * scale)

    fun distanceTo(other: AimVec3): Double {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    fun horizontalLength(): Double = hypot(x, z)

    companion object {
        val ZERO = AimVec3(0.0, 0.0, 0.0)
    }
}

/**
 * An axis-aligned entity bounding box.
 *
 * The engine only reads boxes, so a target can be represented without holding an entity reference and an
 * entity that unloads between ticks cannot leave a stale object behind.
 */
data class AimBox(
    val minX: Double,
    val minY: Double,
    val minZ: Double,
    val maxX: Double,
    val maxY: Double,
    val maxZ: Double,
) {
    val center: AimVec3
        get() = AimVec3((minX + maxX) * 0.5, (minY + maxY) * 0.5, (minZ + maxZ) * 0.5)

    fun closestPointTo(point: AimVec3): AimVec3 = AimVec3(
        point.x.coerceIn(minX, maxX),
        point.y.coerceIn(minY, maxY),
        point.z.coerceIn(minZ, maxZ),
    )

    companion object {
        /** A box standing on [feet] with the given width and height, centred horizontally on it. */
        fun around(feet: AimVec3, width: Double, height: Double): AimBox {
            val halfWidth = width.coerceAtLeast(0.0) * 0.5
            return AimBox(
                minX = feet.x - halfWidth,
                minY = feet.y,
                minZ = feet.z - halfWidth,
                maxX = feet.x + halfWidth,
                maxY = feet.y + height.coerceAtLeast(0.0),
                maxZ = feet.z + halfWidth,
            )
        }
    }
}

/**
 * Minecraft's yaw and pitch conventions.
 *
 * Yaw 0 looks towards positive Z and increases towards negative X; positive pitch looks down. The
 * helpers take points so callers never have to repeat the spherical conversion, and every angular
 * difference is wrapped so a target just either side of the -180/180 seam is treated as adjacent.
 */
object AimAngles {
    fun viewVector(yawDegrees: Float, pitchDegrees: Float): AimVec3 {
        val yaw = Math.toRadians(yawDegrees.toDouble())
        val pitch = Math.toRadians(pitchDegrees.toDouble())
        return AimVec3(-sin(yaw) * cos(pitch), -sin(pitch), cos(yaw) * cos(pitch))
    }

    fun yawTo(from: AimVec3, to: AimVec3): Float =
        Math.toDegrees(atan2(-(to.x - from.x), to.z - from.z)).toFloat()

    fun pitchTo(from: AimVec3, to: AimVec3): Float {
        val horizontal = hypot(to.x - from.x, to.z - from.z)
        return Math.toDegrees(-atan2(to.y - from.y, horizontal)).toFloat()
    }

    /** The true angle between two view directions, in degrees. */
    fun angleBetween(yawA: Float, pitchA: Float, yawB: Float, pitchB: Float): Float {
        val a = viewVector(yawA, pitchA)
        val b = viewVector(yawB, pitchB)
        val dot = (a.x * b.x + a.y * b.y + a.z * b.z).coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(dot)).toFloat()
    }

    /** [degrees] wrapped into the range [-180, 180). */
    fun wrapDegrees(degrees: Float): Float {
        if (!degrees.isFinite()) return 0f
        var wrapped = degrees % 360f
        if (wrapped >= 180f) wrapped -= 360f
        if (wrapped < -180f) wrapped += 360f
        return wrapped
    }

    fun horizontalSpeed(velocity: AimVec3): Float = hypot(velocity.x, velocity.z).toFloat()
}
