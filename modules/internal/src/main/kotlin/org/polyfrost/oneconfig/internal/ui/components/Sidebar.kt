package org.polyfrost.oneconfig.internal.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.compose.currentBackStackEntryAsState
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.oneconfig.api.notifications.v1.NotificationsManager
import org.polyfrost.oneconfig.internal.ThemeConfig
import org.polyfrost.oneconfig.internal.ui.navigation.ZordyxNav
import org.polyfrost.oneconfig.internal.ui.navigation.ZordyxNavChild
import org.polyfrost.oneconfig.internal.ui.navigation.ZordyxNavState
import org.polyfrost.oneconfig.internal.ui.shell.LocalShell
import org.polyfrost.oneconfig.internal.ui.shell.LocalShellNavController
import org.polyfrost.oneconfig.internal.ui.themes.Accent
import org.polyfrost.oneconfig.internal.ui.themes.AccentBrush
import org.polyfrost.oneconfig.internal.ui.themes.LocalTheme
import org.polyfrost.oneconfig.internal.ui.themes.withOpacityPercent

private val SidebarWidth = 240.dp
private val RailPadding = 12.dp
private val SectionRowHeight = 34.dp
private val ChildRowHeight = 28.dp
private const val ExpandMillis = 180
private const val CollapseMillis = 140

@Composable
fun Sidebar() {
    val appearance = LocalShell.current.appearance
    val flipTopOrder = appearance.flipTopOptionOrder
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxHeight()
            .width(SidebarWidth)
            .background(LocalTheme.current.sidebarBackground.withOpacityPercent(appearance.sidebarOpacity))
            .rightBorder(LocalTheme.current.borderColor, 1f),
    ) {
        Branding()

        ScrollbarBox(scrollState, 10.dp, Modifier.weight(1f)) {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (!flipTopOrder) {
                    TopOptionsSection()
                }
                NavigationEntries()
                Box(Modifier.height(12.dp))
            }
        }

        if (flipTopOrder) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TopOptionsSection()
            }
        }

        Account()
    }
}

