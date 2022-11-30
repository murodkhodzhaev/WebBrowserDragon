package web.browser.dragon.utils.ogparser

import web.browser.dragon.model.OpenGraphResult

interface OpenGraphCallback {
    fun onPostResponse(openGraphResult: OpenGraphResult)
    fun onError(error: String)
}