package web.browser.dragon.huawei.ui.splash

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.edit
import web.browser.dragon.huawei.R
import web.browser.dragon.huawei.model.Settings
import web.browser.dragon.huawei.ui.home.HomeActivity
import web.browser.dragon.huawei.ui.onboarding.OnboardingActivity
import web.browser.dragon.huawei.utils.Constants
import web.browser.dragon.huawei.utils.appsflyer.Utils.isFirstLaunch
import web.browser.dragon.huawei.utils.settings.getSettings
import web.browser.dragon.huawei.utils.settings.saveSettings
import java.util.*

class SplashActivity : AppCompatActivity() {

    private val nameTabsOfIncognito = "tabs_incognito"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        setSettings()
        chooseNextActivity()
    }

    private fun setSettings() {
        getSharedPreferences(nameTabsOfIncognito, Context.MODE_PRIVATE).edit().clear().apply()
        if(getSettings(this) == null) {
            saveSettings(this, Settings())
        }
    }

    private fun chooseNextActivity() {
        if(isFirstLaunch(this)) {
            val languagesCodeArray: Array<String> = resources.getStringArray(R.array.languages_code)
            var code = Locale.getDefault().language
            if(code !in languagesCodeArray){
                code = "en"
            }

            getSharedPreferences(Constants.Settings.SETTINGS_LANGUAGE, Context.MODE_PRIVATE).edit {
                this.putString(Constants.Settings.SETTINGS_LANGUAGE, code)
            }
            startActivity(OnboardingActivity.newIntent(this))
        }
        else {
            startActivity(HomeActivity.newIntent(this))
        }
    }
}