@Composable
private fun Branding() {
    val theme = LocalTheme.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 12.dp, top = 22.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Box(
            Modifier.size(26.dp).background(AccentBrush, theme.sideBarNavigationEntryShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "Z",
                color = theme.accentTextColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                "ZORDYX",
                color = theme.textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "CLIENT",
                color = theme.textColorSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun TopOptionsSection() {
    Column(Modifier.fillMaxWidth().padding(horizontal = RailPadding, vertical = 2.dp)) {
        NavigationRow(
            icon = "hud",
            title = translated("oneconfig.sidebar.edit_hud", "Edit HUD"),
            selected = false,
            onClick = { HudManager.openEditor() },
        )
    }
}

@Composable
private fun NavigationEntries() {
    val entry by LocalShellNavController.current.currentBackStackEntryAsState()
    val shell = LocalShell.current

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = RailPadding),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        ZordyxNav.sections.forEach { section ->
            val active = ZordyxNav.isSectionActive(entry, section)
            val expanded = section.children.isNotEmpty() && ZordyxNavState.isExpanded(section.id)

            NavigationRow(
                icon = section.icon,
                title = section.title,
                selected = active,
                expandable = section.children.isNotEmpty(),
                expanded = expanded,
                onClick = {
                    if (section.children.isNotEmpty()) ZordyxNavState.expand(section.id)
                    if (!ZordyxNav.isActive(entry, section.route)) shell.navigation.navigate(section.route)
                },
                onToggleExpanded = { ZordyxNavState.toggle(section.id) },
            )

            AnimatedVisibility(
                visible = expanded,
                enter = if (ThemeConfig.animations) expandVertically(tween(ExpandMillis)) + fadeIn(tween(ExpandMillis)) else EnterTransition.None,
                exit = if (ThemeConfig.animations) shrinkVertically(tween(CollapseMillis)) + fadeOut(tween(CollapseMillis)) else ExitTransition.None,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 1.dp, bottom = 5.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    section.children.forEach { child ->
                        NavigationChildRow(
                            child = child,
                            selected = ZordyxNav.isActive(entry, child.route),
                            onClick = {
                                ZordyxNavState.expand(section.id)
                                if (!ZordyxNav.isActive(entry, child.route)) shell.navigation.navigate(child.route)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationRow(
    icon: String,
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    expandable: Boolean = false,
    expanded: Boolean = false,
    onToggleExpanded: (() -> Unit)? = null,
) {
    val theme = LocalTheme.current
    val interactionSource = rememberInteractionSource()
    val chevronSource = rememberInteractionSource()
    val hovered by interactionSource.collectIsHoveredAsState()
    val chevronHovered by chevronSource.collectIsHoveredAsState()

    val selectedAlpha by animateFloatAsState(if (selected) 1f else 0f)
    val accentTint = lerp(Accent, theme.textColor, 0.4f)
    val contentColor by animateColorAsState(
        when {
            selected -> accentTint
            hovered -> theme.textColor
            else -> theme.textColor.copy(alpha = 0.72f)
        }
    )
    val hoverBackground by animateColorAsState(
        if (hovered && !selected) theme.textColor.copy(alpha = 0.05f) else Color.Transparent
    )
    val chevronRotation by animateFloatAsState(if (expanded) 0f else -90f)

    Row(
        modifier = Modifier.fillMaxWidth().height(SectionRowHeight)
            .background(hoverBackground, theme.sideBarNavigationEntryShape)
            .background(AccentBrush, theme.sideBarNavigationEntryShape, selectedAlpha * 0.16f)
            .pointerHoverIcon(PointerIcon.Hand)
            .onClick(interactionSource) { onClick() }
            .padding(start = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            color = contentColor,
            modifier = Modifier.size(16.dp),
        )
        Text(
            title,
            color = contentColor,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1,
            modifier = Modifier.weight(1f).padding(start = 10.dp),
        )
        if (expandable) {
            Box(
                modifier = Modifier.fillMaxHeight().width(30.dp)
                    .onClick(chevronSource) { onToggleExpanded?.invoke() }
                    .pointerHoverIcon(PointerIcon.Hand),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    "down",
                    color = when {
                        selected -> accentTint
                        chevronHovered -> theme.textColor
                        else -> theme.textColorSecondary
                    },
                    modifier = Modifier.size(14.dp).graphicsLayer { rotationZ = chevronRotation },
                )
            }
        } else {
            Box(Modifier.width(6.dp))
        }
    }
}

@Composable
private fun NavigationChildRow(child: ZordyxNavChild, selected: Boolean, onClick: () -> Unit) {
    val theme = LocalTheme.current
    val interactionSource = rememberInteractionSource()
    val hovered by interactionSource.collectIsHoveredAsState()
    val accentTint = lerp(Accent, theme.textColor, 0.4f)
    val selectedAlpha by animateFloatAsState(if (selected) 1f else 0f)
    val hoverBackground by animateColorAsState(
        if (hovered && !selected) theme.textColor.copy(alpha = 0.04f) else Color.Transparent
    )

    Row(
        modifier = Modifier.fillMaxWidth().height(ChildRowHeight)
            .background(hoverBackground, theme.sideBarNavigationEntryShape)
            .background(AccentBrush, theme.sideBarNavigationEntryShape, selectedAlpha * 0.11f)
            .pointerHoverIcon(PointerIcon.Hand)
            .onClick(interactionSource) { onClick() }
            .padding(start = 28.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(3.dp)
                .background(if (selected) Accent else theme.textColorSecondary.copy(alpha = 0.28f), CircleShape)
        )
        Text(
            child.title,
            color = if (selected) accentTint else if (hovered) theme.textColor else theme.textColorSecondary,
            fontSize = 12.sp,
            maxLines = 1,
            modifier = Modifier.padding(start = 9.dp),
        )
    }
}

@Composable
private fun NotificationBell() {
    var expanded by remember { mutableStateOf(false) }
    var bellHeightPx by remember { mutableStateOf(0) }
    val interactionSource = rememberInteractionSource()

    Box(
        modifier = Modifier
            .onSizeChanged { bellHeightPx = it.height }
            .onClick(interactionSource) {
                expanded = !expanded
                if (!expanded) NotificationsManager.markAllRead()
            }
            .pointerHoverIcon(PointerIcon.Hand)
    ) {
        IconWithIndicator(
            iconName = "bell",
            color = LocalTheme.current.textColor,
            modifier = Modifier.size(18.dp),
            showIndicator = NotificationsManager.unreadCount > 0,
        )

        if (expanded) {
            ShellPopup(
                // open up and to the right of the bell
                alignment = Alignment.BottomStart,
                offset = IntOffset(0, -(bellHeightPx + 12)),
                onDismissRequest = {
                    expanded = false
                    NotificationsManager.markAllRead()
                },
                properties = PopupProperties(focusable = true),
            ) {
                Box(Modifier.shellTransition()) { NotificationsCenter() }
            }
        }
    }
}

@Composable
private fun Account() {
    Box(
        modifier = Modifier.fillMaxWidth().height(73.dp)
            .topBorder(LocalTheme.current.borderColor, 1f),
        contentAlignment = Alignment.Center
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlayerHead(Modifier.size(32.dp).offset(x = 2.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    val session = LocalShell.current.session
                    Text(session.playerName, color = LocalTheme.current.textColor)
                    Text(
                        session.versionLabel.ifBlank { "Zordyx Client" },
                        color = LocalTheme.current.textColorSecondary,
                        fontSize = 11.sp
                    )
                }
            }
            NotificationBell()
        }
    }
}
