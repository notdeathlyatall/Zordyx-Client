package org.polyfrost.oneconfig.internal.combat.aim

import org.polyfrost.oneconfig.internal.ui.zordyx.AimAssistSettings

/**
 * The stateful heart of AimAssist.
 *
 * One instance owns the analysis state a tick needs: the target kept while Require Mouse Down is down,
 * and the parsed item and block filters. Everything else is a pure function of the settings and the
 * world snapshot, so the engine never holds an entity, a renderer or the settings screen.
 */
class AimAnalysisEngine(private val selector: AimTargetSelector = AimTargetSelector()) {
    private var latchedTargetId: Int? = null

    private var itemFilterSource: String? = null
    private var itemFilter = AimItemFilter.parse("")
    private var blockFilterSource: String? = null
    private var blockFilter = AimBlockFilter.parse("")

    /** The target kept for the current attack-button press, if any. */
    val latched: Int? get() = latchedTargetId

    fun reset() {
        latchedTargetId = null
    }

    fun analyze(
        world: AimWorld,
        settings: AimAssistSettings,
        enabled: Boolean = true,
    ): AimAnalysis {
        val config = settings.normalized()

        if (!enabled) {
            latchedTargetId = null
            return AimAnalysis(AimStatus.Disabled)
        }

        val breaking = world.breakingBlock
        if (config.checkBlockBreak && breaking != null && !blockFilter(config.breakBlocksWhitelist).matches(breaking)) {
            latchedTargetId = null
            return AimAnalysis(AimStatus.PausedBlockBreak)
        }

        if (!itemFilter(config.limitToItems).matches(world.heldItem)) {
            latchedTargetId = null
            return AimAnalysis(AimStatus.PausedItemFilter)
        }

        if (config.requireMouseDown && !world.mouseDown) {
            latchedTargetId = null
            return AimAnalysis(AimStatus.PausedMouseReleased)
        }

        val playerStrafing = AimAngles.horizontalSpeed(world.playerVelocity) > STRAFE_EPSILON
        val selection = AimSelectionConfig(
            mode = config.mode,
            targetMode = config.targetMode,
            targetArea = config.targetArea,
            maxAngle = config.maxAngle,
            maxDistance = config.distance,
            strafeMultiplier = if (playerStrafing) 1f + config.strafeIncrease / 100f else 1f,
        )

        val set = selector.candidates(world, selection) { eligible(it, config) }
        val chosen = selector.best(set.candidates, if (config.requireMouseDown) latchedTargetId else null)
        if (config.requireMouseDown) {
            latchedTargetId = chosen?.target?.id
        } else {
            latchedTargetId = null
        }

        if (chosen == null) {
            return AimAnalysis(
                status = AimStatus.NoTarget,
                considered = set.considered,
                eligible = 0,
                strafeMultiplier = selection.strafeMultiplier,
            )
        }

        val targetStrafing = chosen.targetHorizontalSpeed > STRAFE_EPSILON
        val strafeApplied = playerStrafing || targetStrafing
        val strafeMultiplier = if (strafeApplied) 1f + config.strafeIncrease / 100f else 1f
        val horizontalSpeed = (config.horizontalSpeed * strafeMultiplier).coerceIn(0f, 100f)
        val verticalSpeed = if (config.aimVertically) {
            (config.verticalSpeed * strafeMultiplier).coerceIn(0f, 100f)
        } else {
            0f
        }

        return AimAnalysis(
            status = AimStatus.Tracking,
            target = chosen,
            considered = set.considered,
            eligible = set.candidates.size,
            horizontalSpeed = horizontalSpeed,
            verticalSpeed = verticalSpeed,
            strafeMultiplier = strafeMultiplier,
        )
    }

    private fun eligible(target: AimTarget, settings: AimAssistSettings): Boolean {
        if (!target.alive) return false
        val allowed = when (target.kind) {
            AimEntityKind.Player -> settings.targetPlayers
            AimEntityKind.HostileMob -> settings.targetMobs
            AimEntityKind.Animal -> settings.targetAnimals
            AimEntityKind.Other -> false
        }
        if (!allowed) return false
        if (settings.ignoreInvisible && target.invisible) return false
        // "Naked" only describes a player with no armour; applying it to every mob would filter them all.
        if (settings.ignoreNaked && target.kind == AimEntityKind.Player && target.armor <= 0) return false
        if (settings.ignoreBehindWalls && !target.lineOfSight) return false
        return true
    }

    private fun itemFilter(raw: String): AimItemFilter {
        if (raw != itemFilterSource) {
            itemFilterSource = raw
            itemFilter = AimItemFilter.parse(raw)
        }
        return itemFilter
    }

    private fun blockFilter(raw: String): AimBlockFilter {
        if (raw != blockFilterSource) {
            blockFilterSource = raw
            blockFilter = AimBlockFilter.parse(raw)
        }
        return blockFilter
    }

    private companion object {
        const val STRAFE_EPSILON = 0.05f
    }
}
