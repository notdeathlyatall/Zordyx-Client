package org.polyfrost.oneconfig.internal.ui.navigation

import androidx.compose.runtime.mutableStateMapOf
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.toRoute
import org.polyfrost.oneconfig.internal.ui.navigation.graph.ProfilesGraph
import org.polyfrost.oneconfig.internal.ui.navigation.graph.ZordyxFriendsRoute
import org.polyfrost.oneconfig.internal.ui.navigation.graph.ZordyxLegitModsRoute
import org.polyfrost.oneconfig.internal.ui.navigation.graph.ZordyxMacrosRoute
import org.polyfrost.oneconfig.internal.ui.navigation.graph.ZordyxModulesRoute
import org.polyfrost.oneconfig.internal.ui.navigation.graph.ZordyxOnlineRoute
import org.polyfrost.oneconfig.internal.ui.navigation.graph.ZordyxOverlaysRoute
import org.polyfrost.oneconfig.internal.ui.navigation.graph.ZordyxSettingsRoute
import org.polyfrost.oneconfig.internal.ui.zordyx.ModuleCategory
import org.polyfrost.oneconfig.internal.ui.zordyx.SettingsSection
import org.polyfrost.oneconfig.internal.ui.zordyx.ZordyxCatalog

class ZordyxNavChild(val title: String, val route: Any)

class ZordyxNavSection(
    val id: String,
    val title: String,
    val icon: String,
    val route: Any,
    val children: List<ZordyxNavChild> = emptyList(),
)

/**
 * The sidebar hierarchy: eight top level sections, with the subsections and items that belong to them.
 *
 * Routes rather than pages so a subsection can open one screen on a specific tab, for example a
 * category in [ZordyxModulesRoute] or a panel in [ZordyxSettingsRoute].
 */
object ZordyxNav {
    val sections: List<ZordyxNavSection> = listOf(
        ZordyxNavSection(
            "modules", "Modules", "combat", ZordyxModulesRoute(),
            ModuleCategory.entries.map { ZordyxNavChild(it.title, ZordyxModulesRoute(it.name)) },
        ),
        ZordyxNavSection(
            "overlays", "Overlays", "hud", ZordyxOverlaysRoute(),
            ZordyxCatalog.overlays.map { ZordyxNavChild(it.name, ZordyxOverlaysRoute(it.id)) },
        ),
        ZordyxNavSection("legit-mods", "Legit mods", "check-circle", ZordyxLegitModsRoute),
        ZordyxNavSection("profiles", "Profiles", "profiles", ProfilesGraph),
        ZordyxNavSection(
            "friends", "Friends", "star", ZordyxFriendsRoute(),
            listOf(ZordyxNavChild("Options", ZordyxFriendsRoute(options = true))),
        ),
        ZordyxNavSection("macros", "Macros", "keyboard", ZordyxMacrosRoute),
        ZordyxNavSection(
            "online", "Online", "cloud", ZordyxOnlineRoute(),
            listOf(ZordyxNavChild("Options", ZordyxOnlineRoute(options = true))),
        ),
        ZordyxNavSection(
            "settings", "Global settings", "cog", ZordyxSettingsRoute(),
            SettingsSection.entries.map { ZordyxNavChild(it.title, ZordyxSettingsRoute(it.name)) },
        ),
    )

    /**
     * Whether [route] is the destination on top of the stack.
     *
     * Parameterised routes compare their arguments, otherwise every subsection of a section would read
     * as selected at once.
     */
    fun isActive(entry: NavBackStackEntry?, route: Any): Boolean {
        val entry = entry ?: return false
        val destination = entry.destination
        return when (route) {
            is ZordyxModulesRoute ->
                destination.hasRoute(ZordyxModulesRoute::class) && entry.routeOrNull<ZordyxModulesRoute>() == route

            is ZordyxOverlaysRoute ->
                destination.hasRoute(ZordyxOverlaysRoute::class) && entry.routeOrNull<ZordyxOverlaysRoute>() == route

            is ZordyxFriendsRoute ->
                destination.hasRoute(ZordyxFriendsRoute::class) && entry.routeOrNull<ZordyxFriendsRoute>() == route

            is ZordyxOnlineRoute ->
                destination.hasRoute(ZordyxOnlineRoute::class) && entry.routeOrNull<ZordyxOnlineRoute>() == route

            is ZordyxSettingsRoute ->
                destination.hasRoute(ZordyxSettingsRoute::class) && entry.routeOrNull<ZordyxSettingsRoute>() == route

            else -> destination.hierarchy.any { it.hasRoute(route::class) }
        }
    }

    fun isSectionActive(entry: NavBackStackEntry?, section: ZordyxNavSection): Boolean =
        isActive(entry, section.route) || section.children.any { isActive(entry, it.route) }
}

/** Which sections are open, kept across navigation so the sidebar does not reset under the pointer. */
object ZordyxNavState {
    private val expanded = mutableStateMapOf<String, Boolean>()

    fun isExpanded(id: String): Boolean = expanded[id] ?: (id == "modules")

    fun toggle(id: String) {
        expanded[id] = !isExpanded(id)
    }

    fun expand(id: String) {
        expanded[id] = true
    }
}

private inline fun <reified T : Any> NavBackStackEntry.routeOrNull(): T? = runCatching { toRoute<T>() }.getOrNull()
