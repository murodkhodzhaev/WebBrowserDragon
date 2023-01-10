package web.browser.dragon.huawei.utils

import android.content.Context
import web.browser.dragon.huawei.R
import web.browser.dragon.huawei.model.Onboarding

fun getOnboardingFeatures(context: Context): ArrayList<Onboarding> {

    val arr = arrayListOf<Onboarding>()

    arr.add(
        Onboarding(
            0,
            context.getString(R.string.onboarding_feature_1_title),
            context.getString(R.string.onboarding_feature_1_description),
            R.drawable.image1
        )
    )

    arr.add(
        Onboarding(
            1,
            context.getString(R.string.onboarding_feature_2_title),
            context.getString(R.string.onboarding_feature_2_description),
            R.drawable.image2
        )
    )

    arr.add(
        Onboarding(
            0,
            context.getString(R.string.onboarding_feature_3_title),
            context.getString(R.string.onboarding_feature_3_description),
            R.drawable.image3
        )
    )

    return arr
}