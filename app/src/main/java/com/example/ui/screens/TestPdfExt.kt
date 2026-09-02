package com.example.ui.screens

import android.view.View
import android.view.ViewGroup
import androidx.pdf.viewer.fragment.PdfViewerFragment

fun PdfViewerFragment.applyMode(isPanModeActive: Boolean) {
    val root = this.view ?: return
    
    fun updateView(v: View) {
        if (isPanModeActive) {
            // Disable text selection (often triggered by long click or custom detector)
            v.isLongClickable = false
            // Maybe try setting a long click listener that consumes the event?
            // v.setOnLongClickListener { true }
        } else {
            v.isLongClickable = true
            // v.setOnLongClickListener(null)
        }
        if (v is ViewGroup) {
            for (i in 0 until v.childCount) {
                updateView(v.getChildAt(i))
            }
        }
    }
    updateView(root)
}
