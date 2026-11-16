package org.polyfrost.oneconfig.internal.combat

//? if = 1.8.9 {
/*import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.entity.Entity
import net.minecraft.entity.living.LivingEntity
import net.minecraft.entity.living.mob.monster.Monster
import net.minecraft.entity.living.mob.passive.animal.AnimalEntity
import net.minecraft.entity.living.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.FoodItem
import net.minecraft.item.Item
import org.polyfrost.oneconfig.internal.combat.aim.AimAssistFeed
import org.polyfrost.oneconfig.internal.combat.aim.AimBlock
import org.polyfrost.oneconfig.internal.combat.aim.AimBox
import org.polyfrost.oneconfig.internal.combat.aim.AimEntityKind
import org.polyfrost.oneconfig.internal.combat.aim.AimItem
import org.polyfrost.oneconfig.internal.combat.aim.AimItemCategory
import org.polyfrost.oneconfig.internal.combat.aim.AimTarget
import org.polyfrost.oneconfig.internal.combat.aim.AimVec3
import org.polyfrost.oneconfig.internal.combat.aim.AimWorld
import org.polyfrost.oneconfig.internal.ui.zordyx.ZordyxAimAssist

/**
 * Supplies AimAssist with the client world once per tick on 1.8.9.
 *
 * The legacy client exposes entities as plain values rather than the modern rendering entity list, so
 * this branch copies the same immutable snapshot the modern branch does. No entity is retained.
 */
object MinecraftAimAssistFeed : AimAssistFeed {
    fun install() {
        ZordyxAimAssist.feed = this
        ZordyxAimAssist.ensureTickListener()
    }

    override fun snapshot(): AimWorld? {
        val client = Minecraft.getInstance()
        val player = client.player ?: return null
        val level = client.level ?: return null
        val eyePos = player.getEyePosition(1f)
        val eye = AimVec3(eyePos.x, eyePos.y, eyePos.z)
        val radius = ZordyxAimAssist.settings.distance + 2.0
        val targets = ArrayList<AimTarget>()

        for (entity in level.entities) {
            if (entity === player || !entity.isAlive || entity !is LivingEntity) continue
            val box = entity.getShape()
            val center = AimVec3(
                (box.minX + box.maxX) * 0.5,
                (box.minY + box.maxY) * 0.5,
                (box.minZ + box.maxZ) * 0.5,
            )
            if (eye.distanceTo(center) > radius) continue

            targets += AimTarget(
                id = entity.getNetworkId(),
                name = entity.getName(),
                kind = kindOf(entity),
                box = AimBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ),
                velocity = AimVec3(entity.velocityX, entity.velocityY, entity.velocityZ),
                invisible = entity.isInvisible,
                armor = entity.getArmorProtection(),
                health = entity.getHealth(),
                maxHealth = entity.getMaxHealth(),
                threat = threatOf(entity),
                lineOfSight = player.canSee(entity),
            )
        }

        val held = player.getItemInHand()
        val item = held?.getItem()
        val categories = buildSet {
            if (item is BlockItem) add(AimItemCategory.Blocks)
            if (item is FoodItem) add(AimItemCategory.Food)
        }

        return AimWorld(
            eye = eye,
            yaw = player.yaw,
            pitch = player.pitch,
            targets = targets,
            playerVelocity = AimVec3(player.velocityX, player.velocityY, player.velocityZ),
            heldItem = AimItem(
                registryId = if (item == null) "" else Item.REGISTRY.getKey(item).toString(),
                displayName = held?.getDisplayName()?.getString() ?: "",
                slot = player.inventory.selectedSlot + 1,
                categories = categories,
            ),
            breakingBlock = breakingBlock(client, level),
            mouseDown = client.options.attackKey.isDown(),
        )
    }

    private fun kindOf(entity: LivingEntity): AimEntityKind = when (entity) {
        is PlayerEntity -> AimEntityKind.Player
        is Monster -> AimEntityKind.HostileMob
        is AnimalEntity -> AimEntityKind.Animal
        else -> AimEntityKind.Other
    }

    private fun threatOf(entity: LivingEntity): Float {
        val base = when (entity) {
            is PlayerEntity -> 0.9f
            is Monster -> 0.7f
            is AnimalEntity -> 0.25f
            else -> 0.1f
        }
        val healthFraction = if (entity.getMaxHealth() <= 0f) 1f
        else (entity.getHealth() / entity.getMaxHealth()).coerceIn(0f, 1f)
        return (base * (0.75f + 0.25f * healthFraction)).coerceIn(0f, 1f)
    }

    private fun breakingBlock(client: Minecraft, level: ClientLevel): AimBlock? {
        if (client.interactionManager?.isMiningBlock != true) return null
        val pos = client.hitResult?.getPos() ?: return AimBlock()
        val block = level.getBlockState(pos).getBlock()
        return AimBlock(
            registryId = Block.REGISTRY.getKey(block).toString(),
            displayName = block.getName(),
        )
    }
}
*///?}

