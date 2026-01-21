package com.dktech.baseandroidviewdktech.ui.home.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dktech.baseandroidviewdktech.ui.home.fragment.ListHomeFragment

class VPAdapter(
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        return ListHomeFragment()
    }

    override fun getItemCount(): Int = 4
}