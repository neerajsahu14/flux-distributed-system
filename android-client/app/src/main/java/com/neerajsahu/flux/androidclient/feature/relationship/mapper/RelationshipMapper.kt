package com.neerajsahu.flux.androidclient.feature.relationship.mapper

import com.neerajsahu.flux.androidclient.feature.relationship.data.local.ProfileStatsEntity
import com.neerajsahu.flux.androidclient.feature.relationship.data.remote.dto.ProfileResponse
import com.neerajsahu.flux.androidclient.feature.relationship.data.remote.dto.ProfileStatsResponse
import com.neerajsahu.flux.androidclient.feature.relationship.domain.model.ProfileStats
import com.neerajsahu.flux.androidclient.feature.relationship.domain.model.RelationshipUser

fun ProfileStatsResponse.toProfileStatsEntity(): ProfileStatsEntity {
    return ProfileStatsEntity(
        userId = profile.id,
        username = profile.username,
        fullName = profile.fullName,
        profilePicUrl = profile.profilePicUrl,
        bio = profile.bio,
        postCount = postCount,
        followersCount = followersCount,
        followingCount = followingCount,
        isFollowing = isFollowing,
        isFollowedBy = profile.isFollowedBy
    )
}

fun ProfileStatsEntity.toProfileStats(): ProfileStats {
    return ProfileStats(
        userId = userId,
        username = username,
        fullName = fullName,
        profilePicUrl = profilePicUrl,
        bio = bio,
        postCount = postCount,
        followersCount = followersCount,
        followingCount = followingCount,
        isFollowing = isFollowing,
        isFollowedBy = isFollowedBy
    )
}

fun ProfileStatsResponse.toProfileStats(): ProfileStats {
    return ProfileStats(
        userId = profile.id,
        username = profile.username,
        fullName = profile.fullName,
        profilePicUrl = profile.profilePicUrl,
        bio = profile.bio,
        postCount = postCount,
        followersCount = followersCount,
        followingCount = followingCount,
        isFollowing = isFollowing,
        isFollowedBy = profile.isFollowedBy
    )
}

fun ProfileResponse.toRelationshipUser(): RelationshipUser {
    return RelationshipUser(
        id = id,
        username = username,
        fullName = fullName,
        profilePicUrl = profilePicUrl,
        isFollowing = isFollowing,
        isFollowedBy = isFollowedBy
    )
}
