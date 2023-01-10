package web.browser.dragon.huawei.utils.appsflyer

interface TrackerInitializationCallback {
    fun onInitializationSuccess()
    fun onInitializationFailed(exception: Exception?)
}
