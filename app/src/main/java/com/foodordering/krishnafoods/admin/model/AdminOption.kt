package com.foodordering.krishnafoods.admin.model

data class AdminOption(
    val title: String,
    val iconResId: Int,
    val activityClass: Class<*>
)