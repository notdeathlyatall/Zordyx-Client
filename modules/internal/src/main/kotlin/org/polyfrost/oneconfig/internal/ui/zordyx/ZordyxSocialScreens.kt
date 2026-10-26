package org.polyfrost.oneconfig.internal.ui.zordyx

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.polyfrost.oneconfig.internal.ui.components.Icon
import org.polyfrost.oneconfig.internal.ui.components.Text
import org.polyfrost.oneconfig.internal.ui.components.settings.SwitchControl
import org.polyfrost.oneconfig.internal.ui.shell.PageGutter
import org.polyfrost.oneconfig.internal.ui.themes.Accent
import org.polyfrost.oneconfig.internal.ui.themes.LocalTheme

private enum class FriendStatus(val label: String) {
    Online("Online"),
    Idle("Idle"),
    Offline("Offline"),
}

private data class PreviewFriend(val name: String, val status: FriendStatus, val detail: String)

/** Placeholder entries so the friends interface can be reviewed before any account system exists. */
private val PreviewFriends = listOf(
    PreviewFriend("Alex", FriendStatus.Online, "In a lobby"),
    PreviewFriend("Steve", FriendStatus.Idle, "Away from keyboard"),
    PreviewFriend("Nova", FriendStatus.Online, "SkyBlock"),
    PreviewFriend("Ender", FriendStatus.Offline, "Last seen 2 hours ago"),
    PreviewFriend("Parker", FriendStatus.Offline, "Last seen yesterday"),
)

@Composable
fun ZordyxFriendsScreen(options: Boolean = false) {
    if (options) FriendsOptions() else FriendsList()
}

@Composable
private fun FriendsList() {
    val theme = LocalTheme.current
    var query by remember { mutableStateOf("") }
    val visible = PreviewFriends.filter {
        query.isBlank() || it.name.contains(query.trim(), ignoreCase = true) || it.detail.contains(query.trim(), ignoreCase = true)
    }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = PageGutter),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ZordyxSearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = "Search friends...",
                modifier = Modifier.weight(1f),
            )
            Text(
                "${visible.size} " + if (visible.size == 1) "friend" else "friends",
                color = theme.textColorSecondary,
                fontSize = 12.sp,
            )
        }

        ZordyxPreviewNote(
            "Preview data. Friends, requests and status are placeholders; nothing is loaded from a server.",
            Modifier.padding(horizontal = PageGutter),
        )

        if (visible.isEmpty()) {
            ZordyxEmptyState(
                icon = "search",
                title = "No friends match",
                body = "Try a different name, or clear the search.",
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(horizontal = PageGutter, vertical = 2.dp),
            ) {
                items(visible, key = { it.name }) { friend -> FriendRow(friend) }
            }
        }
    }
}

@Composable
private fun FriendRow(friend: PreviewFriend) {
    val theme = LocalTheme.current
    val statusColor = when (friend.status) {
        FriendStatus.Online -> Accent
        FriendStatus.Idle -> theme.favoriteColor
        FriendStatus.Offline -> theme.textColorSecondary.copy(alpha = 0.6f)
    }

    Row(
        Modifier.fillMaxWidth().heightIn(min = 54.dp)
            .background(theme.modCardBackground, theme.sideBarNavigationEntryShape)
            .border(1.dp, theme.borderColor, theme.sideBarNavigationEntryShape)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier.size(30.dp)
                .background(theme.componentBackground, CircleShape)
                .border(1.dp, theme.borderColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                friend.name.take(1).uppercase(),
                color = theme.textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(friend.name, color = theme.textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(friend.detail, color = theme.textColorSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(6.dp).background(statusColor, CircleShape))
            Text(friend.status.label, color = theme.textColorSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun FriendsOptions() {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = PageGutter)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ZordyxTitle("Friends options", "How friend requests and status are shown.")
            ZordyxPreviewNote("These preferences are visual only; no account or friend data is used.")
            PrototypeSwitch("friend-offline", "Show offline friends", "Keep offline friends in the list.", true)
            PrototypeSwitch("friend-requests", "Friend requests", "Show pending requests at the top of the list.", true)
            PrototypeSwitch("friend-status", "Share online status", "Let friends see when you are online.", false)
            PrototypeSwitch("friend-sounds", "Notification sounds", "Play a sound when a friend comes online.", false)
        }
    }
}

@Composable
fun ZordyxOnlineScreen(options: Boolean = false) {
    if (options) OnlineOptions() else OnlineOverview()
}

@Composable
private fun OnlineOverview() {
    val theme = LocalTheme.current
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = PageGutter),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ZordyxTitle("Zordyx Online", "Account, party and connection status.")
        ZordyxPreviewNote("Online services are not connected in this build. Every panel below is a placeholder.")

        OnlinePanel("Connection") {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(8.dp).background(theme.textColorSecondary.copy(alpha = 0.5f), CircleShape))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Offline", color = theme.textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("No connection to Zordyx Online.", color = theme.textColorSecondary, fontSize = 12.sp)
                }
                ZordyxTag("Not connected")
            }
        }

        OnlinePanel("Account") {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier.size(34.dp).background(theme.componentBackground, theme.sideBarNavigationEntryShape),
                    contentAlignment = Alignment.Center,
                ) { Icon("profiles", color = theme.textColorSecondary, modifier = Modifier.size(16.dp)) }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Not signed in", color = theme.textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("Signing in is disabled in this preview build.", color = theme.textColorSecondary, fontSize = 12.sp)
                }
                ZordyxTag("Placeholder")
            }
        }

        OnlinePanel("Party") {
            Text("No party", color = theme.textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Box(
                Modifier.fillMaxWidth().height(72.dp)
                    .background(theme.componentBackground, theme.sideBarNavigationEntryShape)
                    .border(1.dp, theme.borderColor, theme.sideBarNavigationEntryShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("Party members will appear here.", color = theme.textColorSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun OnlineOptions() {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = PageGutter)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ZordyxTitle("Online options", "What Zordyx Online is allowed to share.")
            ZordyxPreviewNote("These preferences are visual only; no network requests are made.")
            PrototypeSwitch("online-status", "Share online status", "Show your status to friends and party members.", true)
            PrototypeSwitch("online-party", "Party invites", "Allow friends to invite you to a party.", true)
            PrototypeSwitch("online-requests", "Friend requests", "Allow new friend requests.", true)
            PrototypeSwitch("online-leaderboard", "Leaderboards", "Include your profile on public leaderboards.", false)
        }
    }
}

@Composable
private fun OnlinePanel(title: String, content: @Composable ColumnScope.() -> Unit) {
    val theme = LocalTheme.current
    Column(
        Modifier.fillMaxWidth()
            .background(theme.modCardBackground, theme.sideBarNavigationEntryShape)
            .border(1.dp, theme.borderColor, theme.sideBarNavigationEntryShape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(title, color = theme.textColorSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        content()
    }
}

/** A toggle backed by [ZordyxState], for the prototype settings screens. */
@Composable
internal fun PrototypeSwitch(key: String, name: String, description: String, default: Boolean) {
    val enabled = ZordyxState.switch(key, default)
    ZordyxToggleRow(
        name = name,
        description = description,
        enabled = enabled,
        onToggle = { ZordyxState.setSwitch(key, !enabled) },
        trailing = { SwitchControl(enabled) { ZordyxState.setSwitch(key, !enabled) } },
    )
}
