package com.neerajsahu.flux.androidclient.feature.auth.mapper

import com.neerajsahu.flux.androidclient.feature.auth.data.local.UserEntity
import com.neerajsahu.flux.androidclient.feature.auth.data.remote.dto.UserDto
import com.neerajsahu.flux.androidclient.feature.auth.domain.model.User

fun UserDto.toUserEntity(): UserEntity {
    return UserEntity(
        id = id,
        username = username,
        email = email,
        bio = bio,
        profilePicUrl = profilePicUrl
    )
}

fun UserEntity.toUser(): User {
    return User(
        id = id,
        username = username,
        email = email,
        bio = bio,
        profilePicUrl = profilePicUrl
    )
}
