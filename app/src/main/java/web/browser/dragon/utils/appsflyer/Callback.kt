package web.browser.dragon.utils.appsflyer

interface Callback<T> {
    fun onSuccess(result: T)
    fun onFailure(errorCode: Int, errorText: String?)
}