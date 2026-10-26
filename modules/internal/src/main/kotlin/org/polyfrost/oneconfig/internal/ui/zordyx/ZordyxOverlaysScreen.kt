package org.polyfrost.oneconfig.internal.ui.zordyx

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.polyfrost.oneconfig.internal.ui.shell.PageGutter

/**
 * The seven HUD overlays, each with a static preview tile.
 *
 * The [selected] id comes from the sidebar so choosing an overlay there scrolls its row into view. No
 * overlay is rendered over the game.
 */
@Composable
fun ZordyxOverlaysScreen(selected: String? = null) {
    val listState = rememberLazyListState()
    val focusIndex = remember(selected) { ZordyxCatalog.overlays.indexOfFirst { it.id == selected } }

    LaunchedEffect(focusIndex) {
        if (focusIndex >= 0) listState.animateScrollToItem(focusIndex)
    }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ZordyxTitle(
            "HUD overlays",
            "Previews of the overlays that can be placed on your screen.",
            Modifier.padding(horizontal = PageGutter),
        )
        ZordyxPreviewNote(
            "Previews are static. Enabling an overlay only changes this list.",
            Modifier.padding(horizontal = PageGutter),
        )
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = PageGutter, vertical = 2.dp),
        ) {
            items(ZordyxCatalog.overlays, key = { it.id }) { overlay ->
                val enabled = ZordyxState.isEnabled(overlay.id, overlay.enabled)
                ZordyxToggleRow(
                    name = overlay.name,
                    description = overlay.description,
                    enabled = enabled,
                    onToggle = { ZordyxState.setEnabled(overlay.id, !enabled) },
                    leading = { ZordyxOverlayPreview(enabled) },
                )
            }
        }
    }
}
