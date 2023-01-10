package web.browser.dragon.huawei.utils.ogparser

import web.browser.dragon.huawei.model.OpenGraphResult

interface OpenGraphCallback {
    fun onPostResponse(openGraphResult: OpenGraphResult)
    fun onError(error: String)
}