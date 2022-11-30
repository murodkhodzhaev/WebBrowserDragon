package web.browser.dragon.utils.settings

import android.content.Context
import com.google.gson.Gson
import web.browser.dragon.model.Settings
import web.browser.dragon.utils.Constants

fun saveSettings(context: Context, settings: Settings?) {
    context.getSharedPreferences(Constants.Settings.SETTINGS, Context.MODE_PRIVATE)
        .edit()
        .putString(Constants.Settings.SETTINGS_PREFS, Gson().toJson(settings))
        .commit()
}

fun getSettings(context: Context?): Settings? {
    val json = context
        ?.getSharedPreferences(Constants.Settings.SETTINGS, Context.MODE_PRIVATE)
        ?.getString(Constants.Settings.SETTINGS_PREFS, null)
    return if(!json.isNullOrEmpty()) {
        json.let { Gson().fromJson(json, Settings::class.java) }
    }
    else {
        null
    }
}