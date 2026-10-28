package org.polyfrost.oneconfig.internal.ui.navigation.graph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.polyfrost.oneconfig.internal.ui.zordyx.ModuleCategory
import org.polyfrost.oneconfig.internal.ui.zordyx.SettingsSection
import org.polyfrost.oneconfig.internal.ui.zordyx.ZordyxFriendsScreen
import org.polyfrost.oneconfig.internal.ui.zordyx.ZordyxLegitModsScreen
import org.polyfrost.oneconfig.internal.ui.zordyx.ZordyxMacrosScreen
import org.polyfrost.oneconfig.internal.ui.zordyx.ZordyxModuleSettingsScreen
import org.polyfrost.oneconfig.internal.ui.zordyx.ZordyxModulesScreen
import org.polyfrost.oneconfig.internal.ui.zordyx.ZordyxOnlineScreen
import org.polyfrost.oneconfig.internal.ui.zordyx.ZordyxOverlaysScreen
import org.polyfrost.oneconfig.internal.ui.zordyx.ZordyxSettingsScreen

@Serializable
data object ZordyxModulesGraph

@Serializable
data class ZordyxModulesRoute(val category: String? = null)

@Serializable
data class ZordyxModuleSettingsRoute(val module: String)

@Serializable
data object ZordyxOverlaysGraph

@Serializable
data class ZordyxOverlaysRoute(val overlay: String? = null)

@Serializable
data object ZordyxLegitModsGraph

@Serializable
data object ZordyxLegitModsRoute

@Serializable
data object ZordyxFriendsGraph

@Serializable
data class ZordyxFriendsRoute(val options: Boolean = false)

@Serializable
data object ZordyxMacrosGraph

@Serializable
data object ZordyxMacrosRoute

@Serializable
data object ZordyxOnlineGraph

@Serializable
data class ZordyxOnlineRoute(val options: Boolean = false)

@Serializable
data object ZordyxSettingsGraph

@Serializable
data class ZordyxSettingsRoute(val section: String? = null)

fun NavGraphBuilder.zordyxGraph() {
    navigation<ZordyxModulesGraph>(startDestination = ZordyxModulesRoute()) {
        composable<ZordyxModulesRoute> { entry ->
            val route = entry.toRoute<ZordyxModulesRoute>()
            ZordyxModulesScreen(ModuleCategory.byId(route.category) ?: ModuleCategory.Combat)
        }
        composable<ZordyxModuleSettingsRoute> { entry ->
            ZordyxModuleSettingsScreen(entry.toRoute<ZordyxModuleSettingsRoute>().module)
        }
    }
    navigation<ZordyxOverlaysGraph>(startDestination = ZordyxOverlaysRoute()) {
        composable<ZordyxOverlaysRoute> { entry ->
            ZordyxOverlaysScreen(entry.toRoute<ZordyxOverlaysRoute>().overlay)
        }
    }
    navigation<ZordyxLegitModsGraph>(startDestination = ZordyxLegitModsRoute) {
        composable<ZordyxLegitModsRoute> {
            ZordyxLegitModsScreen()
        }
    }
    navigation<ZordyxFriendsGraph>(startDestination = ZordyxFriendsRoute()) {
        composable<ZordyxFriendsRoute> { entry ->
            ZordyxFriendsScreen(options = entry.toRoute<ZordyxFriendsRoute>().options)
        }
    }
    navigation<ZordyxMacrosGraph>(startDestination = ZordyxMacrosRoute) {
        composable<ZordyxMacrosRoute> {
            ZordyxMacrosScreen()
        }
    }
    navigation<ZordyxOnlineGraph>(startDestination = ZordyxOnlineRoute()) {
        composable<ZordyxOnlineRoute> { entry ->
            ZordyxOnlineScreen(options = entry.toRoute<ZordyxOnlineRoute>().options)
        }
    }
    navigation<ZordyxSettingsGraph>(startDestination = ZordyxSettingsRoute()) {
        composable<ZordyxSettingsRoute> { entry ->
            ZordyxSettingsScreen(
                SettingsSection.byId(entry.toRoute<ZordyxSettingsRoute>().section) ?: SettingsSection.General
            )
        }
    }
}
