package web.browser.dragon.huawei.ui.onboarding.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import web.browser.dragon.huawei.model.Onboarding
import kotlinx.android.synthetic.main.item_onboarding.view.*

class OnboardingHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(onboarding: Onboarding) {
        itemView.tv_onboarding_title.text = onboarding.title
        itemView.tv_onboarding_description.text = onboarding.description
    }
}