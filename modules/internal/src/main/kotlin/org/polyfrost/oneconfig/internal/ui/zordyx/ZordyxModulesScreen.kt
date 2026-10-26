package org.polyfrost.oneconfig.internal.ui.zordyx

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import org.polyfrost.oneconfig.internal.ui.components.Text
import org.polyfrost.oneconfig.internal.ui.navigation.graph.ZordyxModulesRoute
import org.polyfrost.oneconfig.internal.ui.shell.LocalShell
import org.polyfrost.oneconfig.internal.ui.shell.PageGutter
import org.polyfrost.oneconfig.internal.ui.shell.pageSearchQuery
import org.polyfrost.oneconfig.internal.ui.themes.LocalTheme

/**
 * The Modules section: a category strip, a local filter and the module rows for the selected
 * category.
 *
 * Rows read their enabled state from [ZordyxState], so a toggle only changes this screen. No module is
 * loaded or applied to the game.
 */
@Composable
fun ZordyxModulesScreen(category: ModuleCategory) {
    val theme = LocalTheme.current
    val navigation = LocalShell.current.navigation
    val headerQuery = pageSearchQuery()
    var query by remember { mutableStateOf("") }
    val filter = query.ifBlank { headerQuery }

    val modules = remember(category) { ZordyxCatalog.modulesIn(category) }
    val visible = remember(modules, filter) {
        if (filter.isBlank()) modules else modules.filter { ZordyxCatalog.matches(it, filter) }
    }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = PageGutter),
        ) {
            items(ModuleCategory.entries) { entry ->
                Chip(
                    label = entry.title,
                    selected = entry == category,
                    icon = entry.icon,
                    onClick = {
                        if (entry != category) navigation.navigate(ZordyxModulesRoute(entry.name))
                    },
                )
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
                placeholder = "Search ${category.title} modules...",
                modifier = Modifier.weight(1f),
            )
            Text(
                count(visible.size, "module"),
                color = theme.textColorSecondary,
                fontSize = 12.sp,
            )
        }

        ZordyxPreviewNote(
            "Interface preview. Toggles and filters change this screen only; nothing is applied to the game.",
            Modifier.padding(horizontal = PageGutter),
        )

        if (visible.isEmpty()) {
            ZordyxEmptyState(
                icon = category.icon,
                title = if (modules.isEmpty()) "No modules in ${category.title} yet" else "No modules match your search",
                body = if (modules.isEmpty()) {
                    "This category is ready and waiting for its module list. Once modules are added they appear here, with their own row and toggle."
                } else {
                    "Try a different name, or clear the search to see every ${category.title} module."
                },
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(horizontal = PageGutter, vertical = 2.dp),
            ) {
                items(visible, key = { it.id }) { module ->
                    ZordyxToggleRow(
                        name = module.name,
                        description = module.description,
                        enabled = ZordyxState.isEnabled(module.id, module.enabled),
                        onToggle = {
                            ZordyxState.setEnabled(module.id, !ZordyxState.isEnabled(module.id, module.enabled))
                        },
                        icon = module.icon ?: category.icon,
                        tags = module.tags,
                    )
                }
            }
        }
    }
}

private fun count(size: Int, singular: String): String = "$size " + if (size == 1) singular else singular + "s"
