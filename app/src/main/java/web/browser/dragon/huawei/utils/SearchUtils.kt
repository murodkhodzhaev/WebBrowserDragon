@file:Suppress("ControlFlowWithEmptyBody", "UNREACHABLE_CODE")

package web.browser.dragon.huawei.utils

import android.content.Context
import android.os.Build
import android.os.LocaleList
import com.google.gson.Gson
import web.browser.dragon.huawei.R
import web.browser.dragon.huawei.model.SearchEngine
import java.util.*


fun saveSelectedSearchEngine(context: Context, searchEngine: SearchEngine?) {
    context.getSharedPreferences(Constants.Search.SEARCH, Context.MODE_PRIVATE)
        .edit()
        .putString(Constants.Search.SEARCH_SELECTED, Gson().toJson(searchEngine))
        .commit()
}

fun getSelectedSearchEngine(context: Context?): SearchEngine? {
    val json = context
        ?.getSharedPreferences(Constants.Search.SEARCH, Context.MODE_PRIVATE)
        ?.getString(Constants.Search.SEARCH_SELECTED, null)
    return if(!json.isNullOrEmpty()) {
        json.let { Gson().fromJson(json, SearchEngine::class.java) }
    }
    else {
        null
    }
}



//        }else ((country == "US") || (country == "FR") || (country == "DE") || (country == "GB") || (country == "CA")){
//            fun getSearchEngines(context: android.content.Context): ArrayList<SearchEngine2> {
//                val arr = kotlin.collections.arrayListOf<web.browser.dragon.huawei.model.SearchEngine2>()
//                arr.add(
//                    web.browser.dragon.huawei.model.SearchEngine(
//                        0,
//                        context.getString(web.browser.dragon.huawei.R.string.google),
//                        "https://www.google.com/search?q="
//                    )
//                )
//
//                arr.add(
//                    web.browser.dragon.huawei.model.SearchEngine(
//                        1,
//                        context.getString(web.browser.dragon.huawei.R.string.yandex),
//                        "https://yandex.ru/search/?&text="
//                    )
//                )
//
//                arr.add(
//                    web.browser.dragon.huawei.model.SearchEngine(
//                        2,
//                        context.getString(web.browser.dragon.huawei.R.string.bing),
//                        //     if ( = )
//                        //"https://www.bing.com/search?q="
//                        "https://t.supersimplesearch1.com/searchm?q="
//                        //
//                    )
//                )
//
//                arr.add(
//                    web.browser.dragon.huawei.model.SearchEngine(
//                        3,
//                        context.getString(web.browser.dragon.huawei.R.string.duck_duck_go),
//                        "https://duckduckgo.com/?q="
//                    )
//                )
//
//                return arr
//
//
//        }
//
//        }
//
//



//fun saveSearchEngines(context: Context, arr: ArrayList<SearchEngine>) {
//    val sharedPref = context.getSharedPreferences(Constants.Search.SEARCH, Context.MODE_PRIVATE) ?: return
//
//    if(arr.find { it.id == getSelectedSearchEngine(context)?.id } != null) {
//        saveSelectedSearchEngine(context, arr.find { it.id == getSelectedSearchEngine(context)?.id })
//    }
//
//    with(sharedPref.edit()) {
//        putString(Constants.Search.SEARCH_ENGINES, Gson().toJson(arr))
//        commit()
//    }
//}
//
//fun getSearchEngines(context: Context): ArrayList<SearchEngine> {
//    val sharedPref = context.getSharedPreferences(Constants.Search.SEARCH, Context.MODE_PRIVATE)
//    val objectJson = sharedPref.getString(Constants.Search.SEARCH_ENGINES, "")
//    val objectType = object : TypeToken<ArrayList<SearchEngine>>() {}.type
//    return Gson().fromJson(objectJson, objectType) ?: arrayListOf()
//}

//
//var tm = this.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
//var countryCodeValue = tm.networkCountryIso
//fun getCountryCode(countryName:String) = Locale.getISOCountries().find {
//       Locale("", it).displayCountry == countryName
//}

//
//fun getUserCountry(context: Context): String? {
//    try {
//        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//        val simCountry = tm.simCountryIso
//        if (simCountry != null && simCountry.length == 2) { // SIM country code is available
//            return simCountry.lowercase(Locale.US)
//        } else if (tm.phoneType != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
//            val networkCountry = tm.networkCountryIso
//            if (networkCountry != null && networkCountry.length == 2) { // network country code is available
//                return networkCountry.lowercase(Locale.US)
//            }
//        }
//    } catch (_: Exception) {
//    }
//    return null
//}


//fun getCountryCode(countryName: String) =
//    Locale.getISOCountries().find { Locale("", it).displayCountry == countryName }

    fun getSearchEngines(context: Context): ArrayList<SearchEngine> {

//val countryName = getCountryCode("Canada")
        val arr = arrayListOf<SearchEngine>()
        val countryName: String = if (Build.VERSION.SDK_INT >= 24) {
            LocaleList.getDefault()[0].language
        } else {
            Locale.getDefault().language
        }
        arr.add(
            SearchEngine(
                0,
                context.getString(R.string.google),
                "https://www.google.com/search?q="
            )
        )

        arr.add(
            SearchEngine(
                1,
                context.getString(R.string.yandex),
                "https://yandex.ru/search/?&text="
            )
        )


    if ((countryName == "en") ||
        (countryName == "fr") ||
        (countryName== "ca") ||
        (countryName == "de") ||
        (countryName == "gb") ||
        (countryName == "au")
     //   (locale_def == "ru")


    )
        {
            arr.add(
                SearchEngine(
                    2,
                    context.getString(R.string.bing),
                       "https://t.supersimplesearch1.com/searchm?q="
//            "https://www.google.com/search?q="
                           //     "https://www.twitch.tv/"


                )

            )

        } else {
            arr.add(
                SearchEngine(
                    2,
                    context.getString(R.string.bing),
                    "https://www.bing.com/search?q="
                    //"https://account.takeads.com/products/monetize-api"
                    //"https://www.youtube.com"


                )
            )
        }


        arr.add(
            SearchEngine(
                3,
                context.getString(R.string.duck_duck_go),
                "https://duckduckgo.com/?q="
            )
        )
        return arr
    }








