package com.example.foodorderingapp.admin.model

import com.example.foodorderingapp.R

data class AdminOption(
    val title: String,
    val iconResId: Int,
    val activityClass: Class<*>,
    val cardColor: Int = R.color.card_default
)