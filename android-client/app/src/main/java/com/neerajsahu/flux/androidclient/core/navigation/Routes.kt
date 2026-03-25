package com.neerajsahu.flux.androidclient.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable
    data object Login : Route

    @Serializable
    data object SignUp : Route

    @Serializable
    data object Home : Route
}
