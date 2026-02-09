package com.neerajsahu.flux.server.feed.api

import com.neerajsahu.flux.server.auth.domain.model.User
import com.neerajsahu.flux.server.feed.service.FeedService
import com.neerajsahu.flux.server.feed.api.dto.*
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/feed")
class FeedController(
    private val feedService: FeedService
) {

    // ==================== CREATE ====================
    @PostMapping("/post")
    fun createPost(
        @RequestParam("image") file: MultipartFile,
        @RequestParam("caption") caption: String?,
        @RequestParam("requestId") requestId: String,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<PostResponse> {
        val req = CreatePostRequest(
            caption = caption,
            requestId = requestId
        )
        return ResponseEntity.ok(feedService.createPost(user, file, req))
    }

    // ==================== READ ====================
    @GetMapping("/posts")
    fun getGlobalFeed(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<PostResponse>> {
        return ResponseEntity.ok(feedService.getGlobalFeed(page, size))
    }

    @GetMapping("/user/{userId}/post")
    fun getUserFeed(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<PostResponse>> {
        return ResponseEntity.ok(feedService.getUserFeed(userId, page, size))
    }

    @GetMapping("/post/{postId}")
    fun getPostByPostId(
        @PathVariable postId: Long
    ): ResponseEntity<PostResponse> {
        return ResponseEntity.ok(feedService.findPostByPostId(postId))
    }

    @GetMapping("/post/{postId}/detail")
    fun getPostDetail(
        @PathVariable postId: Long,
        @AuthenticationPrincipal user: User?
    ): ResponseEntity<PostDetailResponse> {
        return ResponseEntity.ok(feedService.getPostDetail(postId, user))
    }

    // ==================== UPDATE ====================
    @PutMapping("/post/{postId}")
    fun updatePost(
        @PathVariable postId: Long,
        @RequestBody req: UpdatePostRequest,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<PostResponse> {
        return ResponseEntity.ok(feedService.updatePost(user, postId, req))
    }

    // ==================== DELETE ====================
    @DeleteMapping("/post/{postId}")
    fun deletePost(
        @PathVariable postId: Long,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<Map<String, Any>> {
        val result = feedService.deletePost(user, postId)
        return ResponseEntity.ok(mapOf("success" to result, "message" to "Post deleted successfully"))
    }
}