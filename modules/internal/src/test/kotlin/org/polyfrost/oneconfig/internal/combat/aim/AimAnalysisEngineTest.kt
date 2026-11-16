package org.polyfrost.oneconfig.internal.combat.aim

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.polyfrost.oneconfig.internal.ui.zordyx.AimAssistMode
import org.polyfrost.oneconfig.internal.ui.zordyx.AimAssistSettings
import org.polyfrost.oneconfig.internal.ui.zordyx.AimAssistTargetArea
import org.polyfrost.oneconfig.internal.ui.zordyx.AimAssistTargetMode

class AimAnalysisEngineTest {
    private val engine = AimAnalysisEngine()

    @Test
    fun playersMobsAndAnimalsFollowTheirToggles() {
        val player = target(1, z = 3.0, kind = AimEntityKind.Player)
        val mob = target(2, z = 4.0, kind = AimEntityKind.HostileMob)
        val animal = target(3, z = 2.0, kind = AimEntityKind.Animal)

        val withoutAnimals = engine.analyze(world(player, mob, animal), settings())
        assertEquals(1, withoutAnimals.target?.target?.id)
        assertEquals(2, withoutAnimals.eligible)

        val withAnimals = engine.analyze(world(player, mob, animal), settings(targetAnimals = true))
        assertEquals(3, withAnimals.target?.target?.id)
        assertEquals(3, withAnimals.eligible)

        val playersOnly = engine.analyze(
            world(player, mob, animal),
            settings(targetMobs = false, targetAnimals = false, targetPlayers = true),
        )
        assertEquals(1, playersOnly.target?.target?.id)
        assertEquals(1, playersOnly.eligible)
    }

    @Test
    fun invisibleAndNakedFiltersExcludeTheirTargets() {
        val invisible = target(1, z = 3.0, invisible = true)
        val visible = target(2, z = 5.0)

        assertEquals(
            2,
            engine.analyze(world(invisible, visible), settings(ignoreInvisible = true)).target?.target?.id,
        )
        assertEquals(
            1,
            engine.analyze(world(invisible, visible), settings(ignoreInvisible = false)).target?.target?.id,
        )

        val naked = target(3, z = 3.0, kind = AimEntityKind.Player, armor = 0)
        val armored = target(4, z = 4.0, kind = AimEntityKind.Player, armor = 8)
        val mob = target(5, z = 2.0, kind = AimEntityKind.HostileMob, armor = 0)

        val filtered = engine.analyze(
            world(naked, armored, mob),
            settings(ignoreNaked = true, targetAnimals = false),
        )
        // The unarmoured mob survives: "naked" describes a player without armour.
        assertEquals(5, filtered.target?.target?.id)
        assertEquals(2, filtered.eligible)
    }

    @Test
    fun behindWallsFilterUsesLineOfSight() {
        val hidden = target(1, z = 3.0, lineOfSight = false)
        val visible = target(2, z = 4.0, lineOfSight = true)

        assertEquals(
            2,
            engine.analyze(world(hidden, visible), settings(ignoreBehindWalls = true)).target?.target?.id,
        )
        assertEquals(
            1,
            engine.analyze(world(hidden, visible), settings(ignoreBehindWalls = false)).target?.target?.id,
        )
    }

    @Test
    fun distanceAndAngleLimitsExcludeOutliers() {
        val far = target(1, z = 10.0, lineOfSight = true)
        val near = target(2, z = 4.0)

        assertEquals(
            2,
            engine.analyze(world(far, near), settings(distance = 6f)).target?.target?.id,
        )

        val behind = target(3, z = -4.0)
        val ahead = target(4, z = 4.0)
        assertEquals(
            4,
            engine.analyze(world(behind, ahead), settings(maxAngle = 90f)).target?.target?.id,
        )
        assertEquals(
            3,
            engine.analyze(world(behind, ahead), settings(maxAngle = 180f)).target?.target?.id,
        )
    }

