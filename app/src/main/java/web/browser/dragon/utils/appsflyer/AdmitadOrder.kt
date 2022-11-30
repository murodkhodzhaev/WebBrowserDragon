package web.browser.dragon.utils.appsflyer

import android.text.TextUtils
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import ru.tachos.admitadstatisticsdk.AdmitadEvent

class AdmitadOrder private constructor(private val params: Map<String, String>) {
    fun toEvent(@AdmitadEvent.Type type: Int): AdmitadEvent {
        return AdmitadEvent(type, params)
    }

    class Item(@Nullable id: String?, @NonNull name: String?, quantity: Int) {
        val jsonObject = JSONObject()


        companion object {
            private const val FIELD_ITEM_ID = "id"
            private const val FIELD_ITEM_NAME = "name"
            private const val FIELD_ITEM_QUANTITY = "quantity"
        }

        init {
            try {
                if (!TextUtils.isEmpty(id)) {
                    jsonObject.put(FIELD_ITEM_ID, id)
                }
                if (!TextUtils.isEmpty(name)) {
                    jsonObject.put(FIELD_ITEM_NAME, name)
                }
                try {
                    jsonObject.put(FIELD_ITEM_QUANTITY, quantity)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            } catch (e: JSONException) {
            }
        }
    }

    class UserInfo {
        var jsonObject: JSONObject
        constructor() {
            jsonObject = JSONObject()
            Log.d("test", "json $jsonObject")
        }

        constructor(@NonNull params: Map<String, String?>?) {
            jsonObject = JSONObject(params)
        }

        fun putExtra(@NonNull key: String?, value: String?): UserInfo {
            try {
                if (key != null) {
                    jsonObject.put(key, value)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return this
        }
    }

    class Builder(@NonNull id: String, @NonNull totalPrice: String) {
        private val mainParams: MutableMap<String, String> = HashMap()
        private val items: MutableList<Item>? = ArrayList()
        private var userInfo: UserInfo? = null
        fun setCurrencyCode(@NonNull currencyCode: String): Builder {
            mainParams[FIELD_CURRENCY_CODE] = currencyCode
            return this
        }

        fun setUserInfo(@NonNull userInfo: UserInfo?): Builder {
            this.userInfo = userInfo
            return this
        }

        fun putItem(@NonNull item: Item): Builder {
            items!!.add(item)
            return this
        }

        fun setTarifCode(@NonNull tarifCode: String): Builder {
            mainParams[FIELD_TARIF_CODE] = tarifCode
            return this
        }

        fun setPromocode(@NonNull promocode: String): Builder {
            mainParams[FIELD_PROMOCODE] = promocode
            return this
        }

        fun build(): AdmitadOrder {
            val json = JSONObject()
            if (userInfo != null) {
                try {
                    json.put(FIELD_USER_INFO, userInfo!!.jsonObject)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            if (items != null && items.size > 0) {
                val itemsArray = JSONArray()
                for (item in items) {
                    itemsArray.put(item.jsonObject)
                }
                try {
                    json.put(FIELD_ITEMS, itemsArray)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            mainParams[FIELD_JSON] = json.toString()
            return AdmitadOrder(mainParams)
        }

        companion object {
            private const val FIELD_ID = "oid"
            private const val FIELD_TOTAL_PRICE = "price"
            private const val FIELD_CURRENCY_CODE = "currency_code"
            private const val FIELD_JSON = "json"
            private const val FIELD_USER_INFO = "user_info"
            private const val FIELD_ITEMS = "items"
            private const val FIELD_TARIF_CODE = "tc"
            private const val FIELD_PROMOCODE = "promocode"
        }

        init {
            mainParams[FIELD_ID] = id
            mainParams[FIELD_TOTAL_PRICE] = totalPrice
        }
    }
}
