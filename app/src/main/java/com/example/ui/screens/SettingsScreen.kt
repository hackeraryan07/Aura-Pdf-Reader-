package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.theme.Typography
import com.example.model.SettingsManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import com.example.model.CacheManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager.getInstance(context) }
    val isDarkModeState by settingsManager.isDarkMode.collectAsState()
    val isPdfPreviewEnabled by settingsManager.isPdfPreviewEnabled.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val isDarkMode = isDarkModeState ?: isSystemDark

    var notificationsEnabled by remember { mutableStateOf(true) }
    var syncEnabled by remember { mutableStateOf(true) }
    
    var cacheSize by remember { mutableStateOf("Calculating...") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val size = CacheManager.getCacheSizeString(context)
            cacheSize = size
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.PictureAsPdf,
                            contentDescription = "App Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Settings",
                            style = Typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Account Section
            Text(
                text = "ACCOUNT",
                style = Typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Eleanor Vance", style = Typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                        Text("eleanor.vance@example.com", style = Typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Free Plan", style = Typography.labelSmall, color = MaterialTheme.colorScheme.tertiaryContainer)
                    }
                    Button(
                        onClick = { /*TODO*/ },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.WorkspacePremium, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upgrade to Pro", style = Typography.labelLarge)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // General Section
            Text(
                text = "GENERAL",
                style = Typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            ) {
                SettingsItemToggle("Dark Mode", Icons.Filled.DarkMode, isDarkMode) { settingsManager.setDarkMode(it) }
                Divider()
                SettingsItemToggle("PDF Previews", Icons.Filled.PictureAsPdf, isPdfPreviewEnabled) { settingsManager.setPdfPreviewEnabled(it) }
                Divider()
                SettingsItemToggle("Notifications", Icons.Filled.Notifications, notificationsEnabled) { notificationsEnabled = it }
                Divider()
                SettingsItemNav("Default View", Icons.Filled.GridView, "List")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AI Section
            Text(
                text = "AI",
                style = Typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(Screen.AiIntegration.route) }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("AI Integration", style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Storage Section
            Text(
                text = "STORAGE",
                style = Typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            ) {
                SettingsItemNav("Storage Location", Icons.Filled.Folder, "Local Storage")
                Divider()
                SettingsItemValue("Clear Cache", Icons.Filled.DeleteSweep, cacheSize) {
                    coroutineScope.launch(Dispatchers.IO) {
                        CacheManager.clearCache(context)
                        val size = CacheManager.getCacheSizeString(context)
                        cacheSize = size
                    }
                }
                Divider()
                SettingsItemToggleSubtitle("Sync Files", "Keep files updated across devices", Icons.Filled.Sync, syncEnabled) { syncEnabled = it }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About Section
            Text(
                text = "ABOUT",
                style = Typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            ) {
                SettingsItemNav("Help & Support", Icons.Filled.Help, "")
                Divider()
                SettingsItemNav("Privacy Policy", Icons.Filled.Policy, "")
                Divider()
                SettingsItemValue("Version", Icons.Filled.Info, "1.0.2")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsItemToggle(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                uncheckedBorderColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        )
    }
}

@Composable
fun SettingsItemToggleSubtitle(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = Typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                uncheckedBorderColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        )
    }
}

@Composable
fun SettingsItemNav(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value.isNotEmpty()) {
                Text(value, style = Typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
fun SettingsItemValue(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, onClick: (() -> Unit)? = null) {
    val modifier = if (onClick != null) {
        Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp)
    } else {
        Modifier.fillMaxWidth().padding(16.dp)
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
        Text(value, style = Typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun Divider() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    )
}
