package web.browser.dragon.database.browser

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import web.browser.dragon.utils.Constants.CheckUrl.MONETIZE_API_BASE_URL
import web.browser.dragon.utils.Constants.CheckUrl.MONETIZE_API_PUBLIC_KEY
import web.browser.dragon.utils.other.browser.MonetizeApi

class BrowserViewModel : ViewModel() {
    private val _links = MutableLiveData<List<String>>()
    val links: LiveData<List<String>> = _links

    fun getLinks(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val originalLinks = Jsoup.connect(url).get().select("a[href]").map { it.attr("href") }
            val filteredList = originalLinks.filter {
                it.startsWith("http://") || it.startsWith("https://")
            }

            val retrofit = Retrofit.Builder()
                .baseUrl(MONETIZE_API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val api: MonetizeApi = retrofit.create(MonetizeApi::class.java)
            val map = hashMapOf("iris" to filteredList)
            val result = api.getAffiliatedLinksFromApi(
                key = "Bearer $MONETIZE_API_PUBLIC_KEY",
                body = map
            )
            if (result.isSuccessful) {
                _links.postValue((result.body()?.data?.map { it.deeplink } ?: mutableListOf()))
            }
        }
    }
}