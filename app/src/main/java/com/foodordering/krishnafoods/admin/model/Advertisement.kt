
package com.foodordering.krishnafoods.admin.model

data class Advertisement(
    val id: String = "",
    val imageUrl: String = "",
    @field:JvmField // Needed for Firebase boolean mapping
    val isActive: Boolean = true,
    val timestamp: Long = 0L
)