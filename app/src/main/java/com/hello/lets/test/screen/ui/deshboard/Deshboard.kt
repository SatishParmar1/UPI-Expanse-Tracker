package com.hello.lets.test.screen.ui.deshboard

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hello.lets.test.screen.ui.analytics.AnalyticsScreen
import com.hello.lets.test.screen.ui.goals.GoalsScreen
import com.hello.lets.test.screen.ui.homepage.Homepage
import com.hello.lets.test.screen.ui.settings.KeywordRulesScreen
import com.hello.lets.test.screen.ui.settings.SettingsScreen

/**
 * Navigation destinations for the bottom navigation bar.
 */
enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    HOME("homepage", "Dashboard", Icons.Default.Home, "Dashboard"),
    GOALS("goals", "Goals", Icons.Default.Savings, "Goals"),
    ANALYTICS("analytics", "Analytics", Icons.AutoMirrored.Filled.TrendingUp, "Analytics"),
    SETTINGS("settings", "Settings", Icons.Default.Settings, "Settings")
}

/**
 * Main navigation host for the app.
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Destination,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier
    ) {
        composable(Destination.HOME.route) {
            Homepage()
        }
        composable(Destination.GOALS.route) {
            GoalsScreen()
        }
        composable(Destination.ANALYTICS.route) {
            AnalyticsScreen()
        }
        composable(Destination.SETTINGS.route) {
            SettingsScreen(
                onNavigateToKeywordRules = {
                    navController.navigate("keyword_rules")
                }
            )
        }
        // Keyword Rules sub-screen
        composable("keyword_rules") {
            KeywordRulesScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Main dashboard screen with bottom navigation.
 */
@Preview(showBackground = true)
@Composable
fun Deshboard(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val startDestination = Destination.HOME
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                windowInsets = NavigationBarDefaults.windowInsets
            ) {
                Destination.entries.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selectedDestination == index,
                        onClick = {
                            if (selectedDestination != index) {
                                navController.navigate(route = destination.route) {
                                    // Pop up to the start destination to avoid building up a large stack
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination
                                    launchSingleTop = true
                                    // Restore state when re-selecting a previously selected item
                                    restoreState = true
                                }
                                selectedDestination = index
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.contentDescription
                            )
                        },
                        label = {
                            Text(
                                text = destination.label,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    ) { contentPadding ->
        AppNavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(contentPadding)
        )
    }
}