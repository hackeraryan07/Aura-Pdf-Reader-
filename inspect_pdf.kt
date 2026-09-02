import androidx.pdf.viewer.fragment.PdfViewerFragment

fun inspect() {
    val methods = PdfViewerFragment::class.java.methods
    methods.forEach { println(it.name) }
}
