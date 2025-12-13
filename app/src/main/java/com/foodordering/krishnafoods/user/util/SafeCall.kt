package com.foodordering.krishnafoods.user.util

suspend fun <T> safeCall(onError: (Throwable) -> Unit = {}, call: suspend () -> T): T? {
    return try {
        call()
    } catch (t: Throwable) {
        onError(t)
        null
    }
}
