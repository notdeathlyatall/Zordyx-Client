package org.polyfrost.oneconfig.internal.combat.aim

import kotlin.math.abs
import org.polyfrost.oneconfig.internal.ui.zordyx.AimAssistMode
import org.polyfrost.oneconfig.internal.ui.zordyx.AimAssistTargetArea
import org.polyfrost.oneconfig.internal.ui.zordyx.AimAssistTargetMode

/** The geometry and ranking inputs of one analysis pass. */
data class AimSelectionConfig(
    val mode: AimAssistMode,
    val targetMode: AimAssistTargetMode,
    val targetArea: AimAssistTargetArea,
    val maxAngle: Float,
    val maxDistance: Float,
    /** The strafe multiplier, used to weight angular precision in Adaptive mode. */
    val strafeMultiplier: Float = 1f,
)

data class AimCandidateSet(val candidates: List<AimCandidate>, val considered: Int)

/**
 * Turns a world snapshot into scored candidates.
 *
 * This class knows nothing about the settings screen, rendering or the entity source: it is given an
 * inclusion predicate and a small value configuration, which keeps selection deterministic and easy to
 * test. Ties are always broken by distance and then entity id so the same frame always picks the same
 * target.
 */
class AimTargetSelector {
    fun candidates(
        world: AimWorld,
        config: AimSelectionConfig,
        include: (AimTarget) -> Boolean,
    ): AimCandidateSet {
        val candidates = ArrayList<AimCandidate>(world.targets.size)
        var considered = 0
        val maxDistance = config.maxDistance.coerceAtLeast(0f)
        val maxAngle = config.maxAngle.coerceIn(0f, 180f)

        for (target in world.targets) {
            considered++
            if (!target.alive || !include(target)) continue

            val aimPoint = when (config.targetArea) {
                AimAssistTargetArea.Center -> target.box.center
                AimAssistTargetArea.Closest -> target.box.closestPointTo(world.eye)
            }
            val distance = world.eye.distanceTo(aimPoint).toFloat()
            if (distance > maxDistance) continue

            val yaw = AimAngles.yawTo(world.eye, aimPoint)
            val pitch = AimAngles.pitchTo(world.eye, aimPoint)
            val angle = AimAngles.angleBetween(world.yaw, world.pitch, yaw, pitch)
            if (angle > maxAngle) continue

            val yawDelta = AimAngles.wrapDegrees(yaw - world.yaw)
            val pitchDelta = AimAngles.wrapDegrees(pitch - world.pitch)
            val movement = AimAngles.horizontalSpeed(target.velocity)
            candidates += AimCandidate(
                target = target,
                aimPoint = aimPoint,
                distance = distance,
                angle = angle,
                yawDelta = yawDelta,
                pitchDelta = pitchDelta,
                targetHorizontalSpeed = movement,
                score = score(target, distance, angle, yawDelta, movement, config),
            )
        }

        return AimCandidateSet(candidates, considered)
    }

    /** The candidate with the highest score, or the latched target while it remains a candidate. */
    fun best(candidates: List<AimCandidate>, latchedTargetId: Int? = null): AimCandidate? {
        if (candidates.isEmpty()) return null
        if (latchedTargetId != null) {
            candidates.firstOrNull { it.target.id == latchedTargetId }?.let { return it }
        }
        return candidates.minWithOrNull(
            compareByDescending<AimCandidate> { it.score }
                .thenBy { it.distance }
                .thenBy { it.target.id },
        )
    }

    private fun score(
        target: AimTarget,
        distance: Float,
        angle: Float,
        yawDelta: Float,
        movement: Float,
        config: AimSelectionConfig,
    ): Float {
        val priority = priorityScore(target, distance, yawDelta, config)
        if (config.mode != AimAssistMode.Adaptive) return priority

        val precision = (1f - angle / config.maxAngle.coerceAtLeast(1f)).coerceIn(0f, 1f)
        val stability = 1f / (1f + movement * 4f)
        val strafeWeight = config.strafeMultiplier.coerceIn(1f, 2f)
        return (priority * 0.7f + precision * 0.2f * strafeWeight + stability * 0.1f).coerceIn(0f, 1.5f)
    }

    private fun priorityScore(
        target: AimTarget,
        distance: Float,
        yawDelta: Float,
        config: AimSelectionConfig,
    ): Float = when (config.targetMode) {
        AimAssistTargetMode.Distance ->
            1f - (distance / config.maxDistance.coerceAtLeast(0.001f)).coerceIn(0f, 1f)
        AimAssistTargetMode.Yaw ->
            1f - (abs(yawDelta) / config.maxAngle.coerceAtLeast(1f)).coerceIn(0f, 1f)
        AimAssistTargetMode.Armor ->
            1f - (target.armor.coerceAtLeast(0) / 20f).coerceIn(0f, 1f)
        AimAssistTargetMode.Threat ->
            target.threat.coerceIn(0f, 1f)
        AimAssistTargetMode.Health ->
            if (target.maxHealth <= 0f) 1f
            else 1f - (target.health.coerceAtLeast(0f) / target.maxHealth).coerceIn(0f, 1f)
    }
}
