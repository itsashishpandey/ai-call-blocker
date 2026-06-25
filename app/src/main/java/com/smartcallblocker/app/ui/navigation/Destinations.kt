package com.smartcallblocker.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistAddCheck
import androidx.compose.material.icons.automirrored.rounded.PlaylistAddCheck
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Destination(val route: String) {
    data object Splash : Destination("splash")
    data object Onboarding : Destination("onboarding")
    data object Dashboard : Destination("dashboard")
    data object Blocked : Destination("blocked")
    data object Rules : Destination("rules")
    data object Whitelist : Destination("whitelist")
    data object Blacklist : Destination("blacklist")
    data object Settings : Destination("settings")
    data object Statistics : Destination("statistics")
    data object AddRule : Destination("rules/new") {
        const val ARG_ID = "ruleId"
        fun editRoute(id: Long) = "rules/edit/$id"
        const val EDIT_PATTERN = "rules/edit/{ruleId}"
    }
    data object About : Destination("legal/about")
    data object Privacy : Destination("legal/privacy")
    data object FairUse : Destination("legal/fairuse")
    data object Backup : Destination("backup")
}

data class BottomNavItem(
    val destination: Destination,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem(
        Destination.Dashboard, "Home",
        Icons.Rounded.Shield, Icons.Outlined.Shield,
    ),
    BottomNavItem(
        Destination.Blocked, "Blocked",
        Icons.Rounded.History, Icons.Outlined.History,
    ),
    BottomNavItem(
        Destination.Rules, "Rules",
        Icons.Rounded.Tune, Icons.Outlined.Tune,
    ),
    BottomNavItem(
        Destination.Whitelist, "Allow",
        Icons.AutoMirrored.Rounded.PlaylistAddCheck,
        Icons.AutoMirrored.Outlined.PlaylistAddCheck,
    ),
    BottomNavItem(
        Destination.Blacklist, "Block",
        Icons.Rounded.Block, Icons.Outlined.Block,
    ),
)

/** Routes that should display the bottom navigation bar. */
val topLevelRoutes = bottomNavItems.map { it.destination.route }.toSet()
