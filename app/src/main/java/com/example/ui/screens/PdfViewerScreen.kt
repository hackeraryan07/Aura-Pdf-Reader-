package com.example.ui.screens

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.compose.AndroidFragment
import androidx.pdf.viewer.fragment.PdfViewerFragment
import com.example.api.AiApiService
import com.example.model.SettingsManager
import com.example.ui.components.MarkdownText
import kotlinx.coroutines.launch

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    navController: androidx.navigation.NavController,
    uriString: String
) {
    var isImmersiveMode by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var isPanModeActive by remember { mutableStateOf(true) }
    var pdfFragment by remember { mutableStateOf<PdfViewerFragment?>(null) }
    
    // AI States
    var isAiModeActive by remember { mutableStateOf(false) }
    var showAiSheet by remember { mutableStateOf(false) }
    var capturedText by remember { mutableStateOf("") }
    var aiResponse by remember { mutableStateOf("") }
    var aiLoading by remember { mutableStateOf(false) }
    var customPrompt by remember { mutableStateOf("") }
    
    val view = LocalView.current
    val context = LocalContext.current
    val viewConfiguration = LocalViewConfiguration.current
    val coroutineScope = rememberCoroutineScope()
    var tapJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    
    val settingsManager = remember { SettingsManager.getInstance(context) }
    val aiProvider by settingsManager.aiProvider.collectAsState(initial = "None (Disabled)")
    val aiApiKey by settingsManager.aiApiKey.collectAsState(initial = "")
    val aiModel by settingsManager.aiModel.collectAsState(initial = "")

    val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    
    DisposableEffect(isAiModeActive) {
        val listener = ClipboardManager.OnPrimaryClipChangedListener {
            if (isAiModeActive) {
                val clip = clipboardManager.primaryClip
                if (clip != null && clip.itemCount > 0) {
                    val text = clip.getItemAt(0).text?.toString()
                    if (!text.isNullOrBlank()) {
                        capturedText = text
                        showAiSheet = true
                        aiResponse = ""
                        customPrompt = ""
                    }
                }
            }
        }
        if (isAiModeActive) {
            clipboardManager.addPrimaryClipChangedListener(listener)
        }
        onDispose {
            clipboardManager.removePrimaryClipChangedListener(listener)
        }
    }
    
    DisposableEffect(isImmersiveMode) {
        val window = context.findActivity()?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, view)
            if (isImmersiveMode) {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            val exitWindow = context.findActivity()?.window
            if (exitWindow != null) {
                val insetsController = WindowCompat.getInsetsController(exitWindow, view)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val topBarHeight = 64.dp
        val statusBarsPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val topPadding = if (isImmersiveMode) 0.dp else topBarHeight + statusBarsPadding

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPadding)
                .navigationBarsPadding()
                .imePadding()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .pointerHoverIcon(if (isPanModeActive) PointerIcon.Hand else PointerIcon.Text)
                .pointerInput(Unit) {
                    val doubleTapTimeout = viewConfiguration.doubleTapTimeoutMillis
                    val longPressTimeout = viewConfiguration.longPressTimeoutMillis
                    var lastTapTime = 0L
                    
                    awaitPointerEventScope {
                        while (true) {
                            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                            var isTap = true
                            var upEvent: androidx.compose.ui.input.pointer.PointerInputChange? = null
                            
                            while (true) {
                                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                                if (event.changes.size > 1) {
                                    isTap = false
                                }
                                val change = event.changes.firstOrNull { it.id == down.id }
                                if (change != null) {
                                    if ((change.position - down.position).getDistance() > viewConfiguration.touchSlop) {
                                        isTap = false
                                    }
                                    if (!change.pressed) {
                                        upEvent = change
                                        break
                                    }
                                } else {
                                    isTap = false
                                    break
                                }
                            }
                            
                            if (isTap && upEvent != null) {
                                val up = upEvent!!
                                if ((up.uptimeMillis - down.uptimeMillis) < longPressTimeout) {
                                    val timeSinceLastTap = up.uptimeMillis - lastTapTime
                                    if (timeSinceLastTap < doubleTapTimeout) {
                                        tapJob?.cancel()
                                        tapJob = null
                                        lastTapTime = 0L
                                    } else {
                                        lastTapTime = up.uptimeMillis
                                        tapJob?.cancel()
                                        tapJob = coroutineScope.launch {
                                            kotlinx.coroutines.delay(doubleTapTimeout)
                                            isImmersiveMode = !isImmersiveMode
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        ) {
            AndroidFragment<PdfViewerFragment>(
                modifier = Modifier.fillMaxSize(),
                onUpdate = { fragment ->
                    if (pdfFragment != fragment) {
                        pdfFragment = fragment
                    }
                    if (fragment.documentUri?.toString() != uriString) {
                        fragment.documentUri = Uri.parse(uriString)
                    }
                }
            )
        }
        
        AnimatedVisibility(
            visible = !isImmersiveMode,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            TopAppBar(
                title = { 
                    Text(
                        "PDF Viewer", 
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconToggleButton(
                        checked = isPanModeActive,
                        onCheckedChange = { isPanModeActive = it },
                        modifier = Modifier.background(
                            if (isPanModeActive) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                    ) {
                        Icon(
                            Icons.Default.PanTool, 
                            contentDescription = "Pan/Hand Mode",
                            tint = if (isPanModeActive) MaterialTheme.colorScheme.onPrimaryContainer else LocalContentColor.current
                        )
                    }
                    IconButton(onClick = { 
                        if (aiProvider.contains("Disabled")) {
                            Toast.makeText(context, "Please configure AI API in Settings first.", Toast.LENGTH_LONG).show()
                            return@IconButton
                        }
                        isAiModeActive = !isAiModeActive 
                        if (isAiModeActive) {
                            Toast.makeText(context, "AI Mode Active: Select text & copy it to ask AI.", Toast.LENGTH_LONG).show()
                        }
                    }) {
                        Icon(
                            Icons.Default.AutoAwesome, 
                            contentDescription = "AI Assistant",
                            tint = if (isAiModeActive) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                    IconButton(onClick = { 
                        isSearchActive = !isSearchActive 
                        pdfFragment?.let { frag ->
                            if (frag.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) {
                                frag.isTextSearchActive = isSearchActive
                            }
                        }
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search in PDF")
                    }
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmark")
                    }
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        }
        
        if (showAiSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAiSheet = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .padding(bottom = 24.dp)
                    ) {
                        Text(
                        "AI Assistant",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Text("Selected Text:", style = MaterialTheme.typography.labelMedium)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 16.dp)
                    ) {
                        Text(
                            text = capturedText,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        val actions = listOf(
                            "Explain" to "Explain the following text clearly: \n\n\"$capturedText\"",
                            "Summarize" to "Summarize the following text briefly: \n\n\"$capturedText\"",
                            "Translate" to "Translate the following text into English (or a highly spoken language if already English): \n\n\"$capturedText\""
                        )
                        actions.forEach { (label, prompt) ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    aiResponse = ""
                                    aiLoading = true
                                    coroutineScope.launch {
                                        val result = AiApiService.generateContent(aiProvider, aiApiKey, aiModel, prompt)
                                        aiResponse = result.getOrElse { "Error: ${it.message}" }
                                        aiLoading = false
                                    }
                                },
                                label = { Text(label) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = customPrompt,
                        onValueChange = { customPrompt = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ask a custom question...") },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (customPrompt.isNotBlank()) {
                                        val prompt = "$customPrompt\n\nContext text: \"$capturedText\""
                                        aiResponse = ""
                                        aiLoading = true
                                        coroutineScope.launch {
                                            val result = AiApiService.generateContent(aiProvider, aiApiKey, aiModel, prompt)
                                            aiResponse = result.getOrElse { "Error: ${it.message}" }
                                            aiLoading = false
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                            }
                        },
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    if (aiLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else if (aiResponse.isNotBlank()) {
                        SelectionContainer {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                MarkdownText(
                                    text = aiResponse,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                    }
                }
            }
        }
    }
}
