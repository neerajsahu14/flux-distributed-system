package com.neerajsahu.flux.androidclient.core.utils

sealed class AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>()
    data class Error(val message: String) : AppResult<Nothing>()
}
