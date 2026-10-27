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

package org.polyfrost.oneconfig.internal.ui.themes

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.polyfrost.oneconfig.internal.ThemeConfig

/**
 * Covers the theme files the interface reads from its own theme directory, and the fallback the registry takes
 * when the configuration names a theme that is no longer installed.
 */
class CustomThemesTest {
    private val written = mutableListOf<Path>()
    private lateinit var directory: Path

    @BeforeEach
    fun createDirectory() {
        directory = CustomThemes.directory()
        Files.createDirectories(directory)
    }

    @AfterEach
    fun removeFixtures() {
        written.forEach { Files.deleteIfExists(it) }
        written.clear()
        ThemeConfig.activeTheme = DEFAULT_THEME_ID
    }

    @Test
    fun loadsAValidThemeAndReportsItAsCustom() {
        write("midnight.json", theme("midnight", "Midnight", "\"description\": \"A theme written by hand\""))

        val theme = loadedThemes().firstOrNull { it.id == "midnight" }
            ?: fail("a well formed theme file should load")

        assertEquals("Midnight", theme.name)
        assertEquals("A theme written by hand", theme.description.orEmpty())
        // a theme that names no preview uses the one named after it, so a custom theme needs no extra asset
        assertEquals("assets/oneconfig/images/midnight.svg", theme.previewImage)
        assertTrue(UIThemeExtras.isCustom("midnight"), "a loaded theme file is reported as custom")
    }

    @Test
    fun keepsWellFormedThemesAndDiscardsEveryMalformedOne() {
        write("good.json", theme("good", "Good", null))
        // a colour that is not a hex string
        write("bad-color.json", theme("bad-color", "Bad Color", "\"accent\": \"rebeccapurple\""))
        // a hex string of the wrong length
        write("short-hex.json", theme("short-hex", "Short Hex", "\"accent\": \"#12345\""))
        // no name at all
        write("nameless.json", fields("nameless", null))
        // an id that is not a valid theme id
        write("bad-id.json", theme("Bad Id", "Bad Id", null))
        // a required colour is missing
        write("incomplete.json", "{\"id\": \"incomplete\", \"name\": \"Incomplete\", \"pageBackground\": \"#000000\"}")
        // not JSON, and JSON that is not an object
        write("garbage.json", "this is not a theme")
        write("array.json", "[1, 2, 3]")
        // a preview outside this mod's own asset namespace
        write("escaping.json", theme("escaping", "Escaping", "\"preview\": \"/etc/passwd\""))

        assertEquals(setOf("good"), loadedThemes().map { it.id }.toSet())
    }

    @Test
    fun keepsTheFirstThemeOfADuplicateId() {
        write("a-first.json", theme("duplicate", "First", null))
        write("b-second.json", theme("duplicate", "Second", null))

        val duplicates = loadedThemes().filter { it.id == "duplicate" }

        assertEquals(1, duplicates.size, "an id that is already taken must not be registered twice")
        assertEquals("First", duplicates.single().name)
    }

    @Test
    fun defaultsToTheZordyxDarkTheme() {
        assertEquals(DEFAULT_THEME_ID, ThemeConfig.activeTheme, "Zordyx Dark is the theme a fresh installation starts on")

        ThemeRegistry.loadFromConfig()

        assertEquals(DEFAULT_THEME_ID, ThemeConfig.activeTheme)
    }

    @Test
    fun fallsBackToZordyxDarkWhenTheConfiguredThemeIsGone() {
        // what a configuration written before the Polyfrost themes were removed still points at, by id and by the
        // display name the configurations written before themes had ids stored
        for (removed in listOf("polyglass-dark", "polyglass-light", "PolyGlass Dark", "not-a-theme")) {
            ThemeConfig.activeTheme = removed

            ThemeRegistry.loadFromConfig()

            assertEquals(
                DEFAULT_THEME_ID, ThemeConfig.activeTheme,
                "the removed theme '$removed' must fall back to Zordyx Dark",
            )
        }
    }

    private fun loadedThemes(): List<UITheme> = CustomThemes.readThemes()

    private fun write(fileName: String, contents: String) {
        val file = directory.resolve(fileName)
        Files.writeString(file, contents, StandardCharsets.UTF_8)
        written.add(file)
    }

    /** A theme with every required field, plus [extra] merged into it. */
    private fun theme(id: String, name: String, extra: String?): String {
        val base = fields(id, name)
        if (extra == null) return base
        return base.removeSuffix("}") + ", $extra}"
    }

    private fun fields(id: String, name: String?): String {
        val idField = "\"id\": \"$id\""
        val nameField = if (name == null) "" else ", \"name\": \"$name\""
        return "{$idField$nameField, \"pageBackground\": \"#0A0E11\", \"sidebarBackground\": \"#070A0C\","
            .plus(" \"panelBackground\": \"#121A1E\", \"componentBackground\": \"#0F1519\",")
            .plus(" \"popupBackground\": \"#101619\", \"border\": \"#1FFFFFFF\",")
            .plus(" \"primaryText\": \"#E9F1F2\", \"secondaryText\": \"#828D93\"}")
    }

    private companion object {
        const val DEFAULT_THEME_ID = "zordyx-dark"
    }
}