    @Test
    fun everyTargetModePrioritizesTheExpectedTarget() {
        val near = target(1, z = 3.0, armor = 16, health = 20f, threat = 0.1f)
        val priority = target(2, z = 5.0, armor = 2, health = 4f, threat = 0.9f)

        assertEquals(1, choose(AimAssistTargetMode.Distance, near, priority))
        assertEquals(2, choose(AimAssistTargetMode.Armor, near, priority))
        assertEquals(2, choose(AimAssistTargetMode.Threat, near, priority))
        assertEquals(2, choose(AimAssistTargetMode.Health, near, priority))

        val centered = target(3, z = 4.0)
        val offAxis = target(4, x = 2.0, z = 4.0)
        assertEquals(3, choose(AimAssistTargetMode.Yaw, centered, offAxis, mode = AimAssistMode.Simple))
    }

    @Test
    fun adaptiveModeStillTracksAndScoresCandidates() {
        val still = target(1, z = 4.0)
        val moving = target(2, z = 4.5, vx = 0.4)

        val result = engine.analyze(
            world(still, moving),
            settings(mode = AimAssistMode.Adaptive, targetMode = AimAssistTargetMode.Distance),
        )

        assertEquals(AimStatus.Tracking, result.status)
        assertNotNull(result.target)
        assertTrue(result.eligible >= 1)
    }

    @Test
    fun requireMouseDownLatchesUntilRelease() {
        val first = target(1, z = 3.0)
        val second = target(2, z = 5.0)

        val pressed = engine.analyze(world(first, second, mouseDown = true), settings(requireMouseDown = true))
        assertEquals(1, pressed.target?.target?.id)
        assertEquals(1, engine.latched)

        // The latched target is kept even after another candidate becomes nearer.
        val moved = engine.analyze(
            world(target(1, z = 6.0), target(2, z = 2.0), mouseDown = true),
            settings(requireMouseDown = true),
        )
        assertEquals(1, moved.target?.target?.id)

        val released = engine.analyze(world(first, second, mouseDown = false), settings(requireMouseDown = true))
        assertEquals(AimStatus.PausedMouseReleased, released.status)
        assertNull(engine.latched)

        val pressedAgain = engine.analyze(
            world(target(1, z = 6.0), target(2, z = 2.0), mouseDown = true),
            settings(requireMouseDown = true),
        )
        assertEquals(2, pressedAgain.target?.target?.id)
    }

    @Test
    fun latchedTargetRecoversWhenItDisappears() {
        engine.analyze(
            world(target(1, z = 3.0), target(2, z = 5.0), mouseDown = true),
            settings(requireMouseDown = true),
        )
        assertEquals(1, engine.latched)

        val replacement = engine.analyze(
            world(target(2, z = 5.0), mouseDown = true),
            settings(requireMouseDown = true),
        )
        assertEquals(2, replacement.target?.target?.id)
        assertEquals(2, engine.latched)

        val empty = engine.analyze(world(mouseDown = true), settings(requireMouseDown = true))
        assertEquals(AimStatus.NoTarget, empty.status)
        assertNull(engine.latched)
    }

    @Test
    fun blockBreakPausesUnlessTheBlockIsWhitelisted() {
        val block = AimBlock(registryId = "minecraft:stone")
        val base = world(target(1, z = 3.0), breaking = block)

        assertEquals(
            AimStatus.PausedBlockBreak,
            engine.analyze(base, settings(checkBlockBreak = true)).status,
        )
        assertEquals(
            AimStatus.Tracking,
            engine.analyze(base, settings(checkBlockBreak = true, breakBlocksWhitelist = "stone")).status,
        )
        assertEquals(
            AimStatus.Tracking,
            engine.analyze(base, settings(checkBlockBreak = false)).status,
        )
        assertEquals(
            AimStatus.PausedBlockBreak,
            engine.analyze(
                world(target(1, z = 3.0), breaking = AimBlock(registryId = "minecraft:dirt")),
                settings(checkBlockBreak = true, breakBlocksWhitelist = "stone"),
            ).status,
        )
    }

