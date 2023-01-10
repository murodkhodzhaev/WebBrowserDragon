package web.browser.dragon.huawei.utils.other.browser

import android.content.Context
import web.browser.dragon.huawei.utils.other.database.RecordAction
import web.browser.dragon.huawei.utils.other.unit.RecordUnit
import java.util.ArrayList

class List_protected(private val context: Context) {
    fun isWhite(url: String?): Boolean {
        for (domain in listProtected) {
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
        action.addDomain(domain, RecordUnit.TABLE_PROTECTED)
        action.close()
        listProtected.add(domain)
    }

    @Synchronized
    fun removeDomain(domain: String) {
        val action = RecordAction(context)
        action.open(true)
        action.deleteDomain(domain, RecordUnit.TABLE_PROTECTED)
        action.close()
        listProtected.remove(domain)
    }

    @Synchronized
    fun clearDomains() {
        val action = RecordAction(context)
        action.open(true)
        action.clearTable(RecordUnit.TABLE_PROTECTED)
        action.close()
        listProtected.clear()
    }

    companion object {
        private val listProtected: MutableList<String> = ArrayList()
        @Synchronized
        private fun loadDomains(context: Context) {
            val action = RecordAction(context)
            action.open(false)
            listProtected.clear()
            listProtected.addAll(action.listDomains(RecordUnit.TABLE_PROTECTED))
            action.close()
        }
    }

    init {
        loadDomains(context)
    }
}
