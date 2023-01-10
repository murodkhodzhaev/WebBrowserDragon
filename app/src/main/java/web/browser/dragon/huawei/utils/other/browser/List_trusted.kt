package web.browser.dragon.huawei.utils.other.browser

import android.content.Context
import web.browser.dragon.huawei.utils.other.database.RecordAction
import web.browser.dragon.huawei.utils.other.unit.RecordUnit
import java.util.ArrayList

class List_trusted(private val context: Context) {
    fun isWhite(url: String?): Boolean {
        for (domain in listTrusted) {
            if (url != null && url.contains(domain)) {
                return true
            }
        }
        return false
    }

    @Synchronized
    fun addDomain(domain: String) {
        val action = RecordAction(context)
        action.open(true)
        action.addDomain(domain, RecordUnit.TABLE_TRUSTED)
        action.close()
        listTrusted.add(domain)
    }

    @Synchronized
    fun removeDomain(domain: String) {
        val action = RecordAction(context)
        action.open(true)
        action.deleteDomain(domain, RecordUnit.TABLE_TRUSTED)
        action.close()
        listTrusted.remove(domain)
    }

    @Synchronized
    fun clearDomains() {
        val action = RecordAction(context)
        action.open(true)
        action.clearTable(RecordUnit.TABLE_TRUSTED)
        action.close()
        listTrusted.clear()
    }

    companion object {
        private val listTrusted: MutableList<String> = ArrayList()
        @Synchronized
        private fun loadDomains(context: Context) {
            val action = RecordAction(context)
            action.open(false)
            listTrusted.clear()
            listTrusted.addAll(action.listDomains(RecordUnit.TABLE_TRUSTED))
            action.close()
        }
    }

    init {
        loadDomains(context)
    }
}