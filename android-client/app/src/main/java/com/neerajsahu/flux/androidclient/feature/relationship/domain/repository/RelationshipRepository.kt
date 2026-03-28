package com.neerajsahu.flux.androidclient.feature.relationship.domain.repository

import com.neerajsahu.flux.androidclient.core.utils.AppResult
import com.neerajsahu.flux.androidclient.feature.relationship.data.remote.dto.*
import com.neerajsahu.flux.androidclient.feature.relationship.domain.model.ProfileStats
import com.neerajsahu.flux.androidclient.feature.relationship.domain.model.RelationshipUser
import kotlinx.coroutines.flow.Flow

interface RelationshipRepository {
    suspend fun toggleFollow(targetUserId: Long, requestId: String): AppResult<FollowActionResponse>
    
    fun getFollowers(userId: Long, page: Int, size: Int): Flow<AppResult<List<RelationshipUser>>>
    fun getFollowing(userId: Long, page: Int, size: Int): Flow<AppResult<List<RelationshipUser>>>
    
    suspend fun getRelationshipInfo(targetUserId: Long): AppResult<RelationshipInfoResponse>
    
    fun getProfileStats(userId: Long): Flow<AppResult<ProfileStats>>
    fun getCurrentUserProfileStats(): Flow<AppResult<ProfileStats>>
    
    fun searchUsers(query: String, page: Int, size: Int): Flow<AppResult<List<RelationshipUser>>>
}
