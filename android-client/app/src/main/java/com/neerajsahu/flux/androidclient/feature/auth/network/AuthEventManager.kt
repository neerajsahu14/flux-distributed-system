package com.neerajsahu.flux.androidclient.feature.auth.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AuthEventManager {
    private val _unauthorizedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val unauthorizedEvent = _unauthorizedEvent.asSharedFlow()

    fun triggerUnauthorizedEvent() {
        _unauthorizedEvent.tryEmit(Unit)
    }
}

