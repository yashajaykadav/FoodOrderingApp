package com.foodordering.krishnafoods.core.util

// Author: Yash Kadav
// Email: yashkadav52@gmail.com

import android.app.Activity
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

// Module: UI Helper to handle Edge-to-Edge logic
fun Activity.applyEdgeToEdge(rootView: View, toolbar: View? = null) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        toolbar?.updatePadding(top = systemBars.top)
        view.updatePadding(bottom = systemBars.bottom)
        WindowInsetsCompat.CONSUMED
    }
}

// Module: Firestore Helper to get counts easily
suspend fun Query.getCountOrDash(): String {
    return try {
        this.get().await().size().toString()
    } catch (_: Exception) {
        "-"
    }
}

suspend fun CollectionReference.getCountOrDash(): String {
    return try {
        this.get().await().size().toString()
    } catch (_: Exception) {
        "-"
    }
}

