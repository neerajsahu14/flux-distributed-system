package com.neerajsahu.flux.androidclient.feature.interaction.data.remote

import com.neerajsahu.flux.androidclient.feature.feed.data.remote.dto.PostResponseDto
import com.neerajsahu.flux.androidclient.feature.interaction.data.remote.dto.InteractionRequestDto
import com.neerajsahu.flux.androidclient.feature.interaction.data.remote.dto.InteractionResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface InteractionApi {

    @POST("api/v1/interaction/post/{postId}/like")
    suspend fun likePost(
        @Path("postId") postId: Long,
        @Body request: InteractionRequestDto
    ): InteractionResponseDto

    @HTTP(method = "DELETE", path = "api/v1/interaction/post/{postId}/like", hasBody = true)
    suspend fun unlikePost(
        @Path("postId") postId: Long,
        @Body request: InteractionRequestDto
    ): InteractionResponseDto

    @POST("api/v1/interaction/post/{postId}/bookmark")
    suspend fun bookmarkPost(
        @Path("postId") postId: Long,
        @Body request: InteractionRequestDto
    ): InteractionResponseDto

    @HTTP(method = "DELETE", path = "api/v1/interaction/post/{postId}/bookmark", hasBody = true)
    suspend fun unbookmarkPost(
        @Path("postId") postId: Long,
        @Body request: InteractionRequestDto
    ): InteractionResponseDto

    @POST("api/v1/interaction/post/{postId}/share")
    suspend fun sharePost(
        @Path("postId") postId: Long,
        @Body request: InteractionRequestDto
    ): InteractionResponseDto

    @GET("api/v1/interaction/bookmarks")
    suspend fun getBookmarkedPosts(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): List<PostResponseDto>

    @GET("api/v1/interaction/likes")
    suspend fun getLikedPosts(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): List<PostResponseDto>

    @GET("api/v1/interaction/post/{postId}/liked")
    suspend fun isPostLiked(@Path("postId") postId: Long): Map<String, Boolean>

    @GET("api/v1/interaction/post/{postId}/bookmarked")
    suspend fun isPostBookmarked(@Path("postId") postId: Long): Map<String, Boolean>
}

