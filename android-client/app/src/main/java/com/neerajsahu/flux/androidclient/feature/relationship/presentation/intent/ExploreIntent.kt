package com.neerajsahu.flux.androidclient.feature.relationship.presentation.intent

sealed class ExploreIntent {
    data class QueryChanged(val query: String) : ExploreIntent()
    data class ToggleFollow(val userId: Long) : ExploreIntent()
    object ClearError : ExploreIntent()
}



