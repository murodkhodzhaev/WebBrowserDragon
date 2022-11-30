package web.browser.dragon.utils

import android.content.Context

fun isFirstLaunch(context: Context): Boolean {
    return context.getSharedPreferences(Constants.App.APP, Context.MODE_PRIVATE)
        .getBoolean(Constants.App.APP_IS_FIRST_LAUNCH, true)
}

fun setIsFirstLaunch(context: Context, isFirstLaunch: Boolean) {
    context.getSharedPreferences(Constants.App.APP, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(Constants.App.APP_IS_FIRST_LAUNCH, isFirstLaunch)
        .apply()
}

fun isIncognitoMode(context: Context): Boolean {
    return context.getSharedPreferences(Constants.App.APP, Context.MODE_PRIVATE)
        .getBoolean(Constants.App.IS_INCOGNITO_MODE, false)
}

fun setIsIncognitoMode(context: Context, isIncognitoMode: Boolean) {
    context.getSharedPreferences(Constants.App.APP, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(Constants.App.IS_INCOGNITO_MODE, isIncognitoMode)
        .apply()
}

fun onCheckTheme(context: Context): Boolean {
    return context.getSharedPreferences(Constants.App.APP, Context.MODE_PRIVATE)
        .getBoolean(Constants.App.DARK_MODE, false)
}

fun setOnCheckTheme(context: Context, darkMode: Boolean) {
    context.getSharedPreferences(Constants.App.APP, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(Constants.App.DARK_MODE, darkMode)
        .apply()
}

fun isRequestDefaultBrowser(context: Context): Boolean {
    return context.getSharedPreferences(Constants.App.APP, Context.MODE_PRIVATE)
        .getBoolean(Constants.App.IS_REQUEST_DEFAULT_BROWSER, false)
}

fun setRequestDefaultBrowser(context: Context, isRequestDefaultBrowser: Boolean) {
    context.getSharedPreferences(Constants.App.APP, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(Constants.App.IS_REQUEST_DEFAULT_BROWSER, isRequestDefaultBrowser)
        .apply()
}

fun isFirstRequestPermissions(context: Context): Boolean {
    return context.getSharedPreferences(Constants.App.APP, Context.MODE_PRIVATE)
        .getBoolean(Constants.App.IS_FIRST_REQUEST_PERMISSIONS, true)
}

fun setFirstRequestPermissions(context: Context, isFirstRequestPermissions: Boolean) {
    context.getSharedPreferences(Constants.App.APP, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(Constants.App.IS_FIRST_REQUEST_PERMISSIONS, isFirstRequestPermissions)
        .apply()
}