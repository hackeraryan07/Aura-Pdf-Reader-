import android.view.View
import android.view.ViewGroup
fun dumpViews(v: View, prefix: String = "") {
    println("$prefix${v.javaClass.name}")
    if (v is ViewGroup) {
        for (i in 0 until v.childCount) {
            dumpViews(v.getChildAt(i), "$prefix  ")
        }
    }
}
