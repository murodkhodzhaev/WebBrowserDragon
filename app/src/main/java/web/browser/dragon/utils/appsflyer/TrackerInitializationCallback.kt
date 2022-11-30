package web.browser.dragon.utils.appsflyer

interface TrackerInitializationCallback {
    fun onInitializationSuccess()
    fun onInitializationFailed(exception: Exception?)
}
