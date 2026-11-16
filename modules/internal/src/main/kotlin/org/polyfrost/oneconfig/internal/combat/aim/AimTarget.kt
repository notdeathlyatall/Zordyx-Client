package org.polyfrost.oneconfig.internal.combat.aim

import java.util.Locale

/** The generic item groups the Limit to Items setting understands. */
enum class AimItemCategory(val keyword: String) {
    Swords("swords"),
    Shovels("shovels"),
    Axes("axes"),
    Pickaxes("pickaxes"),
    Blocks("blocks"),
    Hand("hand"),
    Food("food"),
}

/** The entity groups the Players, Mobs and Animals filters understand. */
enum class AimEntityKind(val label: String) {
    Player("Player"),
    HostileMob("Hostile mob"),
    Animal("Animal"),
    Other("Other"),
}

/** The item held when a frame was captured. */
data class AimItem(
    val registryId: String = "",
    val displayName: String = "",
    val legacyId: Int? = null,
    val metadata: Int? = null,
    /** The selected hotbar slot, 1 to 9, or 0 when the source does not report one. */
    val slot: Int = 0,
    val categories: Set<AimItemCategory> = emptySet(),
) {
    val isEmpty: Boolean
        get() = registryId.isBlank() || registryId.equals("minecraft:air", ignoreCase = true)

    /**
     * The categories this item belongs to.
     *
     * A source can classify an item exactly, for example knowing that a particular modded block is a
     * block or that a golden apple is food. When it does not, the registry id and display name still
     * identify the common tool groups, so the item filter works with plain vanilla names too.
     */
    val effectiveCategories: Set<AimItemCategory>
        get() {
            if (isEmpty) return setOf(AimItemCategory.Hand)
            val result = LinkedHashSet(categories)
            val text = "$registryId $displayName".lowercase(Locale.ROOT)
            if (text.contains("sword")) result += AimItemCategory.Swords
            if (text.contains("shovel")) result += AimItemCategory.Shovels
            if (text.contains("pickaxe")) {
                result += AimItemCategory.Pickaxes
            } else if (text.contains("axe")) {
                result += AimItemCategory.Axes
            }
            return result
        }

    companion object {
        val HAND = AimItem(categories = setOf(AimItemCategory.Hand))
    }
}

/** The block being broken when a frame was captured. */
data class AimBlock(
    val registryId: String = "",
    val legacyId: Int? = null,
    val metadata: Int? = null,
    val displayName: String = "",
)

/**
 * One eligible-looking entity at the moment a frame was captured.
 *
 * Everything is a value: the analysis never keeps an entity object, so an entity that unloads, changes
 * dimension or is replaced simply stops appearing in the next frame.
 */
data class AimTarget(
    val id: Int,
    val name: String,
    val kind: AimEntityKind,
    val box: AimBox,
    val velocity: AimVec3 = AimVec3.ZERO,
    val invisible: Boolean = false,
    val armor: Int = 0,
    val health: Float = 20f,
    val maxHealth: Float = 20f,
    /** A source-supplied danger score in the range 0 to 1. */
    val threat: Float = 0f,
    val lineOfSight: Boolean = true,
    val alive: Boolean = true,
)

/** Everything the engine needs for one analysis pass. */
data class AimWorld(
    val eye: AimVec3,
    val yaw: Float,
    val pitch: Float,
    val targets: List<AimTarget> = emptyList(),
    val playerVelocity: AimVec3 = AimVec3.ZERO,
    val heldItem: AimItem = AimItem.HAND,
    val breakingBlock: AimBlock? = null,
    val mouseDown: Boolean = false,
)

/** A source that can supply world snapshots, such as a platform tick adapter. */
fun interface AimAssistFeed {
    fun snapshot(): AimWorld?
}

enum class AimStatus(val label: String) {
    Disabled("Disabled"),
    Tracking("Tracking"),
    NoTarget("No target"),
    PausedBlockBreak("Paused while breaking a block"),
    PausedMouseReleased("Paused until the attack button is held"),
    PausedItemFilter("Paused by the item filter"),
}

/** One target that passed every filter, with the geometry and score used to rank it. */
data class AimCandidate(
    val target: AimTarget,
    val aimPoint: AimVec3,
    val distance: Float,
    val angle: Float,
    val yawDelta: Float,
    val pitchDelta: Float,
    val targetHorizontalSpeed: Float,
    val score: Float,
)

/** The outcome of one analysis pass. */
data class AimAnalysis(
    val status: AimStatus,
    val target: AimCandidate? = null,
    val considered: Int = 0,
    val eligible: Int = 0,
    val horizontalSpeed: Float = 0f,
    val verticalSpeed: Float = 0f,
    val strafeMultiplier: Float = 1f,
)
