import androidx.pdf.viewer.fragment.PdfViewerFragment
import java.lang.reflect.Modifier

val clazz = PdfViewerFragment::class.java
for (method in clazz.declaredMethods) {
    if (Modifier.isPublic(method.modifiers)) {
        println("${method.returnType.simpleName} ${method.name}(${method.parameterTypes.joinToString { it.simpleName }})")
    }
}
