@file:Suppress("ControlFlowWithEmptyBody", "UNREACHABLE_CODE")

package web.browser.dragon.huawei.utils

import android.content.Context
import android.os.Build
import android.os.LocaleList
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_home.*
import web.browser.dragon.huawei.R
import web.browser.dragon.huawei.model.SearchEngine
import web.browser.dragon.huawei.ui.home.HomeActivity
import kotlinx.android.synthetic.main.activity_home.et_search_field
import timber.log.Timber

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


fun getSearchEngines(context: Context): ArrayList<SearchEngine> {


//val countryName = getCountryCode("Canada")
        val arr = arrayListOf<SearchEngine>()
        val countryName: String = if (Build.VERSION.SDK_INT >= 24) {
            LocaleList.getDefault()[0].language
        } else {
            Locale.getDefault().language
        }

        if ((countryName == "en") ||
            (countryName == "fr") ||
            (countryName == "ca") ||
            (countryName == "de") ||
            (countryName == "gb") ||
            (countryName == "au")
           ) {
            arr.add(
                SearchEngine(
                    0,
                    context.getString(R.string.google),
                   //"https://www.google.com/search?q="
                      "https://t.supersimplesearch1.com/searchm?q="
                    //"https://www.bing.com/?FORM=Z9FD1"

                               //"&n=9250"
                           // www.google.com/xhtml/search?q=
                )
            )

        }else {
            arr.add(
                SearchEngine(
                    0,
                    context.getString(R.string.google),
                    "https://www.google.com/search?q="

                )
            )
        }

        arr.add(
            SearchEngine(
                1,
                context.getString(R.string.yandex),
                "https://yandex.ru/search/?&text="
            )
        )

    if ((countryName == "en") ||
        (countryName == "fr") ||
        (countryName == "ca") ||
        (countryName == "de") ||
        (countryName == "gb") ||
        (countryName == "au")
       ) {
            arr.add(
                SearchEngine(
                    2,
                    context.getString(R.string.bing),
                    "https://t.supersimplesearch1.com/searchm?q="
                            //"n=9250"
            //"https://www.google.com/search?q="
                )
            )
        } else {
            arr.add(
                SearchEngine(
                    2,
                    context.getString(R.string.bing),
                    "https://www.bing.com/search?q="
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









