package com.example.foodorderingapp.user.util

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.foodorderingapp.user.fragment.CartFragment
import com.example.foodorderingapp.user.fragment.MenuFragment
import com.example.foodorderingapp.user.fragment.OrdersFragment

class ViewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3 // Number of tabs (Menu, Cart, Orders)

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MenuFragment()
            1 -> CartFragment()
            2 -> OrdersFragment()
            else -> MenuFragment()
        }
    }
}
