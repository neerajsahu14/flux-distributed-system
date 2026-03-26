package com.neerajsahu.flux.androidclient.feature.relationship.mapper

import com.neerajsahu.flux.androidclient.feature.relationship.data.local.ProfileStatsEntity
import com.neerajsahu.flux.androidclient.feature.relationship.data.remote.dto.ProfileStatsResponse
import com.neerajsahu.flux.androidclient.feature.relationship.domain.model.ProfileStats

fun ProfileStatsResponse.toProfileStatsEntity(): ProfileStatsEntity {
    return ProfileStatsEntity(
        userId = profile.id,
        username = profile.username,
        fullName = profile.fullName,
        bio = profile.bio,
        postCount = postCount,
        followersCount = followersCount,
        followingCount = followingCount,
        isFollowing = isFollowing
    )
}

fun ProfileStatsEntity.toProfileStats(): ProfileStats {
    return ProfileStats(
        userId = userId,
        username = username,
        fullName = fullName,
        bio = bio,
        postCount = postCount,
        followersCount = followersCount,
        followingCount = followingCount,
        isFollowing = isFollowing
    )
}

fun ProfileStatsResponse.toProfileStats(): ProfileStats {
    return ProfileStats(
        userId = profile.id,
        username = profile.username,
        fullName = profile.fullName,
        bio = profile.bio,
        postCount = postCount,
        followersCount = followersCount,
        followingCount = followingCount,
        isFollowing = isFollowing
    )
}