    @Test
    fun limitToItemsPausesOutsideTheFilter() {
        val sword = AimItem(registryId = "minecraft:diamond_sword", displayName = "Diamond Sword", slot = 1)

        assertEquals(
            AimStatus.Tracking,
            engine.analyze(world(target(1, z = 3.0), held = sword), settings(limitToItems = "swords")).status,
        )
        assertEquals(
            AimStatus.PausedItemFilter,
            engine.analyze(
                world(target(1, z = 3.0), held = AimItem(registryId = "minecraft:apple")),
                settings(limitToItems = "swords"),
            ).status,
        )
        assertEquals(
            AimStatus.Tracking,
            engine.analyze(
                world(target(1, z = 3.0), held = AimItem.HAND),
                settings(limitToItems = "hand"),
            ).status,
        )
    }

    @Test
    fun disablingClearsTheAnalysisAndTheLatch() {
        engine.analyze(world(target(1, z = 3.0), mouseDown = true), settings(requireMouseDown = true))
        assertEquals(1, engine.latched)

        val disabled = engine.analyze(world(target(1, z = 3.0)), settings(), enabled = false)
        assertEquals(AimStatus.Disabled, disabled.status)
        assertNull(disabled.target)
        assertNull(engine.latched)
    }

    @Test
    fun strafeMultiplierUsesPlayerOrTargetMovement() {
        val still = settings(horizontalSpeed = 40f, strafeIncrease = 50f)
        val stationary = engine.analyze(world(target(1, z = 3.0)), still)
        assertEquals(1f, stationary.strafeMultiplier, 1e-4f)
        assertEquals(40f, stationary.horizontalSpeed, 1e-4f)

        val playerStrafing = engine.analyze(
            world(target(1, z = 3.0), velocity = AimVec3(0.3, 0.0, 0.0)),
            still,
        )
        assertEquals(1.5f, playerStrafing.strafeMultiplier, 1e-4f)
        assertEquals(60f, playerStrafing.horizontalSpeed, 1e-4f)

        val targetStrafing = engine.analyze(
            world(target(1, z = 3.0, vx = 0.3)),
            still,
        )
        assertEquals(1.5f, targetStrafing.strafeMultiplier, 1e-4f)
    }

    @Test
    fun verticalAnalysisOnlyReportsSpeedWhenEnabled() {
        val without = engine.analyze(world(target(1, z = 3.0)), settings(aimVertically = false, verticalSpeed = 40f))
        assertEquals(0f, without.verticalSpeed, 1e-4f)

        val with = engine.analyze(world(target(1, z = 3.0)), settings(aimVertically = true, verticalSpeed = 40f))
        assertEquals(40f, with.verticalSpeed, 1e-4f)
    }

    @Test
    fun targetAreaChoosesCenterOrClosestPoint() {
        val centered = engine.analyze(
            world(target(1, z = 4.0)),
            settings(targetArea = AimAssistTargetArea.Center),
        ).target!!
        val closest = engine.analyze(
            world(target(1, z = 4.0)),
            settings(targetArea = AimAssistTargetArea.Closest),
        ).target!!

        assertEquals(4.0, centered.aimPoint.z, 1e-4)
        assertEquals(3.7, closest.aimPoint.z, 1e-4)
        assertTrue(closest.distance < centered.distance)
    }

    @Test
    fun itemFilterParsesCategoriesSlotsNamesAndLegacyIds() {
        val sword = AimItem(registryId = "minecraft:diamond_sword", displayName = "Diamond Sword", slot = 4)

        assertTrue(AimItemFilter.parse("swords").matches(sword))
        assertTrue(AimItemFilter.parse("slot 4").matches(sword))
        assertTrue(AimItemFilter.parse("Diamond Sword").matches(sword))
        assertTrue(AimItemFilter.parse("minecraft:diamond_sword").matches(sword))
        assertFalse(AimItemFilter.parse("axes").matches(sword))
        assertFalse(AimItemFilter.parse("slot 2").matches(sword))

        val legacy = AimItem(legacyId = 35, metadata = 14)
        assertTrue(AimItemFilter.parse("35:14").matches(legacy))
        assertFalse(AimItemFilter.parse("35:15").matches(legacy))
        assertTrue(AimItemFilter.parse("").matches(AimItem(registryId = "minecraft:apple")))
    }

