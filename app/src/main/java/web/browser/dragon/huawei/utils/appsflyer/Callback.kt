package web.browser.dragon.huawei.utils.appsflyer

interface Callback<T> {
    fun onSuccess(result: T)
    fun onFailure(errorCode: Int, errorText: String?)
}