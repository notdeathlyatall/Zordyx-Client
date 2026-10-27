/*
 * This file is part of Zordyx Client, a fork of OneConfig.
 * OneConfig - Next Generation Config Library for Minecraft: Java Edition
 * Copyright (C) 2021~2024 Polyfrost.
 *   <https://polyfrost.org> <https://github.com/Polyfrost/>
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *   OneConfig is licensed under the terms of version 3 of the GNU Lesser
 * General Public License as published by the Free Software Foundation, AND
 * under the Additional Terms Applicable to OneConfig, as published by Polyfrost,
 * either version 1.0 of the Additional Terms, or (at your option) any later
 * version.
 *
 *   This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.polyfrost.oneconfig.internal.ui.zordyx

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Guards the Modules section's module listings: the module inventory, which subsection each module
 * belongs to, and the session-only toggles the rows read from [ZordyxState].
 */
class ZordyxModuleCatalogTest {
    /** Every module, in listing order, per existing subsection. */
    private val expectedNames: Map<ModuleCategory, List<String>> = mapOf(
        ModuleCategory.Combat to listOf(
            "AimAssist", "AutoClicker", "BlockHit", "HitFlick", "HitSelect", "Reach", "RightClicker",
            "SilentAura", "Sprint", "JumpReset", "WTap", "Triggerbot", "CrystalAura",
        ),
        ModuleCategory.Render to listOf(
            "AntiDebuff", "Arrows", "Chams", "ESP", "Explosions", "Fullbright", "Health", "Indicators",
            "ItemESP", "NameTags", "Projectiles", "Search", "SpawnerFinder", "StorageESP", "Tracers",
            "Trajectories",
        ),
        ModuleCategory.Utility to listOf(
            "AntiFireball", "AutoAnchor", "AutoMace", "AutoPearl", "Clutch", "HitSwap", "Panic",
            "PearlCatch", "Scaffold", "ShieldBreaker", "TargetFilter", "Velocity", "WindCharge",
        ),
        ModuleCategory.World to listOf(
            "Anti-AFK", "AutoFish", "AutoTool", "Block In", "ChestSteal", "FastPlace", "Freecam",
            "MLG", "Parkour", "SafeWalk", "XRay",
        ),
        ModuleCategory.Inventory to listOf(
            "ArmorSwitch", "AutoArmor", "AutoHotbar", "AutoTotem", "InvCleaner", "InventoryManager",
            "InventoryFill", "Refill", "ThrowDebuff", "ThrowPot",
        ),
        ModuleCategory.Network to listOf("BackTrack", "Blink", "FakeLag", "KnockbackDelay"),
        ModuleCategory.Minigames to listOf("BedBreaker", "BedPlates", "MurdererFinder", "PropHunt"),
    )

    @Test
    fun everyModuleIsInItsOwnSubsection() {
        for ((category, names) in expectedNames) {
            val actual = ZordyxCatalog.modulesIn(category).map { it.name }
            assertEquals(names, actual, "${category.title} should list exactly these modules")
        }
    }

    @Test
    fun theCatalogHoldsAllSeventyOneModulesOnce() {
        assertEquals(71, ZordyxCatalog.modules.size)
        assertEquals(71, expectedNames.values.sumOf { it.size })

        val ids = ZordyxCatalog.modules.map { it.id }
        assertEquals(ids.size, ids.toSet().size, "module ids must be unique so toggles never share a state")
        assertEquals(ids.size, ZordyxCatalog.modules.map { it.name }.toSet().size)

        assertTrue(ZordyxCatalog.modules.all { it.name.isNotBlank() && it.description.isNotBlank() })
        assertTrue(ZordyxCatalog.modules.all { expectedNames.getValue(it.category).contains(it.name) })
    }

    @Test
    fun togglesAreSessionStateThatSurvivesSwitchingSubsections() {
        val combat = ZordyxCatalog.modulesIn(ModuleCategory.Combat).first { it.name == "AimAssist" }
        assertFalse(ZordyxState.isEnabled(combat.id, combat.enabled), "modules start off")

        ZordyxState.setEnabled(combat.id, true)

        // switching subsection re-reads the same state, so the toggle is still where the user left it
        assertTrue(ZordyxState.isEnabled(combat.id, combat.enabled))
        assertTrue(ZordyxState.isEnabled("aim-assist", false), "state is keyed by id, not by subsection")

        // only that module changed
        val untouched = ZordyxCatalog.modulesIn(ModuleCategory.Combat).first { it.name == "Reach" }
        assertFalse(ZordyxState.isEnabled(untouched.id, untouched.enabled))

        ZordyxState.setEnabled(combat.id, false)
        assertFalse(ZordyxState.isEnabled(combat.id, combat.enabled))
    }

    @Test
    fun searchFindsModulesByTheirOwnName() {
        val scaffold = ZordyxCatalog.modulesIn(ModuleCategory.Utility).first { it.name == "Scaffold" }

        assertTrue(ZordyxCatalog.matches(scaffold, "scaffold"))
        assertTrue(ZordyxCatalog.matches(scaffold, "places blocks"))
        assertFalse(ZordyxCatalog.matches(scaffold, "tracers"))
    }
}
