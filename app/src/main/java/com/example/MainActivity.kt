package com.example

import android.os.Bundle
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.model.SettingsManager
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.AppTheme

class MainActivity : FragmentActivity() {

    private var lastUpTime = 0L
    private var lastUpX = 0f
    private var lastUpY = 0f
    
    private var isPotentialDoubleTap = false
    private var isZooming = false
    private var doubleTapTimeout = 300L
    private var touchSlop = 16f
    
    private var initialZoomY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val viewConfig = ViewConfiguration.get(this)
        doubleTapTimeout = ViewConfiguration.getDoubleTapTimeout().toLong()
        touchSlop = viewConfig.scaledTouchSlop.toFloat()
        
        enableEdgeToEdge()
        val settingsManager = SettingsManager.getInstance(this)
        setContent {
            val isDarkSystem = isSystemInDarkTheme()
            val isDarkMode by settingsManager.isDarkMode.collectAsState()
            
            AppTheme(darkTheme = isDarkMode ?: isDarkSystem) {
                MainAppScreen()
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE) {
            val action = ev.actionMasked
            
            // Convert to FINGER so PDF viewer pans instead of always selecting text.
            // Text selection will still work via long-press (like touch).
            val properties = arrayOfNulls<MotionEvent.PointerProperties>(1)
            val coords = arrayOfNulls<MotionEvent.PointerCoords>(1)
            properties[0] = MotionEvent.PointerProperties()
            ev.getPointerProperties(0, properties[0])
            properties[0]?.toolType = MotionEvent.TOOL_TYPE_FINGER

            coords[0] = MotionEvent.PointerCoords()
            ev.getPointerCoords(0, coords[0])
            
            val fingerEvent = MotionEvent.obtain(
                ev.downTime, ev.eventTime, ev.action, 1,
                properties.requireNoNulls(), coords.requireNoNulls(),
                ev.metaState, ev.buttonState, ev.xPrecision, ev.yPrecision,
                ev.deviceId, ev.edgeFlags, ev.source, ev.flags
            )

            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    isZooming = false
                    val timeSinceLastUp = ev.eventTime - lastUpTime
                    val dx = ev.x - lastUpX
                    val dy = ev.y - lastUpY
                    if (timeSinceLastUp < doubleTapTimeout && (dx * dx + dy * dy) < touchSlop * touchSlop * 10) {
                        isPotentialDoubleTap = true
                        initialZoomY = ev.y
                    } else {
                        isPotentialDoubleTap = false
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isPotentialDoubleTap && !isZooming) {
                        val dy = ev.y - initialZoomY
                        if (Math.abs(dy) > touchSlop) {
                            isZooming = true
                            // Start fake pinch
                            dispatchFakePinch(fingerEvent, MotionEvent.ACTION_POINTER_DOWN or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT))
                            fingerEvent.recycle()
                            return true
                        }
                    }
                    if (isZooming) {
                        dispatchFakePinch(fingerEvent, MotionEvent.ACTION_MOVE)
                        fingerEvent.recycle()
                        return true
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (action == MotionEvent.ACTION_UP) {
                        lastUpTime = ev.eventTime
                        lastUpX = ev.x
                        lastUpY = ev.y
                    }
                    
                    if (isZooming) {
                        dispatchFakePinch(fingerEvent, MotionEvent.ACTION_POINTER_UP or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT))
                        val upResult = super.dispatchTouchEvent(fingerEvent)
                        fingerEvent.recycle()
                        isZooming = false
                        isPotentialDoubleTap = false
                        return upResult
                    }
                    isPotentialDoubleTap = false
                }
            }
            
            val result = super.dispatchTouchEvent(fingerEvent)
            fingerEvent.recycle()
            return result
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun dispatchFakePinch(ev: MotionEvent, action: Int) {
        val properties = arrayOfNulls<MotionEvent.PointerProperties>(2)
        val coords = arrayOfNulls<MotionEvent.PointerCoords>(2)
        
        properties[0] = MotionEvent.PointerProperties().apply {
            id = 0
            toolType = MotionEvent.TOOL_TYPE_FINGER
        }
        coords[0] = MotionEvent.PointerCoords().apply {
            x = lastUpX
            y = initialZoomY
            pressure = 1.0f
            size = 1.0f
        }
        
        properties[1] = MotionEvent.PointerProperties().apply {
            id = 1
            toolType = MotionEvent.TOOL_TYPE_FINGER
        }
        val dy = ev.y - initialZoomY
        // Moving up (negative dy) = zoom in (distance increases).
        // Moving down (positive dy) = zoom out (distance decreases).
        val startDistance = 400f
        val distance = Math.max(50f, startDistance - dy * 2.0f)
        
        coords[1] = MotionEvent.PointerCoords().apply {
            x = lastUpX
            y = initialZoomY + distance
            pressure = 1.0f
            size = 1.0f
        }
        
        val pinchEvent = MotionEvent.obtain(
            ev.downTime, ev.eventTime, action, 2,
            properties.requireNoNulls(), coords.requireNoNulls(),
            ev.metaState, ev.buttonState, ev.xPrecision, ev.yPrecision,
            ev.deviceId, ev.edgeFlags, ev.source, ev.flags
        )
        super.dispatchTouchEvent(pinchEvent)
        pinchEvent.recycle()
    }
}
