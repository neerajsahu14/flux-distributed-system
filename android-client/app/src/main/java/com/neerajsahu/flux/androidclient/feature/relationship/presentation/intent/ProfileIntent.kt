package com.neerajsahu.flux.androidclient.feature.relationship.presentation.intent

sealed class ProfileIntent {
    data class LoadProfile(val userId: Long, val forceRefresh: Boolean = false) : ProfileIntent()
    data class LoadFollowers(val userId: Long, val forceRefresh: Boolean = false) : ProfileIntent()
    data class LoadFollowing(val userId: Long, val forceRefresh: Boolean = false) : ProfileIntent()
    data class ToggleFollow(val userId: Long) : ProfileIntent()
    object ClearError : ProfileIntent()
}


