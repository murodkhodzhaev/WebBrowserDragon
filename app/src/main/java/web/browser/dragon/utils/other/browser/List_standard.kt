package web.browser.dragon.utils.other.browser

import android.content.Context
import web.browser.dragon.utils.other.database.RecordAction
import web.browser.dragon.utils.other.unit.RecordUnit
import java.util.ArrayList

class List_standard(private val context: Context) {
    fun isWhite(url: String?): Boolean {
        for (domain in listStandard) {
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
        action.addDomain(domain, RecordUnit.TABLE_STANDARD)
        action.close()
        listStandard.add(domain)
    }

    @Synchronized
    fun removeDomain(domain: String) {
        val action = RecordAction(context)
        action.open(true)
        action.deleteDomain(domain, RecordUnit.TABLE_STANDARD)
        action.close()
        listStandard.remove(domain)
    }

    @Synchronized
    fun clearDomains() {
        val action = RecordAction(context)
        action.open(true)
        action.clearTable(RecordUnit.TABLE_STANDARD)
        action.close()
        listStandard.clear()
    }

    companion object {
        private val listStandard: MutableList<String> = ArrayList()
        @Synchronized
        private fun loadDomains(context: Context) {
            val action = RecordAction(context)
            action.open(false)
            listStandard.clear()
            listStandard.addAll(action.listDomains(RecordUnit.TABLE_STANDARD))
            action.close()
        }
    }

    init {
        loadDomains(context)
    }
}