package org.polyfrost.oneconfig.internal.ui.zordyx

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.polyfrost.oneconfig.internal.ui.components.Chip
import org.polyfrost.oneconfig.internal.ui.components.IconButton
import org.polyfrost.oneconfig.internal.ui.components.Text
import org.polyfrost.oneconfig.internal.ui.components.settings.SwitchControl
import org.polyfrost.oneconfig.internal.ui.shell.PageGutter
import org.polyfrost.oneconfig.internal.ui.shell.pageSearchQuery
import org.polyfrost.oneconfig.internal.ui.themes.LocalTheme

/**
 * The legit mods view: allowed, non-cheating conveniences split by HUD and game filters.
 *
 * Favourites are local visual state. Nothing here hooks into Minecraft.
 */
@Composable
fun ZordyxLegitModsScreen() {
    val theme = LocalTheme.current
    val headerQuery = pageSearchQuery()
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(LegitFilter.All) }
    val search = query.ifBlank { headerQuery }

    val visible = ZordyxCatalog.legitMods.filter { entry ->
        val matchesFilter = when (filter) {
            LegitFilter.All -> true
            LegitFilter.Hud -> LegitTag.Hud in entry.tags
            LegitFilter.Game -> LegitTag.Game in entry.tags
            LegitFilter.Favorites -> ZordyxState.isFavorite(entry.id)
        }
        matchesFilter && ZordyxCatalog.matches(entry, search)
    }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = PageGutter),
        ) {
            items(LegitFilter.entries) { entry ->
                Chip(label = entry.title, selected = filter == entry) { filter = entry }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = PageGutter),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ZordyxSearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = "Search legit mods...",
                modifier = Modifier.weight(1f),
            )
            Text(
                "${visible.size} " + if (visible.size == 1) "mod" else "mods",
                color = theme.textColorSecondary,
                fontSize = 12.sp,
            )
        }

        ZordyxPreviewNote(
            "These are display entries in this build. Toggles do not change how the game behaves.",
            Modifier.padding(horizontal = PageGutter),
        )

        if (visible.isEmpty()) {
            ZordyxEmptyState(
                icon = if (filter == LegitFilter.Favorites) "star" else "search",
                title = if (filter == LegitFilter.Favorites) "No favourites yet" else "Nothing matches",
                body = if (filter == LegitFilter.Favorites) {
                    "Star a legit mod and it will show up here."
                } else {
                    "Try another filter, or clear the search."
                },
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(horizontal = PageGutter, vertical = 2.dp),
            ) {
                items(visible, key = { it.id }) { entry ->
                    val enabled = ZordyxState.isEnabled(entry.id)
                    ZordyxToggleRow(
                        name = entry.name,
                        description = entry.description,
                        enabled = enabled,
                        onToggle = { ZordyxState.setEnabled(entry.id, !enabled) },
                        icon = entry.icon ?: if (LegitTag.Hud in entry.tags) "hud" else "settings",
                        tags = entry.tags.map { it.label },
                        trailing = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                val favorite = ZordyxState.isFavorite(entry.id)
                                IconButton(
                                    iconName = if (favorite) "star-filled" else "star",
                                    modifier = Modifier.size(16.dp),
                                    foreground = if (favorite) theme.favoriteColor else theme.textColorSecondary,
                                    hoveredForeground = theme.favoriteColor,
                                ) { ZordyxState.toggleFavorite(entry.id) }
                                SwitchControl(enabled) { ZordyxState.setEnabled(entry.id, !enabled) }
                            }
                        },
                    )
                }
            }
        }
    }
}
