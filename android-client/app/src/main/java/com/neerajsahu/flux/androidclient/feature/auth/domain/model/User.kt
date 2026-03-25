package com.neerajsahu.flux.androidclient.feature.auth.domain.model

data class User(
    val id : Long,
    val username : String,
    val email : String,
    val bio : String?,
    val profilePicUrl : String?
)
