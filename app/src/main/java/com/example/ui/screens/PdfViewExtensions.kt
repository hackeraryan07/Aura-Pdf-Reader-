package com.example.ui.screens

import android.view.View
import android.view.ViewGroup
import androidx.pdf.viewer.fragment.PdfViewerFragment

fun PdfViewerFragment.setPanMode(isPanMode: Boolean) {
    val root = this.view ?: return
    
    fun updateView(v: View) {
        if (isPanMode) {
            // Disable text selection (usually triggered by long click)
            v.isLongClickable = false
        } else {
            v.isLongClickable = true
        }
        if (v is ViewGroup) {
            for (i in 0 until v.childCount) {
                updateView(v.getChildAt(i))
            }
        }
    }
    
    updateView(root)
}
