package web.browser.dragon.huawei.utils.other.browser

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PUT
import web.browser.dragon.huawei.model.AffiliatedLinksResponse

interface MonetizeApi {
    @Headers("Content-Type: application/json")
    @PUT("resolve")
    suspend fun getAffiliatedLinksFromApi (
        @Header ("Authorization") key: String,
        @Body body: HashMap<String, List<String>>
    ): Response <AffiliatedLinksResponse>

}