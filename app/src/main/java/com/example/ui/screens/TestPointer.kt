package com.example.ui.screens

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import kotlinx.coroutines.coroutineScope

suspend fun PointerInputScope.handlePdfTouch(isPanModeActive: Boolean) {
    if (isPanModeActive) {
        awaitPointerEventScope {
            while (true) {
                val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                var isPan = false
                while (true) {
                    val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                    val change = event.changes.firstOrNull { it.id == down.id }
                    if (change == null || !change.pressed) break
                    
                    val distance = (change.position - down.position).getDistance()
                    if (distance > viewConfiguration.touchSlop) {
                        isPan = true
                    }
                    
                    val timeElapsed = change.uptimeMillis - down.uptimeMillis
                    // If it hasn't moved and we are close to long press timeout, consume it!
                    if (!isPan && timeElapsed >= viewConfiguration.longPressTimeoutMillis - 20) {
                        change.consume()
                    }
                }
            }
        }
    }
}
