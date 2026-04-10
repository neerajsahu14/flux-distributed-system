package com.neerajsahu.flux.androidclient.feature.feed.presentation.intent

sealed class PostDetailIntent {
    data class Load(val postId: Long) : PostDetailIntent()
    data class Retry(val postId: Long) : PostDetailIntent()
    object Like : PostDetailIntent()
    object Bookmark : PostDetailIntent()
    object Share : PostDetailIntent()
}

