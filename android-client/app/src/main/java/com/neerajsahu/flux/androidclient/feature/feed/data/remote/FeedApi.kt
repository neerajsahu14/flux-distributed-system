package com.neerajsahu.flux.androidclient.feature.feed.data.remote

import com.neerajsahu.flux.androidclient.feature.feed.data.remote.dto.PostDetailResponseDto
import com.neerajsahu.flux.androidclient.feature.feed.data.remote.dto.PostResponseDto
import com.neerajsahu.flux.androidclient.feature.feed.data.remote.dto.UpdatePostRequestDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface FeedApi {

    @GET("api/v1/feed/posts")
    suspend fun getGlobalFeed(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): List<PostResponseDto>

    @GET("api/v1/feed/user/{userId}/post")
    suspend fun getUserFeed(
        @Path("userId") userId: Long,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): List<PostResponseDto>

    @GET("api/v1/feed/timeline")
    suspend fun getTimelineFeed(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): List<PostResponseDto>

    @GET("api/v1/feed/post/{postId}")
    suspend fun getPostById(@Path("postId") postId: Long): PostResponseDto

    @GET("api/v1/feed/post/{postId}/detail")
    suspend fun getPostDetail(@Path("postId") postId: Long): PostDetailResponseDto

    @Multipart
    @POST("api/v1/feed/post")
    suspend fun createPost(
        @Part image: MultipartBody.Part,
        @Part("caption") caption: RequestBody?,
        @Part("requestId") requestId: RequestBody
    ): PostResponseDto

    @PUT("api/v1/feed/post/{postId}")
    suspend fun updatePost(
        @Path("postId") postId: Long,
        @Body request: UpdatePostRequestDto
    ): PostResponseDto

    @DELETE("api/v1/feed/post/{postId}")
    suspend fun deletePost(@Path("postId") postId: Long): Map<String, Any>
}

