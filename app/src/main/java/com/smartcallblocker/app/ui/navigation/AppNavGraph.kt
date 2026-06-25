package com.smartcallblocker.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.smartcallblocker.app.ui.screens.backup.BackupScreen
import com.smartcallblocker.app.ui.screens.blacklist.BlacklistScreen
import com.smartcallblocker.app.ui.screens.blocked.BlockedCallsScreen
import com.smartcallblocker.app.ui.screens.dashboard.DashboardScreen
import com.smartcallblocker.app.ui.screens.legal.AboutScreen
import com.smartcallblocker.app.ui.screens.legal.FairUsePolicyScreen
import com.smartcallblocker.app.ui.screens.legal.PrivacyPolicyScreen
import com.smartcallblocker.app.ui.screens.onboarding.OnboardingScreen
import com.smartcallblocker.app.ui.screens.rules.AddEditRuleScreen
import com.smartcallblocker.app.ui.screens.rules.RulesScreen
import com.smartcallblocker.app.ui.screens.settings.SettingsScreen
import com.smartcallblocker.app.ui.screens.splash.SplashScreen
import com.smartcallblocker.app.ui.screens.statistics.StatisticsScreen
import com.smartcallblocker.app.ui.screens.whitelist.WhitelistScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Destination.Splash.route,
) {
    AppScaffold(navController = navController) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(tween(220)) },
            exitTransition = { fadeOut(tween(180)) },
            popEnterTransition = { fadeIn(tween(220)) },
            popExitTransition = { fadeOut(tween(180)) },
        ) {
            composable(Destination.Splash.route) {
                SplashScreen(onFinished = {
                    // SplashRouter (in MainActivity) decides Onboarding vs Dashboard.
                    // Here we just default to Dashboard; MainActivity overrides startDestination.
                    navController.navigate(Destination.Dashboard.route) {
                        popUpTo(Destination.Splash.route) { inclusive = true }
                    }
                })
            }
            composable(Destination.Onboarding.route) {
                OnboardingScreen(onFinished = {
                    navController.navigate(Destination.Dashboard.route) {
                        popUpTo(Destination.Onboarding.route) { inclusive = true }
                    }
                })
            }
            composable(Destination.Dashboard.route) {
                DashboardScreen(
                    onOpenBlocked = { navController.navigate(Destination.Blocked.route) },
                    onOpenRules = { navController.navigate(Destination.Rules.route) },
                    onOpenSettings = { navController.navigate(Destination.Settings.route) },
                    onOpenStatistics = { navController.navigate(Destination.Statistics.route) },
                    onAddRule = { navController.navigate(Destination.AddRule.route) },
                    onOpenWhitelist = { navController.navigate(Destination.Whitelist.route) },
                    onOpenBlacklist = { navController.navigate(Destination.Blacklist.route) },
                )
            }
            composable(Destination.Blocked.route) {
                BlockedCallsScreen(onBack = { navController.popBackStack() })
            }
            composable(Destination.Rules.route) {
                RulesScreen(
                    onBack = { navController.popBackStack() },
                    onAdd = { navController.navigate(Destination.AddRule.route) },
                    onEdit = { id -> navController.navigate(Destination.AddRule.editRoute(id)) },
                )
            }
            composable(Destination.AddRule.route) {
                AddEditRuleScreen(ruleId = null, onBack = { navController.popBackStack() })
            }
            composable(
                route = Destination.AddRule.EDIT_PATTERN,
                arguments = listOf(navArgument(Destination.AddRule.ARG_ID) { type = NavType.LongType }),
            ) { entry ->
                val id = entry.arguments?.getLong(Destination.AddRule.ARG_ID)
                AddEditRuleScreen(ruleId = id, onBack = { navController.popBackStack() })
            }
            composable(Destination.Whitelist.route) {
                WhitelistScreen(onBack = { navController.popBackStack() })
            }
            composable(Destination.Blacklist.route) {
                BlacklistScreen(onBack = { navController.popBackStack() })
            }
            composable(Destination.Statistics.route) {
                StatisticsScreen(onBack = { navController.popBackStack() })
            }
            composable(Destination.Settings.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenAbout = { navController.navigate(Destination.About.route) },
                    onOpenPrivacy = { navController.navigate(Destination.Privacy.route) },
                    onOpenFairUse = { navController.navigate(Destination.FairUse.route) },
                    onOpenBackup = { navController.navigate(Destination.Backup.route) },
                )
            }
            composable(Destination.Backup.route) {
                BackupScreen(onBack = { navController.popBackStack() })
            }
            composable(Destination.About.route) {
                AboutScreen(onBack = { navController.popBackStack() })
            }
            composable(Destination.Privacy.route) {
                PrivacyPolicyScreen(onBack = { navController.popBackStack() })
            }
            composable(Destination.FairUse.route) {
                FairUsePolicyScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