//? if > 1.8.9 {
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BlockItem
import net.minecraft.world.phys.BlockHitResult
import org.polyfrost.oneconfig.internal.combat.aim.AimAssistFeed
import org.polyfrost.oneconfig.internal.combat.aim.AimBlock
import org.polyfrost.oneconfig.internal.combat.aim.AimBox
import org.polyfrost.oneconfig.internal.combat.aim.AimEntityKind
import org.polyfrost.oneconfig.internal.combat.aim.AimItem
import org.polyfrost.oneconfig.internal.combat.aim.AimItemCategory
import org.polyfrost.oneconfig.internal.combat.aim.AimTarget
import org.polyfrost.oneconfig.internal.combat.aim.AimVec3
import org.polyfrost.oneconfig.internal.combat.aim.AimWorld
import org.polyfrost.oneconfig.internal.ui.zordyx.ZordyxAimAssist

/**
 * Supplies AimAssist with the client world once per tick.
 *
 * The feed reads only what the analysis needs and copies it into value snapshots, so the engine never
 * holds an entity and an entity that unloads between ticks simply disappears.
 */
object MinecraftAimAssistFeed : AimAssistFeed {
    fun install() {
        ZordyxAimAssist.feed = this
        ZordyxAimAssist.ensureTickListener()
    }

    override fun snapshot(): AimWorld? {
        val client = Minecraft.getInstance()
        val player = client.player ?: return null
        val level = client.level ?: return null
        val eyePos = player.eyePosition
        val eye = AimVec3(eyePos.x, eyePos.y, eyePos.z)
        // The settings cap candidate distance at 8 blocks; a little slack keeps line-of-sight checks
        // limited to entities the engine could actually consider.
        val radius = ZordyxAimAssist.settings.distance + 2.0
        val targets = ArrayList<AimTarget>()

        for (entity in level.entitiesForRendering()) {
            if (entity === player || !entity.isAlive || entity !is LivingEntity) continue
            val box = entity.boundingBox
            val center = AimVec3(
                (box.minX + box.maxX) * 0.5,
                (box.minY + box.maxY) * 0.5,
                (box.minZ + box.maxZ) * 0.5,
            )
            if (eye.distanceTo(center) > radius) continue

            val velocity = entity.deltaMovement
            targets += AimTarget(
                id = entity.id,
                name = entity.name.string,
                kind = kindOf(entity),
                box = AimBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ),
                velocity = AimVec3(velocity.x, velocity.y, velocity.z),
                invisible = entity.isInvisible,
                armor = entity.armorValue,
                health = entity.health,
                maxHealth = entity.maxHealth,
                threat = threatOf(entity),
                lineOfSight = player.hasLineOfSight(entity),
            )
        }

        val held = player.mainHandItem
        //? if >= 1.21.5 {
        val slot = player.inventory.selectedSlot + 1
        //?} else {
        /*val slot = player.inventory.selected + 1
        *///?}
        val categories = buildSet {
            if (held.item is BlockItem) add(AimItemCategory.Blocks)
            if (held.has(DataComponents.FOOD)) add(AimItemCategory.Food)
        }

        val movement = player.deltaMovement
        return AimWorld(
            eye = eye,
            yaw = player.yRot,
            pitch = player.xRot,
            targets = targets,
            playerVelocity = AimVec3(movement.x, movement.y, movement.z),
            heldItem = AimItem(
                registryId = BuiltInRegistries.ITEM.getKey(held.item).toString(),
                displayName = held.hoverName.string,
                slot = slot,
                categories = categories,
            ),
            breakingBlock = breakingBlock(client, level),
            mouseDown = client.options.keyAttack.isDown(),
        )
    }

    private fun kindOf(entity: LivingEntity): AimEntityKind = when (entity) {
        is Player -> AimEntityKind.Player
        is Monster -> AimEntityKind.HostileMob
        is Animal -> AimEntityKind.Animal
        else -> AimEntityKind.Other
    }

    /**
     * A rough threat score for the Threat target mode.
     *
     * Vanilla exposes no single danger value, so the entity group supplies the base and a healthy target
     * ranks slightly above a wounded one. Players outrank hostile mobs, which outrank passive animals.
     */
    private fun threatOf(entity: LivingEntity): Float {
        val base = when (entity) {
            is Player -> 0.9f
            is Monster -> 0.7f
            is Animal -> 0.25f
            else -> 0.1f
        }
        val healthFraction = if (entity.maxHealth <= 0f) 1f else (entity.health / entity.maxHealth).coerceIn(0f, 1f)
        return (base * (0.75f + 0.25f * healthFraction)).coerceIn(0f, 1f)
    }

    private fun breakingBlock(client: Minecraft, level: ClientLevel): AimBlock? {
        if (client.gameMode?.isDestroying != true) return null
        val hit = client.hitResult as? BlockHitResult ?: return AimBlock()
        val block = level.getBlockState(hit.blockPos).block
        return AimBlock(
            registryId = BuiltInRegistries.BLOCK.getKey(block).toString(),
            displayName = block.name.string,
        )
    }
}
//?}
