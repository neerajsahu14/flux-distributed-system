package com.neerajsahu.flux.androidclient.feature.feed.presentation.intent

sealed class FeedIntent {
    data class LoadGlobal(val forceRefresh: Boolean = false) : FeedIntent()
    data class LoadTimeline(val forceRefresh: Boolean = false) : FeedIntent()
    object LoadMore : FeedIntent()
    object Refresh : FeedIntent()

    data class Like(val postId: Long) : FeedIntent()
    data class Bookmark(val postId: Long) : FeedIntent()
    data class Share(val postId: Long) : FeedIntent()

    object ApplyNewPosts : FeedIntent()
}

