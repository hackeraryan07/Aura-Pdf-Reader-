package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.model.SettingsManager
import com.example.ui.theme.Typography
import com.example.api.AiApiService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiIntegrationScreen(navController: NavController) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val aiProvider by settingsManager.aiProvider.collectAsState()
    val aiApiKey by settingsManager.aiApiKey.collectAsState()
    val aiModel by settingsManager.aiModel.collectAsState()

    var showProviderSheet by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showModelSheet by remember { mutableStateOf(false) }
    
    var availableModels by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isLoadingModels by remember { mutableStateOf(false) }
    var isTestingApi by remember { mutableStateOf(false) }
    var testApiResult by remember { mutableStateOf<String?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("AI Integration", style = Typography.titleLarge.copy(fontWeight = FontWeight.Normal))
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Provider",
                style = Typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f))
            ) {
                // Provider Item
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showProviderSheet = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("AI provider", style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text("Choose the AI service.", style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (aiProvider != "None (Disabled)") {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(aiProvider, style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), thickness = 1.dp)

                // API Key Item
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showApiKeyDialog = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Key,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("API key", style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            if (aiApiKey.isNotEmpty()) "Configured" else "Not configured",
                            style = Typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), thickness = 1.dp)

                // Model Item
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showModelSheet = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Model", style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        if (aiModel.isNotEmpty()) {
                            Text(aiModel, style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Text("Tap refresh to load available models", style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (isLoadingModels) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(
                            onClick = { 
                                if (aiProvider == "Custom" || aiProvider.startsWith("None")) {
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Please select Gemini or OpenAI to fetch models.") }
                                    return@IconButton
                                }
                                if (aiApiKey.isBlank()) {
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Please enter an API key first.") }
                                    return@IconButton
                                }
                                isLoadingModels = true
                                coroutineScope.launch {
                                    val result = AiApiService.fetchModels(aiProvider, aiApiKey)
                                    isLoadingModels = false
                                    result.fold(
                                        onSuccess = { models ->
                                            availableModels = models
                                            if (models.isNotEmpty()) {
                                                showModelSheet = true
                                            } else {
                                                snackbarHostState.showSnackbar("No models found.")
                                            }
                                        },
                                        onFailure = { err ->
                                            snackbarHostState.showSnackbar("Failed: ${err.message}")
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp))
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), thickness = 1.dp)

                // Test API Item
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (aiProvider == "Custom" || aiProvider.startsWith("None")) {
                                coroutineScope.launch { snackbarHostState.showSnackbar("Please select Gemini or OpenAI to test.") }
                                return@clickable
                            }
                            if (aiApiKey.isBlank()) {
                                coroutineScope.launch { snackbarHostState.showSnackbar("Please enter an API key first.") }
                                return@clickable
                            }
                            isTestingApi = true
                            testApiResult = "Testing..."
                            coroutineScope.launch {
                                val result = AiApiService.testApi(aiProvider, aiApiKey)
                                isTestingApi = false
                                result.fold(
                                    onSuccess = { msg ->
                                        testApiResult = "Success"
                                        snackbarHostState.showSnackbar("API Test Successful!")
                                    },
                                    onFailure = { err ->
                                        testApiResult = "Failed"
                                        snackbarHostState.showSnackbar("Test failed: ${err.message}")
                                    }
                                )
                            }
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isTestingApi) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = if (testApiResult == "Success") Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Test API", style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            testApiResult ?: if (aiApiKey.isNotEmpty()) "API ready" else "Not ready",
                            style = Typography.bodyMedium,
                            color = if (testApiResult == "Success") Color(0xFF4CAF50) else if (testApiResult == "Failed") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    if (showProviderSheet) {
        ModalBottomSheet(
            onDismissRequest = { showProviderSheet = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    "AI provider",
                    style = Typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                val providers = listOf("Gemini", "OpenAI", "Custom", "None (Disabled)")
                providers.forEach { provider ->
                    val isSelected = provider == aiProvider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer)
                            .clickable {
                                settingsManager.setAiProvider(provider)
                                showProviderSheet = false
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            provider,
                            style = Typography.bodyLarge.copy(fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                        if (isSelected) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }

    if (showApiKeyDialog) {
        Dialog(onDismissRequest = { showApiKeyDialog = false }) {
            var tempKey by remember { mutableStateOf(aiApiKey) }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.Key,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        "API key",
                        style = Typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    OutlinedTextField(
                        value = tempKey,
                        onValueChange = { tempKey = it },
                        label = { Text("API key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showApiKeyDialog = false }) {
                            Text("Cancel", color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = {
                            settingsManager.setAiApiKey(tempKey)
                            showApiKeyDialog = false
                        }) {
                            Text("Save", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    if (showModelSheet) {
        ModalBottomSheet(
            onDismissRequest = { showModelSheet = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxHeight(0.8f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    "Model",
                    style = Typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                var searchQuery by remember { mutableStateOf("") }
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (aiProvider == "Custom") "Enter custom model ID" else "Search models") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )
                
                if (aiProvider == "Custom" && searchQuery.isNotBlank()) {
                    // Allow adding custom model directly
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .clickable {
                                settingsManager.setAiModel(searchQuery)
                                showModelSheet = false
                            }
                            .padding(16.dp)
                    ) {
                        Text(
                            "Use custom model: $searchQuery",
                            style = Typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                val filteredModels = availableModels.filter { it.first.contains(searchQuery, ignoreCase = true) || it.second.contains(searchQuery, ignoreCase = true) }
                
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (filteredModels.isEmpty() && aiProvider != "Custom") {
                        item {
                            Text(
                                "No models available. Try fetching them.",
                                style = Typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    items(filteredModels) { (displayName, modelId) ->
                        val isSelected = modelId == aiModel
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer)
                                .clickable {
                                    settingsManager.setAiModel(modelId)
                                    showModelSheet = false
                                }
                                .padding(16.dp)
                        ) {
                            Text(
                                displayName,
                                style = Typography.bodyLarge,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                modelId,
                                style = Typography.bodyMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
