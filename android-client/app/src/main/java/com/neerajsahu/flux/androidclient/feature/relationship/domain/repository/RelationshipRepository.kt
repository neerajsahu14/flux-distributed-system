package com.neerajsahu.flux.androidclient.feature.relationship.domain.repository

import com.neerajsahu.flux.androidclient.core.utils.AppResult
import com.neerajsahu.flux.androidclient.feature.auth.data.remote.dto.UserResponse
import com.neerajsahu.flux.androidclient.feature.relationship.data.remote.dto.*
import com.neerajsahu.flux.androidclient.feature.relationship.domain.model.ProfileStats
import kotlinx.coroutines.flow.Flow

interface RelationshipRepository {
    suspend fun toggleFollow(targetUserId: Long, requestId: String): AppResult<FollowActionResponse>
    suspend fun getFollowers(userId: Long, page: Int, size: Int): AppResult<List<UserResponse>>
    suspend fun getFollowing(userId: Long, page: Int, size: Int): AppResult<List<UserResponse>>
    suspend fun getRelationshipInfo(targetUserId: Long): AppResult<RelationshipInfoResponse>
    
    fun getProfileStats(userId: Long): Flow<AppResult<ProfileStats>>
    suspend fun getCurrentUserProfileStats(): AppResult<ProfileStatsResponse>
}
