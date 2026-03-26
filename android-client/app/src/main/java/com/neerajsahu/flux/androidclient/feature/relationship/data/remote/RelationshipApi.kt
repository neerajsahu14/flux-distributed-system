package com.neerajsahu.flux.androidclient.feature.relationship.data.remote

import com.neerajsahu.flux.androidclient.feature.relationship.data.remote.dto.*
import retrofit2.http.*

interface RelationshipApi {

    @POST("api/v1/relationship/follow/{targetUserId}")
    suspend fun toggleFollow(
        @Path("targetUserId") targetUserId: Long,
        @Body request: FollowRequest
    ): FollowActionResponse

    @GET("api/v1/relationship/{targetUserId}/followers")
    suspend fun getFollowers(
        @Path("targetUserId") targetUserId: Long,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): List<ProfileResponse>

    @GET("api/v1/relationship/{targetUserId}/following")
    suspend fun getFollowing(
        @Path("targetUserId") targetUserId: Long,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): List<ProfileResponse>

    @GET("api/v1/relationship/info/{targetUserId}")
    suspend fun getRelationshipInfo(
        @Path("targetUserId") targetUserId: Long
    ): RelationshipInfoResponse

    @GET("api/v1/relationship/stats/{targetUserId}")
    suspend fun getTargetProfileStats(
        @Path("targetUserId") targetUserId: Long
    ): ProfileStatsResponse

    @GET("api/v1/relationship/stats/me")
    suspend fun getCurrentUserProfileStats(): ProfileStatsResponse
}
