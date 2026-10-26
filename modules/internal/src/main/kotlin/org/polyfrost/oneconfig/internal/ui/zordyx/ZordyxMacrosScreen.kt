package org.polyfrost.oneconfig.internal.ui.zordyx

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.polyfrost.oneconfig.internal.ui.components.Chip
import org.polyfrost.oneconfig.internal.ui.components.Icon
import org.polyfrost.oneconfig.internal.ui.components.Text
import org.polyfrost.oneconfig.internal.ui.components.onClick
import org.polyfrost.oneconfig.internal.ui.components.rememberInteractionSource
import org.polyfrost.oneconfig.internal.ui.shell.PageGutter
import org.polyfrost.oneconfig.internal.ui.themes.LocalTheme

/**
 * Macros: a local list, a create dialog and the bind shown for each entry.
 *
 * Nothing is executed and no keybind is registered. New macros last for the session only.
 */
@Composable
fun ZordyxMacrosScreen() {
    val theme = LocalTheme.current
    var creating by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = PageGutter),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                ZordyxTitle("Macros", "Run a command or press keys from one bind.")
                Chip(label = "New macro", selected = false, icon = "plus") { creating = true }
            }

            ZordyxPreviewNote(
                "Preview only. Macros are not bound or executed, and nothing is saved between sessions.",
                Modifier.padding(horizontal = PageGutter),
            )

            if (ZordyxMacros.entries.isEmpty()) {
                ZordyxEmptyState(
                    icon = "keyboard",
                    title = "No macros yet",
                    body = "Create one to see how the macro list and its keybind will look.",
                    modifier = Modifier.weight(1f),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = PageGutter, vertical = 2.dp),
                ) {
                    items(ZordyxMacros.entries, key = { it.id }) { macro ->
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 54.dp)
                                .background(theme.modCardBackground, theme.sideBarNavigationEntryShape)
                                .border(1.dp, theme.borderColor, theme.sideBarNavigationEntryShape)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                Modifier.size(30.dp).background(theme.componentBackground, theme.sideBarNavigationEntryShape),
                                contentAlignment = Alignment.Center,
                            ) { Icon("console", color = theme.textColorSecondary, modifier = Modifier.size(16.dp)) }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(macro.name, color = theme.textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    macro.command,
                                    color = theme.textColorSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Box(
                                Modifier.background(theme.chipBackground, theme.sideBarNavigationEntryShape)
                                    .border(1.dp, theme.borderColor, theme.sideBarNavigationEntryShape)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(macro.keybind, color = theme.textColorSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        if (creating) {
            MacroDialog(onDismiss = { creating = false })
        }
    }
}

@Composable
private fun MacroDialog(onDismiss: () -> Unit) {
    val theme = LocalTheme.current
    var name by remember { mutableStateOf("") }
    var command by remember { mutableStateOf("") }
    val canAdd = name.isNotBlank()

    Box(
        Modifier.fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .pointerHoverIcon(PointerIcon.Default)
            .onClick(rememberInteractionSource()) { onDismiss() },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.width(380.dp)
                .background(theme.popupBackground, theme.popupShape)
                .border(1.dp, theme.borderColor, theme.popupShape)
                // an empty click stops the scrim underneath from closing the dialog
                .onClick(rememberInteractionSource()) { }
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("New macro", color = theme.textColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(
                "Not bound to a key and not executed in this build.",
                color = theme.textColorSecondary,
                fontSize = 12.sp,
            )
            ZordyxSearchField(
                value = name,
                onValueChange = { name = it },
                placeholder = "Macro name",
                leadingIcon = "text-input",
            )
            ZordyxSearchField(
                value = command,
                onValueChange = { command = it },
                placeholder = "Command, for example /spawn",
                leadingIcon = "console",
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                Chip(label = "Cancel", selected = false) { onDismiss() }
                Chip(label = "Add macro", selected = canAdd) {
                    if (canAdd) {
                        ZordyxMacros.create(name, command)
                        onDismiss()
                    }
                }
            }
        }
    }
}
