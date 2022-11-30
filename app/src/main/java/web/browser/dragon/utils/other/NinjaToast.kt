package web.browser.dragon.utils.other

import android.content.Context
import android.widget.Toast

object NinjaToast {
    fun show(context: Context, stringResId: Int) {
        Toast.makeText(context, context.getString(stringResId), Toast.LENGTH_SHORT).show()
    }

    fun show(context: Context?, text: String?) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }
}