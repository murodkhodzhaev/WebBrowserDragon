package web.browser.dragon.ui.splash

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.edit
import web.browser.dragon.R
import web.browser.dragon.model.Settings
import web.browser.dragon.ui.home.HomeActivity
import web.browser.dragon.ui.onboarding.OnboardingActivity
import web.browser.dragon.utils.Constants
import web.browser.dragon.utils.isFirstLaunch
import web.browser.dragon.utils.settings.getSettings
import web.browser.dragon.utils.settings.saveSettings
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