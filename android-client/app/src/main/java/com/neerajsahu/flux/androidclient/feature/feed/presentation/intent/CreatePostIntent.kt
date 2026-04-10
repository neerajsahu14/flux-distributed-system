package com.neerajsahu.flux.androidclient.feature.feed.presentation.intent

import okhttp3.MultipartBody

sealed class CreatePostIntent {
    data class CaptionChanged(val caption: String) : CreatePostIntent()
    data class MediaSelected(val uri: String?) : CreatePostIntent()
    object ClearSelectedMedia : CreatePostIntent()
    data class Submit(val mediaPart: MultipartBody.Part) : CreatePostIntent()
    object ClearError : CreatePostIntent()
    data class SetError(val message: String) : CreatePostIntent()
    object ConsumeSuccess : CreatePostIntent()
}

