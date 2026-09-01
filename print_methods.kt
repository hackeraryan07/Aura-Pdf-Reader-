package com.example

import androidx.pdf.viewer.fragment.PdfViewerFragment
import java.lang.reflect.Modifier

fun main() {
    val clazz = PdfViewerFragment::class.java
    println("--- Methods for PdfViewerFragment ---")
    for (method in clazz.declaredMethods) {
        if (Modifier.isPublic(method.modifiers)) {
            println("${method.returnType.simpleName} ${method.name}(${method.parameterTypes.joinToString { it.simpleName }})")
        }
    }
}
