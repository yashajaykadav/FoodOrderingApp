package com.foodordering.krishnafoods.user.util

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.foodordering.krishnafoods.user.fragment.CartFragment
import com.foodordering.krishnafoods.user.fragment.MenuFragment
import com.foodordering.krishnafoods.user.fragment.OrdersFragment

class ViewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MenuFragment()
            1 -> CartFragment()
            2 -> OrdersFragment()
            else -> MenuFragment()
        }
    }
}
