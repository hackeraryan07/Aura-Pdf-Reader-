package com.example.ui.screens

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.ui.theme.Typography

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Files : Screen("files", "Files", Icons.Outlined.Description, Icons.Filled.Description)
    object Recent : Screen("recent", "Recent", Icons.Outlined.Schedule, Icons.Filled.Schedule)
    object Settings : Screen("settings", "Settings", Icons.Outlined.Settings, Icons.Filled.Settings)
    object Search : Screen("search", "Search", Icons.Outlined.Description, Icons.Filled.Description)
    object Permission : Screen("permission", "Permission", Icons.Outlined.Description, Icons.Filled.Description)
    object AiIntegration : Screen("ai_integration", "AI Integration", Icons.Outlined.Settings, Icons.Filled.Settings)
}

val items = listOf(
    Screen.Files,
    Screen.Recent,
    Screen.Settings
)

fun hasStoragePermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
}

@Composable
fun MainAppScreen() {
    val context = LocalContext.current
    val startDestination = remember {
        if (hasStoragePermission(context)) Screen.Files.route else Screen.Permission.route
    }
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            
            if (currentRoute in items.map { it.route }) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (currentRoute == screen.route) screen.selectedIcon else screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title, style = Typography.labelSmall) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            composable(Screen.Files.route) { FilesScreen(navController) }
            composable(Screen.Recent.route) { RecentScreen(navController) }
            composable(Screen.Settings.route) { SettingsScreen(navController) }
            composable(Screen.Search.route) { SearchScreen(navController) }
            composable(Screen.Permission.route) { PermissionScreen(navController) }
            composable(Screen.AiIntegration.route) { AiIntegrationScreen(navController) }
            composable(
                route = "pdf_viewer?uri={uri}",
                arguments = listOf(navArgument("uri") { type = NavType.StringType })
            ) { backStackEntry ->
                val uriString = backStackEntry.arguments?.getString("uri") ?: ""
                PdfViewerScreen(navController, uriString)
            }
        }
    }
}
