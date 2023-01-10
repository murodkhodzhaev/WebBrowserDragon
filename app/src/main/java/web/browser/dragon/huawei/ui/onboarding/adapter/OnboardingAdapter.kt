package web.browser.dragon.huawei.ui.onboarding.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import web.browser.dragon.huawei.R
import web.browser.dragon.huawei.model.Onboarding

class OnboardingAdapter(var list: ArrayList<Onboarding>): RecyclerView.Adapter<OnboardingHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_onboarding, parent, false)
        return OnboardingHolder(view)
    }

    override fun getItemCount(): Int = list.size
    override fun onBindViewHolder(holder: OnboardingHolder, position: Int) = holder.bind(list[position])

    fun updateData(list: ArrayList<Onboarding>) {
        this.list = list
        notifyDataSetChanged()
    }
}