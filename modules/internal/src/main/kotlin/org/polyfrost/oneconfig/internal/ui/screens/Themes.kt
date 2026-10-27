package org.polyfrost.oneconfig.internal.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.polyfrost.oneconfig.internal.ZordyxClientInfo
import org.polyfrost.oneconfig.internal.ui.api.ConfigRegistry
import org.polyfrost.oneconfig.internal.ui.components.Text
import org.polyfrost.oneconfig.internal.ui.components.rememberInteractionSource
import org.polyfrost.oneconfig.internal.ui.components.rememberSvgResourcePainter
import org.polyfrost.oneconfig.internal.ui.shell.PageGutter
import org.polyfrost.oneconfig.internal.ui.themes.Accent
import org.polyfrost.oneconfig.internal.ui.themes.LocalTheme
import org.polyfrost.oneconfig.internal.ui.themes.ThemeRegistry
import org.polyfrost.oneconfig.internal.ui.themes.UITheme

const val THEMES_ID = ZordyxClientInfo.THEMES_ID

@Composable
fun Themes() {
    val revision = ConfigRegistry.revision
    val tree = ConfigRegistry.findTree(THEMES_ID)
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(26.dp),
            contentPadding = PaddingValues(horizontal = PageGutter),
        ) {
            items(ThemeRegistry.registry) {
                ThemeCard(it)
            }
        }
        if (tree == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No preferences available.", color = LocalTheme.current.textColorSecondary)
            }
            return
        }
        key(revision, tree) {
            ConfigScreen(tree, pageKey = THEMES_ID)
        }
    }
}

@Composable
private fun ThemeCard(theme: UITheme) {
    val interactionSource = rememberInteractionSource()
    val isSelected = theme == LocalTheme.current

    val borderColor by animateColorAsState(
        if (isSelected) Accent else LocalTheme.current.borderColor
    )
    val borderWidth by animateDpAsState(
        if (isSelected) 3.dp else 1.dp
    )

    Column(
        modifier = Modifier
            .background(LocalTheme.current.modCardBackground, LocalTheme.current.backgroundShape)
            .border(borderWidth, borderColor, LocalTheme.current.backgroundShape)
            .clickable(interactionSource, indication = null) {
                // Use clickable because onClick always plays CLICK. ThemeRegistry.activate chooses CLICK or OPEN
                ThemeRegistry.activate(theme)
            }
            .pointerHoverIcon(PointerIcon.Hand)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // a custom theme's preview comes from its own file and may be missing, so it falls back to a plain panel
        // instead of failing to resolve a resource that is not in the jar
        val preview = rememberSvgResourcePainter(theme.previewImage)
        if (preview != null) {
            Image(
                painter = preview,
                contentDescription = null,
                modifier = Modifier.size(168.dp, 108.dp)
            )
        } else {
            Box(
                Modifier
                    .size(168.dp, 108.dp)
                    .background(
                        LocalTheme.current.componentBackground,
                        LocalTheme.current.checkBoxShape
                    )
            )
        }
        Text(theme.name, color = LocalTheme.current.textColor)
        theme.description?.let { description ->
            Text(description, color = LocalTheme.current.textColorSecondary, fontSize = 12.sp)
        }
    }
}
