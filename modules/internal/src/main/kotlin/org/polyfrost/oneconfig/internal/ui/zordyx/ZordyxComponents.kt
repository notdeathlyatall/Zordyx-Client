package org.polyfrost.oneconfig.internal.ui.zordyx

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.polyfrost.oneconfig.internal.ui.components.Icon
import org.polyfrost.oneconfig.internal.ui.components.IconButton
import org.polyfrost.oneconfig.internal.ui.components.Text
import org.polyfrost.oneconfig.internal.ui.components.onClick
import org.polyfrost.oneconfig.internal.ui.components.rememberInteractionSource
import org.polyfrost.oneconfig.internal.ui.components.settings.SwitchControl
import org.polyfrost.oneconfig.internal.ui.shell.PageGutter
import org.polyfrost.oneconfig.internal.ui.themes.Accent
import org.polyfrost.oneconfig.internal.ui.themes.LocalTheme

/** A field for filtering the list on the page itself, separate from the header search. */
@Composable
internal fun ZordyxSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: String = "search",
) {
    val theme = LocalTheme.current
    val interactionSource = rememberInteractionSource()
    val focused by interactionSource.collectIsFocusedAsState()
    val borderColor by animateColorAsState(if (focused) Accent.copy(alpha = 0.55f) else theme.borderColor)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        interactionSource = interactionSource,
        cursorBrush = SolidColor(Accent),
        textStyle = TextStyle(
            color = theme.textColor,
            fontSize = 13.sp,
            fontFamily = theme.typography.family,
        ),
        modifier = modifier,
    ) { innerTextField ->
        Row(
            Modifier.fillMaxWidth().height(34.dp)
                .background(theme.chipBackground, theme.sideBarNavigationEntryShape)
                .border(1.dp, borderColor, theme.sideBarNavigationEntryShape)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(leadingIcon, color = theme.textColorSecondary, modifier = Modifier.size(14.dp))
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        placeholder,
                        color = theme.textColorSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                innerTextField()
            }
            if (value.isNotEmpty()) {
                IconButton("close", Modifier.size(14.dp)) { onValueChange("") }
            }
        }
    }
}

/** A page title inside the content area, for the section views the header only names generically. */
@Composable
internal fun ZordyxTitle(title: String, description: String, modifier: Modifier = Modifier) {
    val theme = LocalTheme.current
    Column(modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, color = theme.textColor, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Text(description, color = theme.textColorSecondary, fontSize = 12.sp)
    }
}

@Composable
internal fun ZordyxTag(text: String) {
    val theme = LocalTheme.current
    Box(
        Modifier.background(Accent.copy(alpha = 0.14f), theme.checkBoxShape)
            .padding(horizontal = 5.dp, vertical = 1.dp)
    ) {
        Text(text, color = theme.textColorSecondary, fontSize = 9.sp, fontWeight = FontWeight.Medium)
    }
}

/**
 * One entry in a module, overlay or legit mod list.
 *
 * The whole row toggles, matching the way a module card footer behaves, while the switch stays a
 * separate target for pointer users.
 */
