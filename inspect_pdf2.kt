import androidx.pdf.viewer.fragment.PdfViewerFragment
import java.lang.reflect.Method

fun main() {
    val methods = PdfViewerFragment::class.java.methods
    methods.filter { it.name.contains("Search", ignoreCase = true) }.forEach {
        println("${it.name}(${it.parameterTypes.joinToString { p -> p.simpleName }})")
    }
}
