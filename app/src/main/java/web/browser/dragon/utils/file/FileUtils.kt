package web.browser.dragon.utils.file

import android.webkit.MimeTypeMap
import java.text.DecimalFormat

fun getMimeType(url: String?): String? {
    var type: String? = null
    val extension = MimeTypeMap.getFileExtensionFromUrl(url)
    if (extension != null) {
        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
    return type
}

fun getStringSizeLengthFile(size: Long): String {
    val df = DecimalFormat("0.00")
    val sizeKb = 1024.0f
    val sizeMb = sizeKb * sizeKb
    val sizeGb = sizeMb * sizeKb
    val sizeTerra = sizeGb * sizeKb
    if (size < sizeMb) return df.format(size / sizeKb)
        .toString() + " Kb" else if (size < sizeGb) return df.format(size / sizeMb)
        .toString() + " Mb" else if (size < sizeTerra) return df.format(size / sizeGb)
        .toString() + " Gb"
    return ""
}