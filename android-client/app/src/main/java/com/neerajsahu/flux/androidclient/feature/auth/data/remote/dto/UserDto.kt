package com.neerajsahu.flux.androidclient.feature.auth.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id") val id: Long,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("bio") val bio: String?,
    @SerializedName("profilePicUrl") val profilePicUrl: String?
)