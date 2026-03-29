package com.neerajsahu.flux.androidclient.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable
    data object Login : Route

    @Serializable
    data object SignUp : Route

    @Serializable
    data object Main : Route

    @Serializable
    data object NewsFeed : Route

    @Serializable
    data object Explore : Route

    @Serializable
    data object CreatePost : Route

    @Serializable
    data object Notifications : Route

    @Serializable
    data object Profile : Route

    @Serializable
    data object EditProfile : Route

    @Serializable
    data class UserProfile(val userId: Long) : Route

    @Serializable
    data class Connections(val userId: Long, val initialTab: Int) : Route

    @Serializable
    data class PostDetail(val postId: Long) : Route
}
