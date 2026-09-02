package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
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
    
    val view = LocalView.current
    val context = LocalContext.current
    val viewConfiguration = LocalViewConfiguration.current
    
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
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .pointerInput(Unit) {
                    val doubleTapTimeout = viewConfiguration.doubleTapTimeoutMillis
                    val longPressTimeout = viewConfiguration.longPressTimeoutMillis
                    
                    while (true) {
                        val tapEvent = awaitPointerEventScope {
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
                                if (upEvent.uptimeMillis - down.uptimeMillis < longPressTimeout) {
                                    upEvent
                                } else null
                            } else null
                        }
                        
                        if (tapEvent != null) {
                            val secondTap = kotlinx.coroutines.withTimeoutOrNull(doubleTapTimeout) {
                                awaitPointerEventScope {
                                    awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                                }
                            }
                            
                            if (secondTap == null) {
                                isImmersiveMode = !isImmersiveMode
                            }
                        }
                    }
                }
        ) {
            AndroidFragment<PdfViewerFragment>(
                modifier = Modifier.fillMaxSize(),
                onUpdate = { fragment ->
                    fragment.documentUri = Uri.parse(uriString)
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
                    IconButton(onClick = { /* TODO */ }) {
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
    }
}
