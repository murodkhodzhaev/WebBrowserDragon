package web.browser.dragon.huawei.ui.history

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import web.browser.dragon.huawei.WebBrowserDragon
import web.browser.dragon.huawei.R
import web.browser.dragon.huawei.database.history.HistoryRecordsViewModel
import web.browser.dragon.huawei.database.history.HistoryRecordsViewModelFactory
import web.browser.dragon.huawei.model.HistoryRecord
import web.browser.dragon.huawei.ui.browser.BrowserActivity
import web.browser.dragon.huawei.ui.history.adapter.HistoryRecordAdapter
import kotlinx.android.synthetic.main.activity_history_records.*
import kotlinx.android.synthetic.main.activity_history_records.cl_toolbar
import kotlinx.android.synthetic.main.activity_history_records.content
import kotlinx.android.synthetic.main.activity_history_records.iv_back
import kotlinx.android.synthetic.main.activity_history_records.tv_toolbar_title
import web.browser.dragon.huawei.utils.Constants
import web.browser.dragon.huawei.utils.onCheckTheme
import java.util.*
import kotlin.collections.ArrayList

class HistoryRecordsActivity : AppCompatActivity() {

    companion object {
        fun newIntent(context: Context) = Intent(context, HistoryRecordsActivity::class.java)
    }

    private val historyRecordsViewModel: HistoryRecordsViewModel by viewModels {
        HistoryRecordsViewModelFactory((this.application as WebBrowserDragon).historyRecordsRepository)
    }

    private var adapter: HistoryRecordAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_records)

        if(onCheckTheme(this)) darkMode()

        initRecycler()
        observeHistoryRecords()
        setOnClickListener()
    }

    private fun darkMode() {
        tv_toolbar_title.setTextColor(ContextCompat.getColor(this, R.color.white))
        cl_toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.incognito_dark))
        content.setBackgroundColor(ContextCompat.getColor(this, R.color.incognito_dark))
    }

    private fun setOnClickListener() {
        iv_back?.setOnClickListener {
            finish()
        }
    }

    private fun initRecycler() {
        adapter = HistoryRecordAdapter(arrayListOf(), {
            onHistoryRecordClicked(it)
        }, {
            onDeleteHistoryRecordClicked(it)
        })
        rv_history_records?.layoutManager = LinearLayoutManager(this)
        rv_history_records?.adapter = adapter
    }

    private fun onHistoryRecordClicked(historyRecord: HistoryRecord) {
        startActivity(BrowserActivity.newIntent(this, historyRecord.link))
    }

    private fun onDeleteHistoryRecordClicked(historyRecord: HistoryRecord) {
        AlertDialog.Builder(this)
            .setCancelable(false)
            .setMessage(getString(R.string.history_records_delete_message))
            .setPositiveButton(
                getString(R.string.yes)
            ) { dialog, _ ->
                historyRecordsViewModel.delete(historyRecord).observe(this, Observer {
                    it?.let {
                        adapter?.removeItem(historyRecord)
                        dialog.dismiss()
                    }
                })
            }
            .setNegativeButton(
                getString(R.string.no)
            ) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun observeHistoryRecords() {
        historyRecordsViewModel?.visibleHistoryRecords?.observe(this, Observer {
            it?.let {
                adapter?.updateData(ArrayList(it))
            }
        })
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(applySelectedAppLanguage(base))
    }

    private fun applySelectedAppLanguage(context: Context): Context {
        val newestLanguage = context.getSharedPreferences(Constants.Settings.SETTINGS_LANGUAGE, Context.MODE_PRIVATE).getString(
            Constants.Settings.SETTINGS_LANGUAGE, "en")
        val locale = Locale(newestLanguage)
        val newConfig = Configuration(context.resources.configuration)
        Locale.setDefault(locale)
        newConfig.setLocale(locale)
        return context.createConfigurationContext(newConfig)
    }
}