    @Test
    fun blockFilterIsAnEmptyByDefaultWhitelist() {
        assertFalse(AimBlockFilter.parse("").matches(AimBlock(registryId = "minecraft:stone")))
        assertTrue(AimBlockFilter.parse("stone").matches(AimBlock(registryId = "minecraft:stone")))
        assertTrue(AimBlockFilter.parse("1:0").matches(AimBlock(legacyId = 1, metadata = 0)))
        assertFalse(AimBlockFilter.parse("dirt").matches(AimBlock(registryId = "minecraft:stone")))
    }

    private fun choose(
        targetMode: AimAssistTargetMode,
        vararg targets: AimTarget,
        mode: AimAssistMode = AimAssistMode.Simple,
    ): Int? = engine.analyze(
        world(*targets),
        settings(mode = mode, targetMode = targetMode, maxAngle = 180f),
    ).target?.target?.id

    private fun world(
        vararg targets: AimTarget,
        velocity: AimVec3 = AimVec3.ZERO,
        mouseDown: Boolean = true,
        held: AimItem = AimItem.HAND,
        breaking: AimBlock? = null,
    ) = AimWorld(
        eye = AimVec3(0.0, 1.62, 0.0),
        yaw = 0f,
        pitch = 0f,
        targets = targets.toList(),
        playerVelocity = velocity,
        heldItem = held,
        breakingBlock = breaking,
        mouseDown = mouseDown,
    )

    private fun target(
        id: Int,
        x: Double = 0.0,
        z: Double,
        kind: AimEntityKind = AimEntityKind.Player,
        armor: Int = 0,
        health: Float = 20f,
        threat: Float = 0f,
        invisible: Boolean = false,
        lineOfSight: Boolean = true,
        vx: Double = 0.0,
    ) = AimTarget(
        id = id,
        name = "Target $id",
        kind = kind,
        box = AimBox.around(AimVec3(x, 1.0, z), 0.6, 1.8),
        velocity = AimVec3(vx, 0.0, 0.0),
        invisible = invisible,
        armor = armor,
        health = health,
        maxHealth = 20f,
        threat = threat,
        lineOfSight = lineOfSight,
    )

    private fun settings(
        mode: AimAssistMode = AimAssistMode.Simple,
        targetMode: AimAssistTargetMode = AimAssistTargetMode.Distance,
        targetPlayers: Boolean = true,
        targetMobs: Boolean = true,
        targetAnimals: Boolean = false,
        ignoreInvisible: Boolean = false,
        ignoreNaked: Boolean = false,
        ignoreBehindWalls: Boolean = false,
        requireMouseDown: Boolean = false,
        checkBlockBreak: Boolean = false,
        breakBlocksWhitelist: String = "",
        aimVertically: Boolean = false,
        verticalSpeed: Float = 35f,
        horizontalSpeed: Float = 55f,
        strafeIncrease: Float = 25f,
        maxAngle: Float = 180f,
        distance: Float = 8f,
        limitToItems: String = "",
        targetArea: AimAssistTargetArea = AimAssistTargetArea.Center,
    ) = AimAssistSettings(
        mode = mode,
        targetPlayers = targetPlayers,
        targetMobs = targetMobs,
        targetAnimals = targetAnimals,
        ignoreInvisible = ignoreInvisible,
        ignoreNaked = ignoreNaked,
        ignoreBehindWalls = ignoreBehindWalls,
        requireMouseDown = requireMouseDown,
        strafeIncrease = strafeIncrease,
        checkBlockBreak = checkBlockBreak,
        breakBlocksWhitelist = breakBlocksWhitelist,
        aimVertically = aimVertically,
        verticalSpeed = verticalSpeed,
        horizontalSpeed = horizontalSpeed,
        maxAngle = maxAngle,
        distance = distance,
        limitToItems = limitToItems,
        targetArea = targetArea,
        targetMode = targetMode,
    ).normalized()
}