@Composable
internal fun ZordyxToggleRow(
    name: String,
    description: String,
    enabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    icon: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    tags: List<String> = emptyList(),
) {
    val theme = LocalTheme.current
    val interactionSource = rememberInteractionSource()
    val hovered by interactionSource.collectIsHoveredAsState()
    val background by animateColorAsState(if (hovered) theme.chipBackground else theme.modCardBackground)

    Row(
        modifier.fillMaxWidth().heightIn(min = 54.dp)
            .background(background, theme.sideBarNavigationEntryShape)
            .border(1.dp, theme.borderColor, theme.sideBarNavigationEntryShape)
            .pointerHoverIcon(PointerIcon.Hand)
            .onClick(interactionSource) { onToggle() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when {
            leading != null -> leading()
            icon != null -> Box(
                Modifier.size(30.dp).background(theme.componentBackground, theme.sideBarNavigationEntryShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    color = if (enabled) theme.textColor else theme.textColorSecondary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    name,
                    color = theme.textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                tags.forEach { ZordyxTag(it) }
            }
            if (description.isNotEmpty()) {
                Text(
                    description,
                    color = theme.textColorSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (trailing != null) trailing() else SwitchControl(enabled) { onToggle() }
    }
}

/** A row that opens another screen instead of holding a value. */
@Composable
internal fun ZordyxLinkRow(
    title: String,
    description: String,
    action: String,
    onClick: () -> Unit,
    icon: String? = null,
) {
    val theme = LocalTheme.current
    val interactionSource = rememberInteractionSource()
    val hovered by interactionSource.collectIsHoveredAsState()
    val background by animateColorAsState(if (hovered) theme.chipBackground else theme.modCardBackground)

    Row(
        Modifier.fillMaxWidth().heightIn(min = 54.dp)
            .background(background, theme.sideBarNavigationEntryShape)
            .border(1.dp, theme.borderColor, theme.sideBarNavigationEntryShape)
            .pointerHoverIcon(PointerIcon.Hand)
            .onClick(interactionSource) { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (icon != null) {
            Box(
                Modifier.size(30.dp).background(theme.componentBackground, theme.sideBarNavigationEntryShape),
                contentAlignment = Alignment.Center,
            ) { Icon(icon, color = theme.textColorSecondary, modifier = Modifier.size(16.dp)) }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = theme.textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(description, color = theme.textColorSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(action, color = theme.textColorSecondary, fontSize = 12.sp)
    }
}

/** A read-only setting row, for values a prototype cannot change. */
@Composable
internal fun ZordyxInfoRow(title: String, description: String, value: String, icon: String? = null) {
    val theme = LocalTheme.current
    Row(
        Modifier.fillMaxWidth().heightIn(min = 54.dp)
            .background(theme.modCardBackground, theme.sideBarNavigationEntryShape)
            .border(1.dp, theme.borderColor, theme.sideBarNavigationEntryShape)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (icon != null) {
            Box(
                Modifier.size(30.dp).background(theme.componentBackground, theme.sideBarNavigationEntryShape),
                contentAlignment = Alignment.Center,
            ) { Icon(icon, color = theme.textColorSecondary, modifier = Modifier.size(16.dp)) }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = theme.textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(description, color = theme.textColorSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        ZordyxTag(value)
    }
}

/** A static preview tile so an overlay entry has something to look at without touching the game. */
@Composable
internal fun ZordyxOverlayPreview(enabled: Boolean) {
    val theme = LocalTheme.current
    Box(
        Modifier.size(56.dp, 34.dp)
            .background(theme.componentBackground, theme.sideBarNavigationEntryShape)
            .border(1.dp, theme.borderColor, theme.sideBarNavigationEntryShape)
            .padding(6.dp)
    ) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Box(
                Modifier.fillMaxWidth(0.72f).height(4.dp)
                    .background(if (enabled) Accent else theme.textColor.copy(alpha = 0.45f), CircleShape)
            )
            Box(Modifier.fillMaxWidth(0.46f).height(3.dp).background(theme.textColorSecondary.copy(alpha = 0.5f), CircleShape))
            Box(Modifier.fillMaxWidth(0.6f).height(3.dp).background(theme.textColorSecondary.copy(alpha = 0.3f), CircleShape))
        }
    }
}

@Composable
internal fun ZordyxEmptyState(icon: String, title: String, body: String, modifier: Modifier = Modifier) {
    val theme = LocalTheme.current
    Box(modifier.fillMaxSize().padding(horizontal = PageGutter), contentAlignment = Alignment.Center) {
        Column(
            Modifier.widthIn(max = 360.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Box(
                Modifier.size(46.dp).background(theme.chipBackground, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, color = theme.textColorSecondary, modifier = Modifier.size(20.dp))
            }
            Text(title, color = theme.textColor, fontSize = 15.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
            Text(
                body,
                color = theme.textColorSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Separates pure-preview screens from anything that looks like it changes the game. */
@Composable
internal fun ZordyxPreviewNote(text: String, modifier: Modifier = Modifier) {
    val theme = LocalTheme.current
    Row(
        modifier.fillMaxWidth()
            .background(theme.chipBackground, theme.sideBarNavigationEntryShape)
            .border(1.dp, theme.borderColor, theme.sideBarNavigationEntryShape)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon("info", color = theme.textColorSecondary, modifier = Modifier.size(14.dp))
        Text(text, color = theme.textColorSecondary, fontSize = 12.sp)
    }
}
