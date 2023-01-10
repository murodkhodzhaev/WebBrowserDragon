package web.browser.dragon.huawei.utils.other

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment


fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}