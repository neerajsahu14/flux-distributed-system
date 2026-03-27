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
    data object Profile : Route

    @Serializable
    data class UserProfile(val userId: Long) : Route

    @Serializable
    data class Connections(val userId: Long, val initialTab: Int) : Route
}
