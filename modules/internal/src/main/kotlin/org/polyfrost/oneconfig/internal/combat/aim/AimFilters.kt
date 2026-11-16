package org.polyfrost.oneconfig.internal.combat.aim

import java.util.Locale
import org.polyfrost.oneconfig.internal.ui.zordyx.splitAimAssistEntries

private val CATEGORY_KEYWORDS: Map<String, AimItemCategory> =
    AimItemCategory.entries.associateBy { it.keyword }

private val LEGACY_ID = Regex("^(\\d+)(?::(\\d+))?$")

/**
 * The Limit to Items setting, parsed once into its entries.
 *
 * Entries are matched case-insensitively against an item's categories, its registry id with or without
 * the namespace, its display name, its hotbar slot written `slot 1` to `slot 9`, and a legacy numeric id
 * with optional metadata such as `35` or `35:14`. An empty filter allows every item.
 */
class AimItemFilter private constructor(private val entries: List<String>) {
    val isEmpty: Boolean get() = entries.isEmpty()

    fun matches(item: AimItem): Boolean {
        if (entries.isEmpty()) return true
        val categories = item.effectiveCategories
        val registry = item.registryId.lowercase(Locale.ROOT)
        val simpleId = registry.substringAfter(':')
        val display = item.displayName.lowercase(Locale.ROOT)

        return entries.any { raw ->
            val entry = raw.lowercase(Locale.ROOT)
            when {
                CATEGORY_KEYWORDS[entry]?.let { it in categories } == true -> true
                entry.startsWith("slot ") -> {
                    val slot = entry.removePrefix("slot ").trim().toIntOrNull()
                    item.slot > 0 && slot == item.slot
                }
                LEGACY_ID.matches(entry) -> matchesLegacy(entry, item.legacyId, item.metadata)
                entry == registry || (simpleId.isNotEmpty() && entry == simpleId) -> true
                display.isNotEmpty() && entry == display -> true
                else -> false
            }
        }
    }

    companion object {
        fun parse(raw: String): AimItemFilter = AimItemFilter(splitAimAssistEntries(raw))
    }
}

/**
 * The Break Blocks Whitelist setting.
 *
 * This is a whitelist: an empty list allows no block, so any block break pauses analysis. Listing a
 * block by registry id, display name or legacy numeric id lets the player keep breaking that block
 * while analysis stays active.
 */
class AimBlockFilter private constructor(private val entries: List<String>) {
    val isEmpty: Boolean get() = entries.isEmpty()

    fun matches(block: AimBlock): Boolean {
        if (entries.isEmpty()) return false
        val registry = block.registryId.lowercase(Locale.ROOT)
        val simpleId = registry.substringAfter(':')
        val display = block.displayName.lowercase(Locale.ROOT)

        return entries.any { raw ->
            val entry = raw.lowercase(Locale.ROOT)
            when {
                LEGACY_ID.matches(entry) -> matchesLegacy(entry, block.legacyId, block.metadata)
                entry == registry || (simpleId.isNotEmpty() && entry == simpleId) -> true
                display.isNotEmpty() && entry == display -> true
                else -> false
            }
        }
    }

    companion object {
        fun parse(raw: String): AimBlockFilter = AimBlockFilter(splitAimAssistEntries(raw))
    }
}

private fun matchesLegacy(entry: String, legacyId: Int?, metadata: Int?): Boolean {
    if (legacyId == null) return false
    val id = entry.substringBefore(':').toIntOrNull() ?: return false
    if (id != legacyId) return false
    if (!entry.contains(':')) return true
    val entryMetadata = entry.substringAfter(':').toIntOrNull() ?: return false
    return entryMetadata == metadata
}
