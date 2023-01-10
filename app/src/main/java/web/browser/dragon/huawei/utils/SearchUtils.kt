package web.browser.dragon.huawei.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import web.browser.dragon.huawei.R
import web.browser.dragon.huawei.model.SearchEngine

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
    val arr = arrayListOf<SearchEngine>()

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

    arr.add(
        SearchEngine(
            2,
            context.getString(R.string.bing),
            "https://www.bing.com/search?q="
        )
    )

    arr.add(
        SearchEngine(
            3,
            context.getString(R.string.duck_duck_go),
            "https://duckduckgo.com/?q="
        )
    )

    return arr
}