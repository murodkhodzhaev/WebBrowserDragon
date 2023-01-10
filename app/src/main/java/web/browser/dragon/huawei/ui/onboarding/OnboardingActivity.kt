package web.browser.dragon.huawei.ui.onboarding

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import web.browser.dragon.huawei.WebBrowserDragon
import web.browser.dragon.huawei.R
import web.browser.dragon.huawei.database.bookmarks.BookmarksViewModel
import web.browser.dragon.huawei.database.bookmarks.BookmarksViewModelFactory
import web.browser.dragon.huawei.ui.home.HomeActivity
import web.browser.dragon.huawei.ui.onboarding.adapter.OnboardingAdapter
import kotlinx.android.synthetic.main.activity_onboarding.*
import web.browser.dragon.huawei.utils.Constants
import web.browser.dragon.huawei.utils.getBookmarks
import web.browser.dragon.huawei.utils.getOnboardingFeatures
import web.browser.dragon.huawei.utils.setIsFirstLaunch
import java.util.*

class OnboardingActivity : AppCompatActivity() {

    private val bookmarksViewModel: BookmarksViewModel by viewModels {
        BookmarksViewModelFactory((this.application as WebBrowserDragon).bookmarksRepository)
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, OnboardingActivity::class.java)
    }

    private var adapter: OnboardingAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        setOnClickListeners()
        initViewPager()
    }

    private fun setOnClickListeners() {
        b_next?.setOnClickListener {
            onNextClicked()
        }
    }

    private fun goToHomeActivity() {
        insertInitialBookmarks()
    }

    private fun insertInitialBookmarks() {
        bookmarksViewModel.insertAll(getBookmarks()).observe(this, Observer {
            it?.let {
                setIsFirstLaunch(this, false)
                finishAffinity()
                startActivity(HomeActivity.newIntent(this))
            }
        })
    }

    private fun onNextClicked() {
        if(viewpager?.currentItem != null) {
            if (viewpager.currentItem < 2) {
                viewpager.currentItem = viewpager.currentItem + 1
            } else {
                goToHomeActivity()
            }
        }
        else {
            goToHomeActivity()
        }
    }

    private fun initViewPager() {
        viewpager?.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        adapter = OnboardingAdapter(arrayListOf())
        viewpager?.adapter = adapter
        ci2_dots?.setViewPager(viewpager)
        adapter?.registerAdapterDataObserver(ci2_dots.adapterDataObserver)

        viewpager?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if (position == 0) {
                    b_next?.text = getString(R.string.onboarding_next)
                    iv_onboarding_image?.setImageResource(R.drawable.image1)
                }
                if (position == 1) {
                    b_next?.text = getString(R.string.onboarding_next)
                    iv_onboarding_image?.setImageResource(R.drawable.image2)
                }
                if (position == 2) {
                    b_next?.text = getString(R.string.onboarding_start)
                    iv_onboarding_image?.setImageResource(R.drawable.image3)
                }
            }
        })

        adapter?.updateData(getOnboardingFeatures(this))
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